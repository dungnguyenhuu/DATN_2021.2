package vn.app.qrcode.activity

import android.content.Context
import com.base.common.base.fragment.BaseNavFragmentHost
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.module.Module
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentMainBinding
import vn.app.qrcode.di.module.viewModelModule

class MainNavFragmentHost :
    BaseNavFragmentHost<MainNavEvent, FragmentMainBinding, MainNavFragmentViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_main
    override val viewModel: MainNavFragmentViewModel by viewModel()

    override fun initViewModelModule(ctx: Context): Module = viewModelModule
}