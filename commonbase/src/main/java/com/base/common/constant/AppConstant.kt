package com.base.common.constant

object AppConstant {
    const val SUPPORT_NUMBER = "0914607788"
    const val MAX_COUNT_DOWN = 60

    const val HEADER_ACCEPT = "Accept"
    const val HEADER_AUTHORIZATION = "Access-Token"
    const val HEADER_USER_AGENT = "User-Agent"
    const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
    const val HEADER_CONTENT_TYPE = "Content-type"

    //------
    // Header Values
    //------
    const val HEADER_ACCEPT_VALUE = "application/json"
    const val HEADER_AUTHORIZATION_VALUE = "Bearer %s"

    // ERROR
    const val SESSION_TIMEOUT = "401"
    const val UPDATE_APP = "426"
    const val CALL_ERROR = "422"
    const val UPDATE_APP_TAG = "UPDATE"

    // DIALOG
    enum class DialogState {
        Close,
        Call,
        Back
    }
}