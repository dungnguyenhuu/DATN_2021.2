package vn.app.qrcode.data.model

import com.google.gson.annotations.SerializedName

data class CheckVersionResponse(
    @SerializedName("url")
    val url: String
)