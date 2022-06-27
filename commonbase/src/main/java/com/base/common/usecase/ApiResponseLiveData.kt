package com.base.common.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.base.common.data.result.ApiResult

class ApiResponseLiveData<T>(loading: LiveData<Boolean>, result: LiveData<ApiResult<T>>) :
    MediatorLiveData<Pair<Boolean?, ApiResult<T>?>>() {
    init {
        addSource(loading) { loadingValue -> setValue(Pair(loadingValue, result.value)) }
        addSource(result) { resultValue -> setValue(Pair(loading.value, resultValue)) }
    }
}