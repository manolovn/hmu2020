package me.amryousef.webrtc_demo

import android.view.MotionEvent
import android.view.View
import io.ktor.util.KtorExperimentalAPI
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class EditionController(
    drawingController: DrawingController,
    isAdmin: Boolean,
    private val drawingEventsDispatcher: DrawingEventsDispatcher
) : View.OnTouchListener {

    private val touchEvents = PublishSubject.create<TouchEvent>()

    init {
        if (isAdmin) {
            drawingController.bindDrawingCommands(touchEvents)
        } else {
            drawingController.bindDrawingCommands(drawingEventsDispatcher.wsTouchEventsStream())
        }
    }

    var isEditing = false
        private set

    fun startEditing() {
        isEditing = true
    }

    fun stopEditing() {
        isEditing = false
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchEvent = TouchEvent.ActionDown(x, y)
                touchEvents.onNext(touchEvent)
                drawingEventsDispatcher.dispatch(touchEvent)
            }
            MotionEvent.ACTION_MOVE -> {
                val touchEvent = TouchEvent.ActionMove(x, y)
                touchEvents.onNext(touchEvent)
                drawingEventsDispatcher.dispatch(touchEvent)
            }
            MotionEvent.ACTION_UP -> {
                val touchEvent = TouchEvent.ActionUp()
                drawingEventsDispatcher.dispatch(touchEvent)
                drawingEventsDispatcher.dispatch(touchEvent)
            }
        }
        return true
    }
}