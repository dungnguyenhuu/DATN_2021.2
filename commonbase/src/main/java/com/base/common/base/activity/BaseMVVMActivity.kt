package com.base.common.base.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.base.common.BR
import com.base.common.base.viewmodel.BaseViewModel
import com.base.common.constant.AppConstant
import com.base.common.constant.AppConstant.CALL_ERROR
import com.base.common.data.event.MessageDialog
import com.base.common.utils.rx.bus.RxBus
import com.base.common.utils.rx.bus.RxEvent
import com.kaopiz.kprogresshud.KProgressHUD
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


abstract class BaseMVVMActivity<E, T : ViewDataBinding, V : BaseViewModel<E>> :
    AppCompatActivity() {
    var viewDataBinding: T? = null
        private set

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
    protected var isNeedLoading = true

    private var compositeDisposable = CompositeDisposable()


    val loadingDialog: KProgressHUD by lazy {
        KProgressHUD(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adjustFontScale(resources.configuration)
        performDataBinding()
    }

    open fun adjustFontScale(configuration: Configuration) {
        configuration.fontScale = 1.0.toFloat()
        val metrics = resources.displayMetrics
        val wm =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        baseContext.resources.updateConfiguration(configuration, metrics)
    }

    private fun performDataBinding() {
        viewModel.loadingStatus.observe(this, Observer {
            Timber.d(it.toString())
            if (!isNeedLoading) return@Observer
            if (it) {
                showDialogLoading()
            } else {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        })
        viewModel.registerToListenToEvent(this, { event ->
            run {
                event?.let {
                    onReceiverMessage(viewModel, event)
                }
            }
        })

        initDispose()
        if (layoutId == 0) return
        viewDataBinding = DataBindingUtil.setContentView(this, layoutId)
        viewDataBinding!!.executePendingBindings()
        viewDataBinding!!.lifecycleOwner = this
        viewDataBinding!!.setVariable(bindingVariable, viewModel)

        setUpObserver()
    }

    private fun initDispose() {
        compositeDisposable.add(RxBus.listen(RxEvent.EventCloseDialog::class.java).subscribe {
            handleEventCloseDialog(it)
        })
        compositeDisposable.add(RxBus.listen(RxEvent.EventDialog::class.java).subscribe {
            handleEventDialog(it)
        })
    }

    open fun handleEventDialog(event: RxEvent.EventDialog?) {}

    open fun <T> addDisposable(classType: Class<T>, handler: (T) -> Unit) {
        compositeDisposable.add(RxBus.listen(classType).subscribe({
            handler(it)
        }, {
            Timber.e(it)
        }))
    }

    protected open fun setUpObserver() {
        viewModel.errorResponse.observe(this, Observer {
            it?.let { errorApi ->
                when (errorApi.errorCode) {
                    AppConstant.SESSION_TIMEOUT -> onSessionTimeOut(errorApi.message)
                    AppConstant.UPDATE_APP -> showDialogMessage(
                        MessageDialog(
                            "Thông báo",
                            errorApi.message
                        ).apply { tag = "UPDATE" })
                    CALL_ERROR -> {
                        showDialogMessage(
                            MessageDialog(
                                "Thông báo",
                                errorApi.message,
                                datas = errorApi.datas
                            ).apply { tag = CALL_ERROR })
                    }
                    else -> showDialogMessage(
                        MessageDialog(
                            "Thông báo",
                            errorApi.message,
                            tag = errorApi.errorCode
                        )
                    )
                }
            }
        })
    }

    var active = false

    public override fun onStart() {
        super.onStart()
        active = true
    }

    public override fun onStop() {
        super.onStop()
        active = false
    }

    open fun handleEventCloseDialog(event: RxEvent.EventCloseDialog) {}

    open fun onSessionTimeOut(message: String) {}

    open fun showDialogMessage(message: MessageDialog) {}

    open fun showDialog2ButtonMessage(message: MessageDialog) {}

    open fun showDialogEditMessage(message: MessageDialog) {}

    open fun showDialogMultiEditMessage(message: MessageDialog) {}

    open fun showDialogLoading() {
        if (isFinishing || !active) return
        try {
            loadingDialog.show()
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    open fun onReceiverMessage(sender: Observable?, event: E) {}

}



