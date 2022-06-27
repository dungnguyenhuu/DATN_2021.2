package vn.app.qrcode.activity

import android.content.Context
import com.base.common.base.fragment.BaseNavFragmentHost
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentMainBinding
import vn.app.qrcode.di.module.networkModule
import vn.app.qrcode.di.module.repositoryModule
import vn.app.qrcode.di.module.useCaseModule
import vn.app.qrcode.di.module.viewModelModule

class MainNavFragmentHost :
    BaseNavFragmentHost<MainNavEvent, FragmentMainBinding, MainNavFragmentViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_main
    override val viewModel: MainNavFragmentViewModel by viewModel()

    override fun initConfigModule(ctx: Context): List<Module> {

        val moduleWithContext = module {
            single { setupGoogleSignInService(ctx) }
            single { Firebase.auth }
        }
        return listOf(moduleWithContext, networkModule)
    }

    override fun initUseCaseModule(ctx: Context): Module = useCaseModule

    override fun initViewModelModule(ctx: Context): Module = viewModelModule
    override fun initRepositoryModule(ctx: Context): Module = repositoryModule

    private fun setupGoogleSignInService(context: Context): GoogleSignInClient {
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        return GoogleSignIn.getClient(context, gso)
    }
}