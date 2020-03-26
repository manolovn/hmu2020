package me.amryousef.webrtc_demo

import android.graphics.Path
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

sealed class DrawingCommand {
    object None : DrawingCommand()
    data class TouchedUpContent(val path: Path, val circlePath: Path): DrawingCommand()
    data class Content(val path: Path, val circlePath: Path) : DrawingCommand()
    object Clear : DrawingCommand()
}

class DrawingController(private val drawingView: CameraDrawingView) {

    private val drawingSubscriptions = CompositeDisposable()

    fun start() {
    }

    fun stop() {
        drawingSubscriptions.clear()
    }

    fun bindDrawingCommands(stream: Observable<DrawingCommand>) {
        stream
            .retry()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onDrawCommand, ::logError)
            .also { drawingSubscriptions.add(it) }
    }

    private fun onDrawCommand(drawCommand: DrawingCommand) {
        drawingView.commandToPaint = drawCommand
        drawingView.invalidate()
    }

    private fun logError(throwable: Throwable) {
        Log.e("ERROR", "something unexpected $throwable")
    }
}