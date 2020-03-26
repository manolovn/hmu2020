package me.amryousef.webrtc_demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import org.webrtc.SurfaceViewRenderer

class CameraDrawingView(context: Context, attrs: AttributeSet) : SurfaceViewRenderer(context, attrs) {

    var commandToPaint: DrawingCommand = DrawingCommand.None

    init {
        setWillNotDraw(false)
        visibility = View.VISIBLE
    }

    private val paintLine = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        strokeWidth = 5F
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (commandToPaint) {
            DrawingCommand.None -> Unit
            DrawingCommand.Line -> canvas.drawLine(50F, 50f, 300f, 300F, paintLine)
            DrawingCommand.Clear -> canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }
    }
}