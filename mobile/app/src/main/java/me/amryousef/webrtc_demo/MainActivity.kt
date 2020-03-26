package me.amryousef.webrtc_demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import io.ktor.util.KtorExperimentalAPI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }

    private lateinit var rtcClient: RTCClient
    private lateinit var signallingClient: SignallingClient

    private lateinit var drawingController: DrawingController
    private lateinit var editionController: EditionController

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
            signallingClient.send(p0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingController = DrawingController(local_view)
        editionController = EditionController(drawingController)
        if (BuildConfig.IS_ADMIN) {
            drawOnScreen.visibility = View.VISIBLE
            drawOnScreen.setOnClickListener {
                if (editionController.isEditing) {
                    videoContainerView.setOnTouchListener(null)
                    editionController.stopEditing()
                } else {
                    editionController.startEditing()
                    videoContainerView.setOnTouchListener(editionController)
                }
            }
        }
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        } else {
            onCameraPermissionGranted()
        }
    }

    private fun onCameraPermissionGranted() {
        rtcClient = RTCClient(
            application,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    signallingClient.send(p0)
                    rtcClient.addIceCandidate(p0)
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(remote_view)
                }
            }
        )
        drawingController.start()
        rtcClient.initSurfaceView(remote_view)
        rtcClient.initSurfaceView(local_view)

        rtcClient.startLocalVideoCapture(local_view)
        signallingClient = SignallingClient(createSignallingClientListener())
        videoOff.setOnClickListener {
            rtcClient.call(sdpObserver)
        }
        switchCamera.setOnClickListener {
            rtcClient.switchCamera()
        }
    }

    private fun createSignallingClientListener() = object : SignallingClientListener {
        override fun onConnectionEstablished() {
            videoOff.isClickable = true
        }

        override fun onOfferReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            rtcClient.answer(sdpObserver)
            remote_view_loading.isGone = true
        }

        override fun onAnswerReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            remote_view_loading.isGone = true
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            rtcClient.addIceCandidate(iceCandidate)
        }
    }

    private fun requestCameraPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION) && !dialogShown) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Required")
            .setMessage("This app need the camera to function")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestCameraPermission(true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onCameraPermissionDenied()
            }
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onCameraPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        signallingClient.destroy()
        drawingController.stop()
        super.onDestroy()
    }
}
