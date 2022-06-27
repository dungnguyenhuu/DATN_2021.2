package com.base.common.usecase

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.base.common.data.result.ApiResult
import com.base.common.utils.rx.SchedulerProvider
import com.base.common.utils.rx.with
import io.reactivex.Completable
import io.reactivex.Observable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class CompleteUseCase<T> protected constructor() : KoinComponent {
    private val schedulers: SchedulerProvider by inject()
    var result = MutableLiveData<ApiResult<T>>()
    var loadingStatus = MutableLiveData<Boolean>()
    var apiResponseLiveData: ApiResponseLiveData<T> = ApiResponseLiveData(loadingStatus, result)
    /**
     * Builds an [Observable] which will be used when executing the current [CompleteUseCase].
     */
    protected abstract fun buildUseCaseObservable(): Completable

    @SuppressLint("CheckResult")
    fun execute() {
        this.buildUseCaseObservable()
            .with(schedulers)
            .doOnSubscribe { loadingStatus.postValue(true)}
            .doFinally { loadingStatus.postValue(false) }
            .subscribe {
                result.postValue(ApiResult.success())
            }

    }

}