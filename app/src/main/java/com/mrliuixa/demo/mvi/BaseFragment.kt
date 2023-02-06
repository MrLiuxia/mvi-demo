package com.mrliuixa.demo.mvi

import androidx.fragment.app.Fragment

abstract class BaseFragment<STATE : MVI.UIState, INTENT : MVI.Intent>(view: MVI.IView<STATE, INTENT>) : Fragment(),
    MVI.IView<STATE, INTENT> by view {
}