package me.amryousef.webrtc_demo

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.functions.BiFunction as BiFunction1

sealed class DrawingCommand {
    object None : DrawingCommand()
    object Line : DrawingCommand()
    object Clear : DrawingCommand()
}

class DrawingController(private val drawingView: CameraDrawingView) {

    private var drawingSubscription = Disposable.empty()

    private val canDrawSubject = PublishSubject.create<Boolean>()
    private val drawingCommands = PublishSubject.create<DrawingCommand>()

    private val canDrawStream = canDrawSubject.startWithItem(true)
    private val drawingCommandsStream = drawingCommands.startWithItem(DrawingCommand.Line)

    fun start() {
        Observable.combineLatest(
            canDrawStream,
            drawingCommandsStream,
            BiFunction1<Boolean, DrawingCommand, DrawingCommand> { canDraw, drawingCommand ->
                if (canDraw) {
                    drawingCommand
                } else {
                    DrawingCommand.None
                }
            })
            .retry()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onDrawCommand) { Log.e("ERROR", "something unexpected ${it.message}") }
            .also { drawingSubscription = it }
    }

    fun stop() {
        drawingSubscription.dispose()
    }

    fun submitCommand() {
        drawingCommands.onNext(DrawingCommand.Clear)
    }

    private fun onDrawCommand(drawCommand: DrawingCommand) {
        drawingView.commandToPaint = drawCommand
        drawingView.invalidate()
    }
}