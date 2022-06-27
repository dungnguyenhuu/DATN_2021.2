package com.base.common.base.adapter

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.base.common.BR
import com.base.common.base.viewmodel.BaseViewModel
import org.koin.core.component.KoinComponent

abstract class BaseLifecycleAdapter<E, T, BINDING : ViewDataBinding, VM : BaseViewModel<E>>(
    itemView: View,
    var activity: FragmentActivity
) :
    BaseViewHolder<T, BINDING>(itemView), LifecycleOwner, KoinComponent {
    lateinit var viewModel: VM
    val bindingVariable = BR.viewModel

    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.markState(Lifecycle.State.INITIALIZED)
    }

    fun markAttach() {
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
    }

    fun markDetach() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onBind(item: T) {
        binding.lifecycleOwner = getLifecycleOwner()
        binding.setVariable(bindingVariable, viewModel)
        viewModel.registerToListenToEvent(getLifecycleOwner(), { event ->
            event?.let {
                onReceiveEvent(event, item)
            }
        })
    }

    open fun onReceiveEvent(event: E, item: T) {

    }

    fun getLifecycleOwner(): FragmentActivity {
        return activity
    }

}