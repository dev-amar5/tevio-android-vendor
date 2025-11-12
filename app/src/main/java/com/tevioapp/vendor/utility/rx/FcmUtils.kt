package com.tevioapp.vendor.utility.rx

import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Single

object FcmUtils {
    fun getCurrentToken(): Single<String> {
        return Single.create { emitter ->
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                emitter.onSuccess(it)
            }.addOnFailureListener {
                emitter.onError(it)
            }

        }
    }
}