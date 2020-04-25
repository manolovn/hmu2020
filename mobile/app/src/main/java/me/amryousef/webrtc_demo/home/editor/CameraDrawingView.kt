package me.amryousef.webrtc_demo.home.editor

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.webrtc.SurfaceViewRenderer
import kotlin.math.abs

class CameraDrawingView(context: Context, attrs: AttributeSet) : SurfaceViewRenderer(context, attrs) {

    private var touchingUp = false
    private var clearingCanvas = false

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 12F
    }

    private val circlePaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.MITER
        strokeWidth = 4f
    }

    init {
        setWillNotDraw(false)
        visibility = View.VISIBLE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when {
            touchingUp -> {
                canvas.drawPath(mPath, paint)
                mPath.reset()
            }
            clearingCanvas -> {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                return
            }
        }
        canvas.drawPath(mPath, paint)
        canvas.drawPath(circlePath, circlePaint)
    }

    private val mPath = Path()
    private val circlePath = Path()
    private var mX = 0f
    private var mY = 0f

    private fun onTouchStart(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun onTouchMove(x: Float, y: Float) {
        val dx = abs(x - mX)
        val dy: Float = abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
            circlePath.reset()
            circlePath.addCircle(mX, mY, 30F, Path.Direction.CW)
        }
    }

    private fun onTouchUp() {
        mPath.lineTo(mX, mY)
        circlePath.reset()
    }

    fun mOnTouchEvent(event: TouchEvent) {
        when (event) {
            is TouchEvent.ActionDown -> {
                val x = event.x
                val y = event.y
                touchingUp = false
                clearingCanvas = false
                onTouchStart(x, y)
                invalidate()
            }
            is TouchEvent.ActionMove -> {
                val x = event.x
                val y = event.y
                touchingUp = false
                clearingCanvas = false
                onTouchMove(x, y)
                invalidate()
            }
            is TouchEvent.ActionUp -> {
                touchingUp = true
                clearingCanvas = false
                onTouchUp()
                invalidate()
            }
            is TouchEvent.Clear -> {
                touchingUp = false
                clearingCanvas = true
                invalidate()
            }
        }
    }

    private companion object {
        const val TOUCH_TOLERANCE = 4f
    }
}