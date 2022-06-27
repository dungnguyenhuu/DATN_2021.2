package com.base.common.data.result

import com.base.common.base.application.CommonApplication
import com.base.common.R
import com.base.common.utils.JsonParserUtils
import retrofit2.HttpException
import java.net.UnknownHostException

class ApiResult<T> private constructor(private val result: Any?) {
    // discovery

    val isFailure: Boolean get() = result is Failure
    val isSuccess: Boolean get() = result !is Failure && !isLoading
    var isLoading: Boolean = false

    // value retrieval

    fun get(): T =
        if (result is Failure) throw result.exception
        else result as T

    fun getOrNull(): T? =
        if (result is Failure) null
        else result as T

    inline fun getOrElse(default: () -> T): T =
        if (isFailure) default()
        else value

    // exception retrieval

    fun exceptionOrNull(): Throwable? =
        if (result is Failure) result.exception
        else null

    fun errorApiOrNull(needCheckToken: Boolean = true): ErrorApi? {
        if (!isFailure) return null
        if (exception is HttpException) run {
            try {
                (exception as HttpException).also { error ->
                    run {
                        if (error.code() == 401 && needCheckToken) {
                            return ErrorApi(
                                CommonApplication.instance!!.getString(R.string.session_timeout),
                                "401"
                            )
                        }
                        val errorApi2 =
                            error.response()?.errorBody()?.string()?.let { JsonParserUtils.getErrorCommon2(it) }
//                        if (error.code() == 401) {
                        if (errorApi2 != null) {
                            if (errorApi2.message == "Access Token is require.") {
                                return ErrorApi(
                                    CommonApplication.instance!!.getString(R.string.session_timeout),
                                    "401"
                                )
                            }
                            return ErrorApi(errorApi2.message, errorApi2.errorCode)
                        }
                    }
                }

            } catch (e: Exception) {
                return ErrorApi(CommonApplication.instance!!.getString(R.string.common_error), "E9999")
            }
        }
        if (exception is UnknownHostException) run {
            return ErrorApi("Mất kết nối", "E9999")
        }
        return ErrorApi(CommonApplication.instance!!.getString(R.string.common_error), "E9999")
    }

    // companion with constructors

    companion object {
        fun <T> success(value: T): ApiResult<T> = ApiResult<T>(value).apply { isLoading = false }
        fun <T> failure(exception: Throwable): ApiResult<T> =
            ApiResult<T>(Failure(exception)).apply { isLoading = false }

        fun <T> success(): ApiResult<T> = ApiResult<T>("").apply { isLoading = false }
        fun <T> loading(): ApiResult<T> = ApiResult<T>(null).apply { isLoading = true }
    }

    // internal API for inline functions

    @PublishedApi
    internal val exception: Throwable
        get() = (result as Failure).exception
    @PublishedApi
    internal val value: T
        get() = result as T

    private class Failure(@JvmField val exception: Throwable)
}

inline fun <T> resultOf(block: () -> T): ApiResult<T> =
    try {
        ApiResult.success(block())
    } catch (e: Throwable) {
        ApiResult.failure(e)
    }

// -- extensions ---

// transformation

inline fun <U, T> ApiResult<T>.map(block: (T) -> U): ApiResult<U> =
    if (isFailure) this as ApiResult<U>
    else resultOf { block(value) }

inline fun <U, T : U> ApiResult<T>.handle(block: (Throwable) -> U): ApiResult<U> =
    if (isFailure) resultOf { block(exception) }
    else this as ApiResult<U>

// "peek" onto value/exception and pipe

inline fun <T> ApiResult<T>.onFailure(block: (Throwable) -> Unit): ApiResult<T> {
    if (isFailure) block(exception)
    return this
}

inline fun <T> ApiResult<T>.onSuccess(block: (T) -> Unit): ApiResult<T> {
    if (isSuccess) block(value)
    return this
}
