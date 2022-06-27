package com.base.common.data.result

import android.text.TextUtils
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ErrorApi(
    @Expose
    @SerializedName("message")
    var message: String,
    @Expose
    @SerializedName("error_code")
    var errorCode: String,
    @Expose
    @SerializedName("datas")
    var datas: JsonObject? = null
) {

    fun mapErrorCode(): String {
        if (TextUtils.isEmpty(errorCode)) return ""
        message = convertErrorCode(errorCode)
        return message
    }
}

data class ErrorApi2(
    @Expose
    @SerializedName("message")
    var message: String,
    @Expose
    @SerializedName("error_code")
    var errorCode: String
) {

    fun mapErrorCode() {
        if (TextUtils.isEmpty(errorCode)) return
        message = convertErrorCode(errorCode)
    }
}

fun convertErrorCode(errorCode: String) = when (errorCode) {
    "EMPTY_USERNAME" -> "Yêu cầu nhập username."

    else -> "no message"
}
