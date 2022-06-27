package com.base.common.utils

import com.base.common.data.result.ErrorApi
import com.base.common.data.result.ErrorApi2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object JsonParserUtils : KoinComponent {
    private val gson: Gson by inject()


    fun getErrorCommon(data: String): ErrorApi? {
        try {
            val type = object : TypeToken<ErrorApi>() {}.type
            val response = gson.fromJson<ErrorApi>(data, type)
            response?.mapErrorCode()
            return response
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    fun getErrorCommon2(data: String): ErrorApi2? {
        try {
            val type = object : TypeToken<ErrorApi2>() {}.type
            val response = gson.fromJson<ErrorApi2>(data, type)
            response?.mapErrorCode()
            return response
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }
}