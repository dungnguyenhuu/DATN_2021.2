package vn.app.qrcode.base

import com.base.common.base.viewmodel.BaseViewModel

abstract class BaseItemViewModel<E, T : BaseViewModel<E>> : BaseViewModel<E>() {
    lateinit var mainViewModel: T
}