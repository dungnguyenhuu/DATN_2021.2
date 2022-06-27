package vn.app.qrcode.data.usermanager

import android.text.TextUtils
import vn.app.qrcode.utils.sharepref.SharedPreUtils

class UserManager {
    private val USER_TOKEN = "user_token"
    private val USER_GOOGLE_TOKEN = "user_google_token"
    private val FIREBASE_TOKEN = "firebase_token"
    private val IS_USER_LOGIN = "is_user_login"
    fun setIsUserLogin(isUserLogin: Boolean) = SharedPreUtils.putBoolean(IS_USER_LOGIN, isUserLogin)

    fun saveUserToken(token: String) = SharedPreUtils.putString(USER_TOKEN, token)

    fun saveUserGoogleToken(token: String) = SharedPreUtils.putString(USER_GOOGLE_TOKEN, token)

    fun getUserToken(): String {
        val userToken = SharedPreUtils.getString(USER_TOKEN)
        return userToken ?: ""
    }

    fun getUserGoogleToken() = SharedPreUtils.getString(USER_GOOGLE_TOKEN)

    fun isUserLoggedIn() = (SharedPreUtils.getBoolean(IS_USER_LOGIN, false)
            && !TextUtils.isEmpty(getUserToken()))

    fun setFireBaseToken(firebaseToken: String) =
        SharedPreUtils.putString(FIREBASE_TOKEN, firebaseToken)

    fun logout() {
        SharedPreUtils.apply {
            remove(USER_GOOGLE_TOKEN)
            remove(USER_TOKEN)
            putBoolean(IS_USER_LOGIN, false)
        }
    }
}