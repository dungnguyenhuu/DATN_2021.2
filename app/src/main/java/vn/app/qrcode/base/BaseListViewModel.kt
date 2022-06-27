package vn.app.qrcode.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.base.common.base.viewmodel.BaseViewModel
import com.base.common.data.result.ApiResult
import vn.app.qrcode.data.constant.DEFAULT_PAGE

abstract class BaseListViewModel<E> : BaseViewModel<E>() {
    var page = DEFAULT_PAGE
    var stateLoading = LoadingState.INIT
    var isRefreshing = MutableLiveData<Boolean>()
    var isEmpty = MutableLiveData<Boolean>()

    init {
        isEmpty.value = true
    }

    override fun observerData(lifecycleOwner: LifecycleOwner) {
        super.observerData(lifecycleOwner)
        loadingStatus.observe(lifecycleOwner, Observer {
            it?.let { loading ->
                isRefreshing.value = stateLoading == LoadingState.REFRESH && loading
            }
        })
    }

    fun <T> handleStateCommonApi(result: ApiResult<T>?, needCheckToken: Boolean = true): T? {
        result?.let {
            when (stateLoading) {
                LoadingState.INIT -> loadingStatus.value = result.isLoading
                LoadingState.REFRESH -> isRefreshing.value = result.isLoading
                else -> {}
            }
            if (result.isSuccess) {
                return result.getOrNull()
            }
            if (result.isFailure) {
                errorResponse.value = result.errorApiOrNull(needCheckToken)
            }
        }
        return null
    }

    open fun refresh() {
        stateLoading = LoadingState.REFRESH
        page = DEFAULT_PAGE
        loadData()
    }

    open fun reset() {
        stateLoading = LoadingState.INIT
        page = DEFAULT_PAGE
        loadData()
    }

    open fun loadMore() {
        stateLoading = LoadingState.LOADMORE
        loadData()
    }

    open fun loadData() {

    }
}

enum class LoadingState {
    INIT, REFRESH, LOADMORE
}