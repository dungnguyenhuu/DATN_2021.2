package com.base.common.data.event

import android.text.InputType
import com.google.gson.JsonObject

class MessageDialog(var title : String = "Thông báo",
                    var content : String,
                    var confirmButton : String = "ok",
                    var cancelButton : String = "Hủy",
                    var tag : String = "Default",
                    var datas: JsonObject? = null,
                    var hint : String = "",
                    var inputType: Int = InputType.TYPE_CLASS_TEXT,
                    var showCancel : Boolean = false){
    var content2: String = ""
}