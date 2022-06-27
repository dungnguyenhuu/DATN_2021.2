package vn.app.qrcode.ui.skipauth

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import com.base.common.utils.ext.setDebounceClickListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentSkipAuthBinding

class SkipAuthFragment :
    BaseMVVMFragment<CommonEvent, FragmentSkipAuthBinding, SkipAuthViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_skip_auth
    override val viewModel: SkipAuthViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewEvent()
    }

    private fun setupViewEvent() {
        viewDataBinding.apply {
            btnBack.setDebounceClickListener {
                findNavController().popBackStack()
            }
        }
    }
}