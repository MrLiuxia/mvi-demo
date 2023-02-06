package com.mrliuixa.demo.mvi

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

interface MVI {

    /**
     * 接口定义，View [UIState] - [Intent] ViewModel
     */
    interface UIState
    interface Intent

    interface IView<STATE : UIState, INTENT : Intent> {
        fun onUIState(state: STATE)
        fun sendIntent(intent: INTENT)
    }

    interface IViewModel<INTENT : Intent, STATE : UIState> {
        suspend fun onIntent(intent: Intent)
        suspend fun setUIState(state: STATE)
    }


    interface BaseView<STATE : UIState, INTENT : Intent, VM : BaseViewModel<INTENT, STATE>> : IView<STATE, INTENT>,
        LifecycleOwner {

        var _viewModel: VM?

        val mViewModel: VM
            get() = _viewModel ?: createViewModel().apply {
                lifecycleScope.launchWhenStarted {
                    uiStateFlow.collect {
                        Log.d(TAG, "onUIState: $it")
                        onUIState(it)
                    }
                }
                _viewModel = this
            }

        fun createViewModel(): VM

        override fun sendIntent(intent: INTENT) {
            lifecycleScope.launch {
                mViewModel.intentChannel.send(intent)
            }
        }
    }


    /**
     * 基类，View ViewModel，包装了之间的UIState、Intent基础传递与处理
     */
    abstract class BaseViewDep<STATE : UIState, INTENT : Intent, VM : BaseViewModel<INTENT, STATE>>(
        private val lifecycleOwner: LifecycleOwner
    ) : IView<STATE, INTENT>, LifecycleOwner by lifecycleOwner {

        val TAG: String
            get() = "MVI-${this@BaseViewDep.javaClass.simpleName}"

        val viewModel: VM by lazy {
            createViewModel().apply {
                lifecycleScope.launchWhenStarted {
                    uiStateFlow.collect {
                        Log.d(TAG, "onUIState: $it")
                        onUIState(it)
                    }
                }
            }
        }

        abstract fun createViewModel(): VM

        override fun sendIntent(intent: INTENT) {
            lifecycleScope.launch {
                Log.d(TAG, "sendIntent: $intent")
                viewModel.intentChannel.send(intent)
            }
        }
    }

    abstract class BaseViewModel<INTENT : Intent, STATE : UIState> : IViewModel<INTENT, STATE>, ViewModel() {

        val TAG = "MVI-${this@BaseViewModel.javaClass.simpleName}"

        private val _uiStateFlow: MutableStateFlow<STATE> by lazy { MutableStateFlow(initialState()) }
        val uiStateFlow: StateFlow<STATE> = _uiStateFlow
        val intentChannel = Channel<Intent>()

        init {
            viewModelScope.launch {
                intentChannel.consumeAsFlow().collect {
                    Log.d(TAG, "onIntent: $it")
                    onIntent(it)
                }
            }
        }

        abstract fun initialState(): STATE

        override suspend fun setUIState(state: STATE) {
            Log.d(TAG, "setUIState: $state")
            _uiStateFlow.emit(state)
        }

    }

}
