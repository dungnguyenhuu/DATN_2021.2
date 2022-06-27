package vn.app.qrcode.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.findNavController
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentSplashBinding
import vn.app.qrcode.utils.sharepref.SharedPreUtils
class SplashFragment : BaseMVVMFragment<CommonEvent, FragmentSplashBinding, SplashViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_splash
    override val viewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreUtils.putBoolean("APP_START", true)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_splash_to_login)
        }, 2000)
    }

}
