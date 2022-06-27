package com.base.common.base.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.base.common.BR
import com.base.common.R
import com.base.common.base.activity.BaseMVVMActivity
import com.base.common.base.viewmodel.BaseViewModel
import com.base.common.constant.AppConstant
import com.base.common.data.event.MessageDialog
import com.base.common.utils.rx.bus.RxBus
import com.base.common.utils.rx.bus.RxEvent
import com.kaopiz.kprogresshud.KProgressHUD
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


abstract class BaseMVVMFragment<E, T : ViewDataBinding, V : BaseViewModel<E>> : Fragment() {

    protected var mRootView: View? = null
    lateinit var viewDataBinding: T
    protected var isNeedLoading = true

    /**
     * @return layout resource id
     */
    @get:LayoutRes
    abstract val layoutId: Int

    abstract val viewModel: V

//    /**
//     * Override for set view model type
//     *
//     * @return view model type for get instance
//     */
//    abstract val viewModelType: Class<V>

    /**
     * @return layout binding variable
     */
    private val bindingVariable = BR.viewModel
    private var compositeDisposable = CompositeDisposable()

    open fun onReceiverMessage(sender: Observable?, event: E) {}

    override fun onCreateView(
        @NonNull inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adjustFontScale(resources.configuration)
        viewDataBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        mRootView = viewDataBinding.root
        return mRootView
    }

    open fun adjustFontScale(configuration: Configuration) {
        configuration.fontScale = 1.0.toFloat()
        val metrics = resources.displayMetrics
        val wm =
            activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        activity?.resources?.updateConfiguration(configuration, metrics)
    }

    override fun onViewCreated(@NonNull view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding.executePendingBindings()
        observerData()
        initDispose()
        viewDataBinding.lifecycleOwner = this
        viewDataBinding.setVariable(bindingVariable, viewModel)
    }

    private fun initDispose() {
        compositeDisposable.add(RxBus.listen(RxEvent.EventCloseDialog::class.java).subscribe {
            handleEventCloseDialog(it)
        })

        compositeDisposable.add(RxBus.listen(RxEvent.EventDialog::class.java).subscribe {
            handleEventDialog(it)
        })
    }

    open fun <T> addDisposable(classType: Class<T>, handler: (T) -> Unit) {
        compositeDisposable.add(RxBus.listen(classType).subscribe({

            handler(it)
        }, {
            Timber.e(it)
        }))
    }

    open fun handleEventCloseDialog(event: RxEvent.EventCloseDialog) {}

    open fun handleEventDialog(event: RxEvent.EventDialog?) {}


    open fun observerData() {
        viewModel.registerToListenToEvent(viewLifecycleOwner, { event ->
            event?.let {
                onReceiverMessage(viewModel, it)
            }
        })
        viewModel.loadingStatus.observe(viewLifecycleOwner, { loading ->
            run {
                loadingHandler()
                findNavHostFragment()?.let { host ->
                    if (loading == null || activity == null || !isNeedLoading || isDetached) return@run
                    host.showDialog(loading)
                }
            }
        })

        viewModel.errorResponse.observe(viewLifecycleOwner, {
            it?.let { errorApi ->
                if (activity is BaseMVVMActivity<*, *, *>) {
                    when (errorApi.errorCode) {
                        AppConstant.SESSION_TIMEOUT -> (activity as BaseMVVMActivity<*, *, *>).onSessionTimeOut(
                            errorApi.message
                        )
                        else -> (activity as BaseMVVMActivity<*, *, *>).showDialogMessage(
                            MessageDialog(
                                getString(R.string.error),
                                errorApi.message,
                                tag = errorApi.errorCode
                            )
                        )
                    }
                }
            }
        })
    }

    open fun loadingHandler() {

    }

    open fun showDialogMessage(message: MessageDialog) {}


    open fun onSessionTimeOut(message: String) {

    }

    protected fun Fragment.findNavHostFragment(): BaseNavFragmentHost<*, *, *>? {
        return if (parentFragment != null && parentFragment is BaseNavFragmentHost<*, *, *>)
            parentFragment as BaseNavFragmentHost<*, *, *>
        else parentFragment?.findNavHostFragment()
    }

}