package me.amryousef.webrtc_demo

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plattysoft.leonids.ParticleSystem
import io.ktor.util.KtorExperimentalAPI
import kotlinx.android.synthetic.main.actions.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.amryousef.webrtc_demo.emoji.EmojiAdapter
import me.amryousef.webrtc_demo.emoji.EmojiController
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import kotlinx.android.synthetic.main.activity_main.actions as mainActions
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_RECORD_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    private lateinit var rtcClient: RTCClient
    private val signallingClient: SignallingClient = SignallingClient(createSignallingClientListener())

    private lateinit var drawingController: DrawingController
    private lateinit var editionController: EditionController

    private lateinit var emojiController: EmojiController

    private var remoteViewLoaded = false
    private var showingEmojiList = false
    private var torchStatus = false

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
            signallingClient.send(p0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        drawingController = DrawingController(local_view)

        setUpShowHideControls()
        editionController = EditionController(drawingController, BuildConfig.IS_ADMIN, DrawingEventsDispatcher(signallingClient))
        if (BuildConfig.IS_ADMIN) {
            drawOnScreen.visibility = VISIBLE
            flashlight.visibility = GONE
            drawOnScreen.setOnClickListener {
                if (editionController.isEditing) {
                    videoContainerView.setOnTouchListener(null)
                    editionController.stopEditing()
                } else {
                    editionController.startEditing()
                    videoContainerView.setOnTouchListener(editionController)
                }
            }
        } else {
            flashlight.visibility = VISIBLE
            drawOnScreen.visibility = GONE
            flashlight.setOnClickListener {
                val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                try {
                    torchStatus = !torchStatus
                    cameraManager.cameraIdList.forEach {
                        cameraManager.setTorchMode(it, torchStatus)
                    }
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }
        }
        checkCameraPermission()
        setupEmojis()
        mute.setOnClickListener {  }
    }

    private fun setUpShowHideControls() {
        var controlsShown = true
        videoContainerView.setOnClickListener {
            val visibility = if (controlsShown) GONE else VISIBLE
            controlsShown = !controlsShown
            if (!remoteViewLoaded) {
                remote_view_loading.visibility = visibility
            }
            if (showingEmojiList) {
                emojisList.visibility = visibility
            }
            val controlsToHide = arrayOf(topElementsContainer, remote_view, mainActions, end_call)
            controlsToHide.forEach { it.visibility = visibility }
        }
    }

    private fun setupEmojis() {
        emojiController = EmojiController(signallingClient)
        emojiController.start()
        emojisList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        emojisList.adapter = EmojiAdapter(emojiController.getEmojis(), this)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, AUDIO_RECORD_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
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
                    if (BuildConfig.IS_ADMIN) {
                        p0?.videoTracks?.get(0)?.addSink(local_view)
                    } else {
                        p0?.videoTracks?.get(0)?.addSink(remote_view)
                    }
                }
            }
        )
        drawingController.start()
        rtcClient.initSurfaceView(remote_view)
        rtcClient.initSurfaceView(local_view)

        if (BuildConfig.IS_ADMIN) {
            rtcClient.startLocalVideoCapture(remote_view)
        } else {
            rtcClient.startLocalVideoCapture(local_view)
        }

        signallingClient.start()
        videoOff.setOnClickListener {
            rtcClient.call(sdpObserver)
        }
        switchCamera.setOnClickListener {
            rtcClient.switchCamera()
        }
        sendEmojiButton.setOnClickListener {
            showEmojisPanel()
        }
        end_call.setOnClickListener {
            signallingClient.endCall()
            finish()
        }
        pipButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                enterPictureInPictureMode()
            }
        }
    }

    private fun showEmojisPanel() {
        if (emojisList.visibility == VISIBLE) {
            showingEmojiList = false
            emojisList.isSelected = false
            emojisList.visibility = GONE
        } else {
            showingEmojiList = true
            emojisList.isSelected = true
            emojisList.visibility = VISIBLE
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
            remoteViewLoaded = true
        }

        override fun onAnswerReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            remote_view_loading.isGone = true
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            rtcClient.addIceCandidate(iceCandidate)
        }

        override fun onEmojiReceived(emojiCode: Int) {
            ParticleSystem(this@MainActivity, 24, emojiController.getEmojiById(emojiCode).image, 1500L)
                .setSpeedRange(0.2f, 0.5f)
                .oneShot(emojis_view, 6)

            /*emojis_view.setImageResource(emojiController.getEmojiById(emojiCode).image)
            val deltaY: Float = emojis_view.bottom + 3F
            val anim = ObjectAnimator.ofFloat(emojis_view, "translationY", emojis_view.bottom.toFloat(), deltaY)
            anim.duration = 500
            anim.interpolator = AccelerateDecelerateInterpolator()
            val fadeAnim = ObjectAnimator.ofFloat(emojis_view, "alpha", 0f, 1f)
            anim.duration = 350
            val animatorSet = AnimatorSet()
            //animatorSet.play(anim)
            animatorSet.play(fadeAnim)
            animatorSet.start()
            object : CountDownTimer(1500, 1000) {
                override fun onFinish() {
                    emojis_view.setImageResource(0)
                }

                override fun onTick(millisUntilFinished: Long) {

                }
            }.start()*/
        }

        override fun onEndCallReceived() {
            finish()
        }
    }

    private fun requestCameraPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION) && !dialogShown) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(
                CAMERA_PERMISSION, AUDIO_RECORD_PERMISSION
            ), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("This app need the camera and mic to function")
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
