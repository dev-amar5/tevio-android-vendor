package com.tevioapp.vendor.utility.agora

import android.content.Context
import android.media.AudioManager
import com.tevioapp.vendor.utility.log.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@ActivityRetainedScoped
class AgoraEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var rtcEngine: RtcEngine? = null
    private var isSpeakerOn = false

    private val subjectChannelJoin = PublishSubject.create<Int>()
    private val subjectUserJoin = PublishSubject.create<Int>()
    private val subjectUserLeft = PublishSubject.create<Int>()
    private val subjectUserMute = BehaviorSubject.create<Boolean>()

    @Synchronized
    fun initEngine(appId: String) {
        if (rtcEngine != null) return

        val rtcHandler = object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                Logger.d("Agora → Joined channel: $uid")
                subjectChannelJoin.onNext(uid)
                setupAudioRoute(false)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                subjectUserJoin.onNext(uid)
                Logger.d("Agora → User joined: $uid")
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                subjectUserLeft.onNext(uid)
                Logger.d("Agora → User offline: $uid")
            }

            override fun onUserMuteAudio(uid: Int, muted: Boolean) {
                subjectUserMute.onNext(muted)
                Logger.d("Agora → User mute changed: $uid → $muted")
            }
        }

        try {
            val config = RtcEngineConfig().apply {
                mContext = this@AgoraEngine.context.applicationContext
                mAppId = appId
                mEventHandler = rtcHandler
                mChannelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                mAudioScenario = Constants.AUDIO_SCENARIO_DEFAULT
            }

            rtcEngine = RtcEngine.create(config).apply {
                setDefaultAudioRoutetoSpeakerphone(false)
                setEnableSpeakerphone(false)
                adjustPlaybackSignalVolume(90)
            }

            Logger.d("Agora → Engine initialized")

        } catch (e: Exception) {
            Logger.e("Agora → Initialization failed: ${e.message}", e)
            rtcEngine = null
        }
    }

    private fun setupAudioRoute(useSpeaker: Boolean = false) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        isSpeakerOn = useSpeaker
        if (useSpeaker) {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = true
            rtcEngine?.setEnableSpeakerphone(true)
            rtcEngine?.adjustPlaybackSignalVolume(300)
        } else {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = false
            rtcEngine?.setDefaultAudioRoutetoSpeakerphone(false)
            rtcEngine?.setEnableSpeakerphone(false)
            rtcEngine?.adjustPlaybackSignalVolume(180)
        }
        Logger.d("Agora → Audio route: ${if (useSpeaker) "SPEAKER" else "EARPIECE"}")
    }

    fun toggleSpeaker() {
        setupAudioRoute(!isSpeakerOn)
    }

    fun joinChannel(channel: String, token: String, uid: Int) {
        Logger.d("Agora → Joining channel: $channel, uid=$uid")
        setupAudioRoute(false)
        rtcEngine?.joinChannel(token, channel, "", uid)
    }

    fun leaveChannel() {
        try {
            rtcEngine?.leaveChannel()
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_NORMAL
            Logger.d("Agora → Left channel")
        } catch (e: Exception) {
            Logger.e("Agora → leaveChannel failed: ${e.message}", e)
        }
    }

    fun destroy() {
        try {
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
            rtcEngine = null
            Logger.d("Agora → Engine destroyed")
        } catch (e: Exception) {
            Logger.e("Agora → Destroy failed: ${e.message}", e)
        }
    }

    fun muteLocalAudio(mute: Boolean) {
        rtcEngine?.muteLocalAudioStream(mute)
        Logger.d("Agora → Local audio mute=$mute")
    }

    fun onChannelJoin(): Observable<Int> = subjectChannelJoin
    fun onUserJoin(): Observable<Int> = subjectUserJoin
    fun onUserLeft(): Observable<Int> = subjectUserLeft
    fun onUserMuteAudio(): Observable<Boolean> = subjectUserMute
}
