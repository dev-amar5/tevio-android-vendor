package com.tevioapp.vendor.utility.socket

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.utility.log.Logger
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.lang.reflect.Type
import java.net.URISyntaxException
import java.util.concurrent.TimeUnit

class SocketClient(
    private val baseUrl: String,
    private val sharedPref: SharedPref,
    private val enableLogging: Boolean = false
) {

    private val gson = Gson()
    private var socket: Socket? = null
    private var token: String? = null
    private val messageSubject = PublishSubject.create<Pair<String, JSONObject>>()
    private val connectionStateSubject =
        BehaviorSubject.createDefault<ConnectionState>(ConnectionState.Disconnected)
    private val registeredEvents = mutableSetOf<String>()
    private var wasPreviouslyConnected = false // Track if previously connected


    private fun buildSocket(): Socket? {
        return try {
            val url = buildUrl()
            log("Socket Url $url")
            val options = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = 3
                reconnectionDelay = 3000
                reconnectionDelayMax = 10000
                forceNew = true
            }
            IO.socket(url, options).apply {
                off(Socket.EVENT_CONNECT)
                off(Socket.EVENT_DISCONNECT)
                off(Socket.EVENT_CONNECT_ERROR)

                on(Socket.EVENT_CONNECT) {
                    log("Connected")
                    if (wasPreviouslyConnected) {
                        connectionStateSubject.onNext(ConnectionState.Reconnected)
                    } else {
                        connectionStateSubject.onNext(ConnectionState.Connected)
                    }
                    wasPreviouslyConnected = true
                }
                on(Socket.EVENT_DISCONNECT) {
                    log("Disconnected")
                    connectionStateSubject.onNext(ConnectionState.Disconnected)
                }
                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    val error = (args.firstOrNull() as? Throwable) ?: Throwable(
                        args.firstOrNull()?.toString() ?: "Unknown error"
                    )
                    if (error.message.orEmpty().contains("xhr")) {
                        connectionStateSubject.onNext(ConnectionState.NoInternet)
                    } else {
                        connectionStateSubject.onNext(ConnectionState.Error(error))
                    }
                }

            }
        } catch (e: URISyntaxException) {
            log("URI Syntax Error: $e")
            connectionStateSubject.onNext(ConnectionState.Error(e))
            null
        }
    }

    private fun buildUrl(): String {
        return "$baseUrl?token=$token"
    }

    fun connect(): Completable = Completable.fromAction {
        val newToken = sharedPref.getToken()
        when {
            // First-time connection
            socket == null -> {
                token = newToken
                socket = buildSocket()
                socket?.connect()
            }
            // Token changed → rebuild socket completely
            token != newToken -> {
                log("Token changed → rebuilding socket with new token")
                token = newToken
                socket?.apply {
                    offAll()
                    disconnect()
                    close()
                }
                socket = buildSocket()
                socket?.connect()
            }
            // Socket exists but is not connected
            socket?.connected() == false -> {
                log("Socket exists but not connected → reconnecting")
                socket?.connect()
            }
            // Already connected with same token
            else -> {
                log("Socket already connected with same token → skipping connect()")
            }
        }
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    fun disconnect(): Completable = Completable.fromAction {
        socket?.disconnect()
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    fun <T> emit(event: String, data: T): Completable = Completable.create { emitter ->
        if (socket?.connected() == true) {
            val json = JSONObject(gson.toJson(data))
            log("Emitting event: $event data: $json")
            socket?.emit(event, json)
            emitter.onComplete()
        } else {
            emitter.onError(Throwable("Socket not connected"))
        }
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun <T> emitWithAck(
        event: String, data: JSONObject = JSONObject(), type: Type, timeoutMs: Long = 10000L
    ): Single<T> {
        return Single.create<T> { emitter ->
            try {
                val ack = Ack { args ->
                    if (args.isNotEmpty()) {
                        val responseJson = args[0].toString()
                        log("Ack response for event: $event ->", responseJson)
                        try {
                            val response: T? = gson.fromJson(responseJson, type)
                            if (response != null) {
                                if (!emitter.isDisposed) emitter.onSuccess(response)
                            } else {
                                if (!emitter.isDisposed) emitter.onError(Throwable("Parsed response is null"))
                            }
                        } catch (ex: Exception) {
                            if (!emitter.isDisposed) emitter.onError(ex)
                        }
                    } else {
                        if (!emitter.isDisposed) emitter.onError(Throwable("Empty ack response"))
                    }
                }
                socket?.emit(event, data, ack)
            } catch (e: Exception) {
                if (emitter.isDisposed.not()) emitter.onError(e)
            }
        }.timeout(timeoutMs, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io())
    }

    fun <T> listen(event: String, type: Type): Observable<T> {
        if (event !in registeredEvents) {
            socket?.on(event) { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONObject) {
                        log(" Socket : $socket listen for event: $event ->", data.toString())
                        messageSubject.onNext(event to data)
                    } else {
                        log("Unexpected data format for event: $event. Data: $data")
                    }
                }
            }
            log("Socket : $socket  Listen event: $event added")
            registeredEvents.add(event)
        } else {
            log("Socket : $socket Listen event: $event already registered")
        }
        return messageSubject.filter { it.first == event }.map { (_, json) ->
            gson.fromJson<T>(json.toString(), type)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun off(vararg events: String) {
        events.forEach { event ->
            log("Removing listener for event: $event")
            socket?.off(event)
            registeredEvents.remove(event)
        }
    }

    fun offAll() {
        registeredEvents.forEach { event ->
            log("Removing listener for event: $event")
            socket?.off(event)
        }
        registeredEvents.clear()
    }

    fun getConnectionState(): Observable<ConnectionState> =
        connectionStateSubject.hide().distinctUntilChanged()

    private fun log(vararg message: String?) {
        if (enableLogging) {
            message.forEach {
                Logger.d(it)
            }
        }
    }
}


sealed class ConnectionState(open val message: String) {
    data object Connected : ConnectionState("Connected successfully")
    data object Reconnected : ConnectionState("Socket reconnected successfully")
    data object Disconnected : ConnectionState("Socket disconnected try after some time!")
    data object NoInternet : ConnectionState("Socket connection error")
    data class Error(
        val throwable: Throwable? = null,
        override val message: String = throwable?.message.orEmpty().ifEmpty { "An error occurred" }
    ) : ConnectionState(message)
}

inline fun <reified T> getTypeToken(): Type = object : TypeToken<T>() {}.type
