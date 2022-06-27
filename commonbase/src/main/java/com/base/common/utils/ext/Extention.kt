package com.base.common.utils.ext

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import org.jetbrains.anko.collections.forEachByIndex
import org.jetbrains.anko.collections.forEachWithIndex
import java.util.*
import kotlin.collections.HashMap

fun ViewGroup.inflateExt(layoutId: Int) =
    LayoutInflater.from(context).inflate(layoutId, this, false)

fun ArrayList<HashMap<String, String>?>.toHashMap(): HashMap<String, String> {
    val map = HashMap<String, String>()
    this.forEachWithIndex { index, hashMap ->
        map[index.toString()] = hashMap?.values?.first() ?: ""
    }
    return map
}

fun HashMap<String, HashMap<String, String>>.toHashMap(): HashMap<String, String> {
    val map = HashMap<String, String>()
    this.forEach {
        map[it.key] = it.value.values.first()
    }
    return map
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.setVisibility(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun TextView.setDrawableStart(@DrawableRes start: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) setCompoundDrawablesRelativeWithIntrinsicBounds(
        start,
        0,
        0,
        0
    )
}

fun TextView.setDrawableTop(@DrawableRes top: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) setCompoundDrawablesRelativeWithIntrinsicBounds(
        0,
        top,
        0,
        0
    )
}

fun TextView.setDrawableEnd(@DrawableRes end: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) setCompoundDrawablesRelativeWithIntrinsicBounds(
        0,
        0,
        end,
        0
    )
}

fun TextView.setDrawableBottom(@DrawableRes bottom: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) setCompoundDrawablesRelativeWithIntrinsicBounds(
        0,
        0,
        0,
        bottom
    )
}

fun View.setBackgroundColorz(@ColorRes resId: Int) =
    setBackgroundColor(ContextCompat.getColor(context, resId))

fun TextView.setTextColorz(@ColorRes resId: Int) =
    setTextColor(ContextCompat.getColor(context, resId))

fun EditText.onTextChanged(text: (String?) -> Unit) = addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        text(s.toString())
    }
})

val Fragment.requireArguments: Bundle
    get() = arguments ?: throw Exception("No arguments found!")

fun ViewPager.onPageSelected(position: (Int?) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(p0: Int) {
        }

        override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
        }

        override fun onPageSelected(position: Int) {
            position(position)
        }
    })
}

fun String.capitalizedWord(): String {
    val words = replace('_', ' ').toLowerCase(Locale.getDefault())
        .split(" ".toRegex())
        .dropLastWhile { it.isEmpty() }
        .toTypedArray()
    var aString = ""
    for (word in words) {
        aString = aString + word.substring(0, 1)
            .toUpperCase(Locale.getDefault()) + word.substring(1) + " "
    }
    return aString
}

fun View.setDebounceClickListener(listener: View.OnClickListener) {
    setOnClickListener(object : View.OnClickListener {
        var currentClickTime = 0L
        override fun onClick(p0: View?) {
            if (currentClickTime == 0L || System.currentTimeMillis() - currentClickTime > 500) {
                currentClickTime = System.currentTimeMillis()
                listener.onClick(p0)
            }
        }

    })
}