package vn.app.qrcode.ui.login

import androidx.fragment.app.Fragment
import com.base.common.base.viewmodel.BaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import vn.app.qrcode.data.usermanager.UserManager

class LoginViewModel(
    private val googleSignInClient: GoogleSignInClient,
    private val userManager: UserManager
) : BaseViewModel<LoginEvent>() {

    companion object {
        const val REQUEST_LOGIN_CODE = 1001
        const val TAG = "LoginViewModel"
    }

    fun requestGoogleLogin(fragment: Fragment) {
        loadingStatus.postValue(true)
        fragment.startActivityForResult(googleSignInClient.signInIntent, REQUEST_LOGIN_CODE)
    }

    fun saveUserCredential(account: GoogleSignInAccount) {
        // call api to sync google token with server token
        userManager.saveUserGoogleToken(account.id!!)
        userManager.setIsUserLogin(true)
        sendEvent(LoginEvent.LoginSuccess)
    }

    fun revokeAccessIfNeeded() {
        googleSignInClient.signOut().addOnCompleteListener {
            userManager.logout()
        }
    }
}

sealed class LoginEvent {
    object LoginSuccess : LoginEvent()
}
