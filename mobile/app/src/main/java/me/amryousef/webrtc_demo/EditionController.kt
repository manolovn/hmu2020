package me.amryousef.webrtc_demo

import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlin.math.abs

class EditionController(drawingController: DrawingController) : View.OnTouchListener {

    private val mPath = Path()
    private val circlePath = Path()

    private val drawingCommands = PublishSubject.create<DrawingCommand>()

    init {
        drawingController.bindDrawingCommands(drawingCommands)
    }

    var isEditing = false
        private set

    fun startEditing() {
        isEditing = true
    }

    fun stopEditing() {
        isEditing = false
    }

    private var x = 0f
    private var y = 0f

    private fun onTouchStart(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        this.x = x
        this.y = y
    }

    private fun onTouchMove(x: Float, y: Float) {
        val dx = abs(x - this.x)
        val dy: Float = abs(y - this.y)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2)
            this.x = x
            this.y = y
            circlePath.reset()
            circlePath.addCircle(this.x, this.y, 30F, Path.Direction.CW)
        }
    }

    private fun onTouchUp() {
        mPath.lineTo(x, y)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchStart(x, y)
                drawingCommands.onNext(DrawingCommand.Content(mPath, circlePath))
            }
            MotionEvent.ACTION_MOVE -> {
                onTouchMove(x, y)
                drawingCommands.onNext(DrawingCommand.Content(mPath, circlePath))
            }
            MotionEvent.ACTION_UP -> {
                onTouchUp()
                drawingCommands.onNext(DrawingCommand.TouchedUpContent(mPath, circlePath))
            }
        }
        return true
    }

    private companion object {
        const val TOUCH_TOLERANCE = 4f
    }
}