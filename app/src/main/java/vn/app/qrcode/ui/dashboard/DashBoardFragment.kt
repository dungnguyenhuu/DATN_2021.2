package vn.app.qrcode.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.databinding.Observable
import androidx.navigation.fragment.findNavController
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import com.base.common.data.result.ErrorApi
import com.base.common.utils.ext.setDebounceClickListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentDashboardBinding

class DashBoardFragment :
    BaseMVVMFragment<CommonEvent, FragmentDashboardBinding, DashBoardViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_dashboard
    override val viewModel: DashBoardViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewEvent()
    }

    private fun setupViewEvent() {

    }

}