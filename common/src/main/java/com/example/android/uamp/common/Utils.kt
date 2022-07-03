package com.example.android.uamp.common

import com.example.android.uamp.media.model.PrefixRoot

object Utils {
    fun generalMediaItemId(prefix: PrefixRoot, s: String): String {
        return "${prefix.name}__$s"
    }
}