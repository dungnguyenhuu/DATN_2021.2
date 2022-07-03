package vn.app.qrcode.di.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import vn.app.qrcode.activity.MainNavFragmentViewModel
import vn.app.qrcode.ui.category.CategoryViewModel
import vn.app.qrcode.ui.dashboard.DashBoardViewModel
import vn.app.qrcode.ui.home.HomeViewModel
import vn.app.qrcode.ui.skipauth.SkipAuthViewModel
import vn.app.qrcode.ui.splash.SplashViewModel

val viewModelModule = module {
    viewModel { MainNavFragmentViewModel() }
    viewModel { SplashViewModel() }
    viewModel { HomeViewModel(get()) }
    viewModel { DashBoardViewModel() }
    viewModel { SkipAuthViewModel() }
    viewModel { CategoryViewModel(get()) }
}