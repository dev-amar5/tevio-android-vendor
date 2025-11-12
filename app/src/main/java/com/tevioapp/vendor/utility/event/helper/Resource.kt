package com.tevioapp.vendor.utility.event.helper

import com.tevioapp.vendor.network.helper.MetaData

/**
 * A generic class that holds a value with its loading status.
 *
 * @param <T>
</T> */
class Resource<T> private constructor(
    val status: Status, var data: T?, val message: String = "", val metaData: MetaData? = null,val errorCode: Int? = null
) {

    override fun toString(): String {
        return "Resource{status=$status, message='$message', data=$data metadata=$metaData}"
    }

    companion object {
        fun <T> success(
            data: T?, msg: String = "", metaData: MetaData? = null
        ): Resource<T> {
            return Resource(
                status = Status.SUCCESS, data = data, message = msg, metaData = metaData
            )
        }

        fun <T> warn(data: T? = null, msg: String = ""): Resource<T> {
            return Resource(Status.WARN, data, msg)
        }

        fun <T> error(data: T? = null, errMsg: String = "", errorCode: Int? = null): Resource<T> {
            return Resource(Status.ERROR, data, errMsg,errorCode=errorCode)
        }

        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, null)
        }
    }

}