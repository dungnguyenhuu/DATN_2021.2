package vn.app.qrcode.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.base.common.base.activity.BaseMVVMActivity
import com.base.common.base.dialog.BaseDialog
import com.base.common.base.viewmodel.BaseViewModel
import com.base.common.constant.AppConstant.SUPPORT_NUMBER
import com.base.common.data.event.MessageDialog
import com.base.common.utils.network.NetworkStateObservable
import org.jetbrains.anko.startActivity
import org.koin.android.ext.android.inject
import timber.log.Timber
import vn.app.qrcode.R
import vn.app.qrcode.activity.MainActivity
import vn.app.qrcode.data.usermanager.UserManager

abstract class BaseAppActivity<E, T : ViewDataBinding, V : BaseViewModel<E>> :
    BaseMVVMActivity<E, T, V>() {
    protected val networkObserver: NetworkStateObservable by lazy { NetworkStateObservable(this) }
    private val userManager: UserManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToListenToNetworkState()
        Timber.e("onCreate" + this.localClassName)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.e("onDestroy" + this.localClassName)
    }

    private fun handleNetworkState(isConnected: Boolean?) {
        if (isConnected == null) return
        if (!isConnected) {
            forceDismissDialog()
            showDialogMessage(
                MessageDialog(
                    getString(R.string.app_name),
                    getString(R.string.lost_connection)
                ).apply {
                    tag = "NETWORK"
                })
        } else {
            forceDismissDialog()
        }
    }

    private fun setupToListenToNetworkState() {
        networkObserver.observe(this, Observer {
            handleNetworkState(it)
        })
        lifecycle.addObserver(networkObserver)
    }

    private fun forceDismissDialog() {
        supportFragmentManager.fragments.forEach {
            if (it is BaseDialog<*, *, *>) {
                it.dismiss()
            }
        }
    }

    override fun onSessionTimeOut(message: String) {
        if (userManager.isUserLoggedIn()) {
            userManager.setIsUserLogin(false)
            userManager.saveUserToken("")
        }
        Toast.makeText(
            this,
            "Phiên đăng nhập hết hạn! Vui lòng đăng nhập lại vào hệ thống!",
            Toast.LENGTH_LONG
        ).show()
        startActivity<MainActivity>()
        finish()
    }

    fun makeCall() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$SUPPORT_NUMBER")
        startActivity(intent)
    }

}