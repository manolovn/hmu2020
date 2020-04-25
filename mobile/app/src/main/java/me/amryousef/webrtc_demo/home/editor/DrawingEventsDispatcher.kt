package me.amryousef.webrtc_demo.home.editor

import io.ktor.util.KtorExperimentalAPI
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.amryousef.webrtc_demo.data.SignallingClient

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class DrawingEventsDispatcher(
    private val signallingClient: SignallingClient
) {

    fun dispatch(touchEvent: TouchEvent) {
        signallingClient.sendFromIO(touchEvent)
    }

    fun wsTouchEventsStream(): Observable<TouchEvent> = signallingClient.wsTouchEvents()
}