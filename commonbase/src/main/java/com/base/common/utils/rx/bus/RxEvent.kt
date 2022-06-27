package com.base.common.utils.rx.bus

import com.base.common.constant.AppConstant

class RxEvent {

    data class EventCloseDialog(val dialogState: AppConstant.DialogState, val tag : String?)

    data class EventDialog(val tag : String?,val data : Any)
}