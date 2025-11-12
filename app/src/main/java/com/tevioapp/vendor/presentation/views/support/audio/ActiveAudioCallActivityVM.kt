package com.tevioapp.vendor.presentation.views.support.audio

import com.tevioapp.vendor.data.AudioCallData
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.utility.agora.AgoraEngine
import com.tevioapp.vendor.utility.event.SingleLiveEvent
import com.tevioapp.vendor.utility.log.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class ActiveAudioCallActivityVM @Inject constructor(
    private val agoraEngine: AgoraEngine
) : BaseViewModel() {

    val obrJoinChannel by lazy { SingleLiveEvent<Int>() }
    val obrUserJoin by lazy { SingleLiveEvent<Int>() }
    val obrUserLeft by lazy { SingleLiveEvent<Int>() }
    val obrUserMute by lazy { SingleLiveEvent<Boolean>() }

    fun setAgoraAppId(appId: String) {
        agoraEngine.initEngine(appId)
        compositeDisposable.add(
            agoraEngine.onChannelJoin().observeOn(AndroidSchedulers.mainThread()).subscribe {
                obrJoinChannel.postValue(it)
            })

        compositeDisposable.add(
            agoraEngine.onUserJoin().observeOn(AndroidSchedulers.mainThread()).subscribe {
                obrUserJoin.postValue(it)
            })
        compositeDisposable.add(
            agoraEngine.onUserLeft().observeOn(AndroidSchedulers.mainThread()).subscribe {
                obrUserLeft.postValue(it)
            })
        compositeDisposable.add(
            agoraEngine.onUserMuteAudio().observeOn(AndroidSchedulers.mainThread())
                .subscribe { info ->
                    obrUserMute.postValue(info)
                })
    }

    fun joinChannel(input: AudioCallData) {
        agoraEngine.joinChannel(input.threadId, input.token, input.uId)
        Logger.d("joinChannel: $input")
    }

    fun leaveChannel() {
        agoraEngine.leaveChannel()
        Logger.d("leaveChannel")
    }

    fun muteLocalAudio(mute: Boolean) {
        agoraEngine.muteLocalAudio(mute)
        Logger.d("muteLocalAudio: $mute")
    }

    fun toggleSpeaker() {
        agoraEngine.toggleSpeaker()
    }


    override fun onCleared() {
        agoraEngine.destroy()
        super.onCleared()
    }

}
