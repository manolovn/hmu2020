package me.amryousef.webrtc_demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import org.webrtc.SurfaceViewRenderer

class CameraDrawingView(context: Context, attrs: AttributeSet) : SurfaceViewRenderer(context, attrs) {

    var commandToPaint: DrawingCommand = DrawingCommand.None

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
        with(commandToPaint) {
            when (this) {
                is DrawingCommand.TouchedUpContent -> {
                    circlePath.reset()
                    canvas.drawPath(path, paint)
                    path.reset()
                    canvas.drawPath(path, paint)
                    canvas.drawPath(circlePath, circlePaint)
                }
                is DrawingCommand.Content -> {
                    canvas.drawPath(path, paint)
                    canvas.drawPath(circlePath, circlePaint)
                }
            }
        }
    }
}