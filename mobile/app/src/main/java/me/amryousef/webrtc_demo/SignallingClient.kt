package me.amryousef.webrtc_demo

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import me.amryousef.webrtc_demo.emoji.EmojiCommand
import me.amryousef.webrtc_demo.endcall.END_CALL_COMMAND_ID
import me.amryousef.webrtc_demo.endcall.EndCallCommand

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class SignallingClient(
    private val listener: SignallingClientListener
) : CoroutineScope {

    private companion object {
        const val HOST_ADDRESS = "hmu2020.herokuapp.com"
    }

    private val job = Job()

    private val gson = GsonBuilder().apply {
        registerTypeAdapter(TouchEvent::class.java, TouchEventAdapter())
    }.create()

    private val touchEventsPublisher = PublishSubject.create<TouchEvent>()

    override val coroutineContext = Dispatchers.IO + job

    private val client = HttpClient(CIO) {
        install(WebSockets)
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    private val sendChannel = ConflatedBroadcastChannel<String>()

    fun start() {
        connect()
    }

    private fun connect() = launch {
        client.ws(host = HOST_ADDRESS, port = 80, path = "/connect") {
            Log.d("SignallingClient", "connected!")
            listener.onConnectionEstablished()
            val sendData = sendChannel.openSubscription()
            try {
                while (true) {
                    sendData.poll()?.let {
                        Log.d("SignallingClient", "Sending: $it")
                        outgoing.send(Frame.Text(it))
                    }
                    incoming.poll()?.let { frame ->
                        if (frame is Frame.Text) {
                            val data = frame.readText()
                            Log.d("SignallingClient", "Received: $data")
                            val jsonObject = gson.fromJson(data, JsonObject::class.java)
                            withContext(Dispatchers.Main) {
                                if (jsonObject.has("serverUrl")) {
                                    listener.onIceCandidateReceived(gson.fromJson(jsonObject, IceCandidate::class.java))
                                } else if (jsonObject.has("type") && jsonObject.get("type").asString == "OFFER") {
                                    listener.onOfferReceived(gson.fromJson(jsonObject, SessionDescription::class.java))
                                } else if (jsonObject.has("type") && jsonObject.get("type").asString == "ANSWER") {
                                    listener.onAnswerReceived(gson.fromJson(jsonObject, SessionDescription::class.java))
                                } else if (jsonObject.has("type") && jsonObject.get("type").asString == "EMOJI") {
                                    listener.onEmojiReceived(jsonObject.get("emojiCode").asInt)
                                } else if (jsonObject.has("type") && jsonObject.get("type").asString == END_CALL_COMMAND_ID) {
                                    listener.onEndCallReceived()
                                } else {
                                    try {
                                        val touchEvent = gson.fromJson(data, TouchEvent::class.java)
                                        touchEventsPublisher.onNext(touchEvent)
                                    } catch (throwable: Throwable) {
                                        Log.d("ERROR", "error $throwable")
                                        // gotta catch them all
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (exception: Throwable) {
                Log.e("SignallingClient", "Failed due to exception: ", exception)
            }
        }
    }

    fun wsTouchEvents(): Observable<TouchEvent> = touchEventsPublisher

    fun send(dataObject: Any?) = runBlocking {
        sendChannel.send(gson.toJson(dataObject))
    }

    fun sendFromIO(dataObject: Any?) {
        launch {
            withContext(Dispatchers.IO) {
                sendChannel.send(gson.toJson(dataObject))
            }
        }
    }

    fun destroy() {
        client.close()
        job.complete()
    }

    fun sendEmoji(it: Int) = runBlocking {
        val emojiObject = EmojiCommand("EMOJI", it)
        sendChannel.send(gson.toJson(emojiObject))
    }

    fun endCall() = runBlocking {
        sendChannel.send(gson.toJson(EndCallCommand()))
    }
}