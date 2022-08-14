package vn.app.qrcode.ui.setting

import android.os.Bundle
import android.view.View
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentSettingBinding

class SettingFragment : BaseMVVMFragment<CommonEvent, FragmentSettingBinding, SettingViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_setting
    override val viewModel: SettingViewModel by viewModel()
    companion object {
        fun newInstance() = SettingFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}