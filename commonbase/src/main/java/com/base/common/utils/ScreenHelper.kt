package com.base.common.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.base.common.base.application.CommonApplication

object ScreenHelper {

    private var screenHeightInPx = -1
    private var screenWidthInPx = -1

    fun getScreenHeightInPx(): Int {
        if (screenHeightInPx < 0) {
            init()
        }
        return screenHeightInPx
    }

    fun getScreenWidthInPx(): Int {
        if (screenWidthInPx < 0) {
            init()
        }
        return screenWidthInPx
    }

    fun reset() {
        screenHeightInPx = -1
        screenWidthInPx = -1
    }

    private fun init() {
        val wm = CommonApplication.instance?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        if (wm != null) {
            val display = wm.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            screenHeightInPx = metrics.heightPixels
            screenWidthInPx = metrics.widthPixels
        }
    }

    fun dpToPx(dp: Int): Int {
        return (dp * CommonApplication.instance?.resources?.displayMetrics?.density!! + 0.5).toInt()
    }

}
