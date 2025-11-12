package com.tevioapp.vendor.utility.audio

interface RecorderCallback {
    /** Current amplitude in decibels (dB) */
    fun updateAmplitude(amp: Int)

    /** Elapsed seconds since recording started */
    fun onElapsed(seconds: Int)

    /** Called when max duration is reached automatically */
    fun onMaxReached()

    /** Called when status changes */
    fun onStatusChanged(status: RecorderStatus)

    /** Called if recording fails to start or errors during run */
    fun onError(error: Exception)
}
