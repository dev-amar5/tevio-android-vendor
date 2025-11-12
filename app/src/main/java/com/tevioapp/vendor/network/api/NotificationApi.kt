package com.tevioapp.vendor.network.api

import com.tevioapp.vendor.data.AudioCallData
import com.tevioapp.vendor.data.ChatMessage
import com.tevioapp.vendor.data.ThreadDetail
import com.tevioapp.vendor.network.helper.ApiResponse
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @POST("chats/threads/get-thread")
    fun apiGetChatThread(@Body map: RequestBody): Single<ApiResponse<String>>

    @GET("chats/threads/{thread_id}/chat")
    fun apiGetChatList(@Path("thread_id") threadId: String): Single<ApiResponse<List<ChatMessage>>>


    @GET("chats/threads/{thread_id}/details")
    fun apiThreadDetail(@Path("thread_id") threadId: String): Single<ApiResponse<ThreadDetail>>

    @POST("chats/threads/{thread_id}/initiate-call")
    fun apiInitiateCall(@Path("thread_id") threadId: String): Single<ApiResponse<AudioCallData>>

}