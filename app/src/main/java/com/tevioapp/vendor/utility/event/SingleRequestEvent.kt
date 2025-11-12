package com.tevioapp.vendor.utility.event

import androidx.lifecycle.LifecycleOwner
import com.tevioapp.vendor.utility.event.helper.Resource

class SingleRequestEvent<T> : SingleLiveEvent<Resource<T>>() {

    fun observe(owner: LifecycleOwner, observer: (Resource<T>) -> Unit) {
        super.observe(
            owner
        ) { value ->
                observer(value)
        }

    }
}
