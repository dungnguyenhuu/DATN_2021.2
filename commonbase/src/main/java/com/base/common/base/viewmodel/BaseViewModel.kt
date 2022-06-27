package com.base.common.base.viewmodel

import androidx.lifecycle.*
import com.base.common.data.event.Event
import com.base.common.data.result.ApiResult
import com.base.common.data.result.ErrorApi
import com.base.common.utils.rx.bus.RxBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class BaseViewModel<E> : ObservableViewModel() {
    val loadingStatus: MutableLiveData<Boolean> = MutableLiveData()
    val errorResponse: MutableLiveData<ErrorApi?> = MutableLiveData()
    private val _eventNavigator = MutableStateFlow<E?>(null)
    val eventNavigator = _eventNavigator

    fun <T> handleCommonApi(result: ApiResult<T>?, needCheckToken: Boolean = true): T? {
        result?.let {
            loadingStatus.value = result.isLoading
            if (result.isSuccess) {
                return result.getOrNull()
            }
            if (result.isFailure) {
                errorResponse.value = result.errorApiOrNull(needCheckToken)
            }
        }
        return null
    }

    fun registerToListenToEvent(
        lifecycleOwner: LifecycleOwner,
        onEventReceived: (E?) -> Unit
    ) {
        lifecycleOwner.lifecycleScope.launchWhenCreated {
            eventNavigator.collect { event ->
                onEventReceived.invoke(event)
            }
        }

        observerData(lifecycleOwner)
    }

    fun sendEvent(event: E) {
        viewModelScope.launch {
            _eventNavigator.emit(event)
        }
    }

    open fun observerData(lifecycleOwner: LifecycleOwner) {
    }

    fun publishRxEvent(classEvent: Any) = RxBus.publish(classEvent)
}

sealed class CommonEvent {
    object Success: CommonEvent()
    data class Failed(val message: String, val errorCode: String): CommonEvent()
}
