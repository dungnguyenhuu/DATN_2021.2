package com.base.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * A collection of [BindingAdapter]s for different UI-related tasks.
 *
 * In Kotlin you can write the Binding Adapters in the traditional way:
 *
 * ```
 * @BindingAdapter("property")
 * @JvmStatic fun propertyMethod(view: ViewClass, parameter1: Param1, parameter2: Param2...)
 * ```
 *
 * Or using extension functions:
 *
 * ```
 * @BindingAdapter("property")
 * @JvmStatic fun ViewClass.propertyMethod(parameter1: Param1, parameter2: Param2...)
 * ```
 *
 * See [EditText.clearTextOnFocus].
 *
 * Also, keep in mind that @JvmStatic is only necessary if you define the methods inside a class or
 * object. Consider moving the Binding Adapters to the top level of the file.
 */
object BindingAdapters {


    /**
     * Hides keyboard when the [EditText] is focused.
     *
     * Note that there can only be one [TextView.OnEditorActionListener] on each [EditText] and
     * this [BindingAdapter] sets it.
     */
    @BindingAdapter("hideKeyboardOnInputDone")
    @JvmStatic
    fun hideKeyboardOnInputDone(view: EditText, enabled: Boolean) {
        if (!enabled) return
        val listener = TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                view.clearFocus()
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            false
        }
        view.setOnEditorActionListener(listener)
    }

    /*
     * Instead of having if-else statements in the XML layout, you can create your own binding
     * adapters, making the layout easier to read.
     *
     * Instead of
     *
     * `android:visibility="@{viewmodel.isStopped ? View.INVISIBLE : View.VISIBLE}"`
     *
     * you use:
     *
     * `android:invisibleUnless="@{viewmodel.isStopped}"`
     *
     */

    /**
     * Makes the View [View.INVISIBLE] unless the condition is met.
     */
    @Suppress("unused")
    @BindingAdapter("invisibleUnless")
    @JvmStatic
    fun invisibleUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Makes the View [View.GONE] unless the condition is met.
     */
    @BindingAdapter("goneUnless")
    @JvmStatic
    fun goneUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * In [ProgressBar], [ProgressBar.setMax] must be called before [ProgressBar.setProgress].
     * By grouping both attributes in a BindingAdapter we can make sure the order is met.
     *
     * Also, this showcases how to deal with multiple API levels.
     */
    @BindingAdapter(value = ["android:max", "android:progress"], requireAll = true)
    @JvmStatic
    fun updateProgress(progressBar: ProgressBar, max: Int, progress: Int) {
        progressBar.max = max
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(progress, false)
        } else {
            progressBar.progress = progress
        }
    }

    @BindingAdapter("loseFocusWhen")
    @JvmStatic
    fun loseFocusWhen(view: EditText, condition: Boolean) {
        if (condition) view.clearFocus()
    }

    @BindingAdapter("setUnderLine")
    @JvmStatic
    fun setUnderLine(view: TextView, content: String?) {
        if (content.isNullOrBlank()) return
        view.paintFlags = view.paintFlags or UNDERLINE_TEXT_FLAG
        view.text = content
    }

    @BindingAdapter("formatFilterText")
    @JvmStatic
    fun formatFilterText(view: TextView, content: String?) {
        if (content.isNullOrBlank()) return
        view.text = "6:00 $content"
    }

    @BindingAdapter("moneyFormat")
    @JvmStatic
    fun moneyFormat(view: TextView, content: String?) {
        if (content.isNullOrBlank()) {
            view.text = "0 ⭐️"
            return
        }
        var dou: Int
        try {
            dou = content.toInt()
        } catch (e: Exception) {
            dou = 0
        }
        val formatter = NumberFormat.getInstance()
//        formatter.applyPattern("#0.##")
        view.text = String.format("%s ⭐️", formatter.format(dou).replace(',', '.'))
    }

    @SuppressLint("SetTextI18n")
    @BindingAdapter(value = ["moneyFormatText", "addText"], requireAll = true)
    @JvmStatic
    fun addTextMoneyFormat(view: TextView, moneyFormatText: String?, addText: String?) {
        view.text = bindMoneyFormat(moneyFormatText, addText)
    }


    fun bindMoneyFormat(moneyFormatText: String?, addText: String?) : String{
        if (moneyFormatText.isNullOrBlank()) {
            return "$addText 0 ⭐"
        }
        var dou: Int
        try {
            dou = moneyFormatText.toInt()
        } catch (e: Exception) {
            dou = 0
        }
        val formatter = NumberFormat.getInstance()
        return  String.format("$addText %s ⭐️", formatter.format(dou).replace(',', '.'))
    }

    @BindingAdapter("timeFormat")
    @JvmStatic
    fun timeFormat(view: TextView, content: String?) {
        if (content.isNullOrBlank()) return
        view.text = milliSecondToDate(content, "HH:mm dd/MM/yyy")
    }

    fun milliSecondToDate(second: String, format: String): String? {
        return try {
            val cal = Calendar.getInstance()
            cal.timeInMillis = second.toLong() * 1000
            SimpleDateFormat(format, Locale.getDefault()).format(cal.time)
        } catch (e: Exception) {
            ""
        }
    }
}