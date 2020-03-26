package me.amryousef.webrtc_demo

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

sealed class TouchEvent {
    abstract val type: String

    data class None(override val type: String = TouchEventType.None) : TouchEvent()
    data class ActionDown(val x: Float, val y: Float, override val type: String = TouchEventType.ActionDown) : TouchEvent()
    data class ActionMove(val x: Float, val y: Float, override val type: String = TouchEventType.ActionMove) : TouchEvent()
    data class ActionUp(override val type: String = TouchEventType.ActionUp) : TouchEvent()
}

class DrawingController(private val drawingView: CameraDrawingView) {

    private val drawingSubscriptions = CompositeDisposable()

    fun start() {
    }

    fun stop() {
        drawingSubscriptions.clear()
    }

    fun bindDrawingCommands(stream: Observable<TouchEvent>) {
        stream
            .subscribe(::onTouchEvent, ::logError)
            .also { drawingSubscriptions.add(it) }
    }

    private fun onTouchEvent(touchEvent: TouchEvent) {
        drawingView.mOnTouchEvent(touchEvent)
    }

    private fun logError(throwable: Throwable) {
        Log.e("ERROR", "something unexpected $throwable")
    }
}