package vn.app.qrcode.base

import androidx.databinding.ViewDataBinding
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.BaseViewModel
import com.base.common.data.event.MessageDialog
import org.koin.android.ext.android.inject
import vn.app.qrcode.data.usermanager.UserManager

abstract class BaseAppFragment<E, T : ViewDataBinding, V : BaseViewModel<E>> :
    BaseMVVMFragment<E, T, V>() {
    private val userManager: UserManager by inject()

    override fun onSessionTimeOut(message: String) {
        if (userManager.isUserLoggedIn()) {
            userManager.setIsUserLogin(false)
            userManager.saveUserToken("")
            //startActivity<LoginActivity>()
            requireActivity().finish()
        }
    }

    override fun showDialogMessage(message: MessageDialog) {
        super.showDialogMessage(message)
    }

}