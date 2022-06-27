package com.base.common.base.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.base.common.BR
import com.base.common.base.viewmodel.BaseViewModel

abstract class BaseDialog<E, T : ViewDataBinding, V : BaseViewModel<E>> : DialogFragment() {

    protected var mRootView: View? = null
    protected var viewDataBinding: T? = null

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

    abstract fun updateUI(savedInstanceState: Bundle?)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = RelativeLayout(activity)
        root.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)

        dialog.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        mRootView = viewDataBinding!!.root
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding!!.executePendingBindings()
        viewModel.registerToListenToEvent(viewLifecycleOwner, { event ->
            run {
                event?.let {
                    onReceiverMessage(viewModel, event)
                }
            }
        })
        viewDataBinding!!.lifecycleOwner = this
        viewDataBinding!!.setVariable(bindingVariable, viewModel)
        updateUI(savedInstanceState)
    }

    @Throws()
    open fun dismissDialog(fragManager: FragmentManager, tag: String) {
        val frag: Fragment? = fragManager.findFragmentByTag(tag)
        frag?.let {
            fragManager.beginTransaction()
                .disallowAddToBackStack()
                .remove(it)
                .commitAllowingStateLoss()
        }
    }

    abstract fun onReceiverMessage(sender: Observable?, event: E)

    @Throws()
    open fun dismissDialog(fragManager: FragmentManager, tag: String, aniIn: Int, aniOut: Int) {
        val frag: Fragment? = fragManager.findFragmentByTag(tag)
        frag?.let {
            fragManager.beginTransaction()
                .disallowAddToBackStack()
                .setCustomAnimations(aniIn, aniOut)
                .remove(it)
                .commitAllowingStateLoss()
        }
    }

}