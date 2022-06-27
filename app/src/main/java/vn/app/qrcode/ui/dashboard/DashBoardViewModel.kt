package vn.app.qrcode.ui.dashboard

import android.app.Activity
import com.base.common.base.viewmodel.BaseViewModel
import com.base.common.base.viewmodel.CommonEvent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import timber.log.Timber
import vn.app.qrcode.data.usermanager.UserManager

class DashBoardViewModel(
    private val googleSignInClient: GoogleSignInClient,
    private val userManager: UserManager
) :
    BaseViewModel<CommonEvent>() {
    fun processLogout(activity: Activity) {
        googleSignInClient.signOut().addOnCompleteListener(activity) { task ->
            task.run {
                Timber.d("$isCanceled -- $isComplete -- $isSuccessful -- $result")
            }
            if (task.isSuccessful) {
                userManager.logout()
                sendEvent(CommonEvent.Success)
            }
        }
    }
}