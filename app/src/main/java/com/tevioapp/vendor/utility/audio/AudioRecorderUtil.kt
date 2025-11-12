package com.tevioapp.vendor.utility.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.CountDownTimer
import java.io.File

class AudioRecorderUtil(
    private val context: Context,
    private val maxDurationSec: Int,
    private val callback: RecorderCallback
) {

    private var recorder: MediaRecorder? = null
    private var outputFile: String? = null
    private var timer: CountDownTimer? = null
    private var status: RecorderStatus = RecorderStatus.IDLE

    /** Start recording, returns output file path */
    fun startRecording() {
        stopRecording()
        try {
            outputFile =
                File(context.cacheDir, "recording_${System.currentTimeMillis()}.m4a").absolutePath
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            status = RecorderStatus.STARTED
            callback.onStatusChanged(status)

            var elapsedSec = 0

            timer = object : CountDownTimer(maxDurationSec * 1000L, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    callback.updateAmplitude(recorder?.maxAmplitude ?: 0)
                    val newElapsed = ((maxDurationSec * 1000L - millisUntilFinished) / 1000L).toInt()
                    if (newElapsed >= elapsedSec) {
                        elapsedSec = newElapsed
                        callback.onElapsed(elapsedSec)
                    }
                }

                override fun onFinish() {
                    stopRecording()
                    callback.onMaxReached()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
            outputFile = null
            status = RecorderStatus.ERROR
            callback.onStatusChanged(status)
            callback.onError(e)
            stopRecording()
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
        timer?.cancel()
        timer = null
        status = RecorderStatus.STOPPED
        callback.onStatusChanged(status)
    }

    /** Release resources; call in onDestroy/onCleared */
    fun release() {
        stopRecording()
        recorder = null
        timer = null
        status = RecorderStatus.IDLE
    }

    fun getOutputFile(): String? = outputFile
    fun getStatus(): RecorderStatus = status
}

