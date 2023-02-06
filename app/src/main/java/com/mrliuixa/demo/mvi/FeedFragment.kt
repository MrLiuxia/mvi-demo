package com.mrliuixa.demo.mvi

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Description:
 * Author: liuxia
 * Date: 2023/2/3
 *
 * @blame: liuxia
 */
class FeedFragment : Fragment(), MVI.BaseView<FeedUIState, FeedIntent, FeedViewModel> {
    override fun onUIState(state: FeedUIState) {

    }

    override var _viewModel: FeedViewModel? = null

    override fun createViewModel(): FeedViewModel {
        println(arguments)
        return FeedViewModel()
    }
}


class FeedViewModel : MVI.BaseViewModel<FeedIntent, FeedUIState>() {
    override fun initialState(): FeedUIState = FeedUIState.Initial

    override suspend fun onIntent(intent: MVI.Intent) {

    }

}


sealed class FeedIntent : MVI.Intent {
    object LoadNet : FeedIntent()
    object Favorite : FeedIntent()                                        // 收藏
    data class Share(val activity: FragmentActivity) : FeedIntent()       // 分享
}

sealed class FeedUIState : MVI.UIState {
    object Initial : FeedUIState()
    object Loading : FeedUIState()
    object Error : FeedUIState()
    object Empty : FeedUIState()
    object Ready : FeedUIState()
    data class UpdateContent(val data: String) : FeedUIState()
}


