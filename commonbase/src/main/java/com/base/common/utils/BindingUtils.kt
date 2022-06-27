package com.base.common.utils

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.databinding.BindingAdapter

/**
 * Created by Kaz on 09:14 2018-12-21
 */
object BindingUtils {

    interface OnKeyPressedListener {
        fun onKeyPressed()
    }

    @JvmStatic
    @BindingAdapter("onKeyDone")
    fun onKeyDone(editText: EditText, action: OnKeyPressedListener) {
        editText.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                action.onKeyPressed()
            }
            false
        }
    }

}