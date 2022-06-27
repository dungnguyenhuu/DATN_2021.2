package com.base.common.base.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.base.common.base.viewmodel.BaseViewModel
import com.kaopiz.kprogresshud.KProgressHUD
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module

abstract class BaseNavFragmentHost<E, T : ViewDataBinding, V : BaseViewModel<E>> :
    BaseMVVMFragment<E, T, V>() {

    var loadingDialog: KProgressHUD? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.let { ctx ->
            val koinModules = initModule(ctx)
            if (koinModules.isNotEmpty())
                loadKoinModules(koinModules)
        }
        super.onViewCreated(view, savedInstanceState)
        iniLoadingDialog()
    }

    private fun iniLoadingDialog() {
        context?.let {
            loadingDialog = KProgressHUD(it)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
        }
    }

    private fun initModule(ctx: Context): List<Module> {
        val preLoadModules = arrayListOf<Module>()
        initConfigModule(ctx)?.let { module ->
            if (module.isNotEmpty())
                preLoadModules.addAll(module)
        }
        initRepositoryModule(ctx)?.let { module ->
            preLoadModules.add(module)
        }
        initUseCaseModule(ctx)?.let { module ->
            preLoadModules.add(module)
        }
        initViewModelModule(ctx)?.let { module ->
            preLoadModules.add(module)
        }

        return preLoadModules
    }

    abstract fun initConfigModule(ctx: Context): List<Module>?
    abstract fun initRepositoryModule(ctx: Context): Module?
    abstract fun initUseCaseModule(ctx: Context): Module?
    abstract fun initViewModelModule(ctx: Context): Module?

    override fun onDestroyView() {
        context?.let { ctx ->
            val loadedModule = initModule(ctx)
            if (loadedModule.isNotEmpty())
                unloadKoinModules(loadedModule)
        }
        super.onDestroyView()
    }

    fun showDialog(isShow: Boolean) {
        if (isDetached)
            return
        if (isShow && loadingDialog != null && !loadingDialog!!.isShowing)
            loadingDialog?.show()

        if (!isShow && loadingDialog?.isShowing == true)
            loadingDialog?.dismiss()
    }

    fun handleBackPress():Boolean {
        return findNavController().popBackStack()
    }
}