package com.base.common.usecase

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.base.common.data.result.ApiResult
import com.base.common.utils.rx.SchedulerProvider
import com.base.common.utils.rx.with
import io.reactivex.Observable
import io.reactivex.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.reactivestreams.Subscriber

/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This interface represents a execution unit for different use cases (this means any use case
 * in the application should implement this contract).
 * By convention each UseCase implementation will return the result using a [Subscriber]
 * that will execute its job in a background thread and will post the result in the UI thread.
 */
abstract class UseCase<T>(private val schedulers: SchedulerProvider) {
    var result = MutableLiveData<ApiResult<T>>()

    /**
     * Builds an [Observable] which will be used when executing the current [UseCase].
     */
    protected abstract fun buildUseCaseObservable(): Single<T>

    @SuppressLint("CheckResult")
    fun execute() {
        this.buildUseCaseObservable()
            .with(schedulers)
            .doOnSubscribe { result.postValue(ApiResult.loading()) }
            .subscribe({ response ->
                result.postValue(ApiResult.success(response))
            }, { error ->
                result.postValue(ApiResult.failure(error))
            })
    }

    @SuppressLint("CheckResult")
    fun executeZip(doNext: (T) -> Unit, doError: () -> Unit) {
        this.buildUseCaseObservable()
            .with(schedulers)
            .doOnSubscribe { result.postValue(ApiResult.loading()) }
            .subscribe({ response ->
                result.postValue(ApiResult.success(response))
                doNext.invoke(response)
            }, { error ->
                result.postValue(ApiResult.failure(error))
                doError.invoke()
            })
    }


}