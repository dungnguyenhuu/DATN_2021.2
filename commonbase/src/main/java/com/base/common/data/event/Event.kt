package com.base.common.data.event

class Event<T>(val content: T) {

    var isHasBeenHandled: Boolean = false

    /**
     * Returns the content and prevents its use again.
     */
    val contentIfNotHandled: T?
        get() {
            if (isHasBeenHandled)
                return null
            else {
                isHasBeenHandled = true
                return content
            }
        }
}