package com.example.ozturkse.x.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.ozturkse.x.CameraSource
import com.example.ozturkse.x.FaceDetectionProcessor
import com.example.ozturkse.x.R
import com.example.ozturkse.x.ui.landing.LandingActivity
import com.example.ozturkse.x.util.Util
import com.google.firebase.FirebaseApp
import com.monitise.mea.android.caki.extensions.doIfGranted
import com.monitise.mea.android.caki.extensions.handlePermissionsResult
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity(), MainView {

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 0
        const val INTENT_ADD_USER = "add_user"
        const val TAG = "MainActivity"

        private lateinit var bitmap: Bitmap
        private lateinit var filesDirector: File
        private lateinit var appContext: Context
        private lateinit var mainPresenter: MainPresenter

        private var requestSent = false

        fun updateBitmap(bitmap: Bitmap) {
            this.bitmap = bitmap
        }

        fun recognizeFace() {
            if(Util.isInternetAvailable(appContext)){
                Toast.makeText(appContext, "No internet connection", Toast.LENGTH_SHORT).show()
                return
            }

            if (!requestSent) {
                mainPresenter.recognizeFace(bitmap, filesDirector, 0f)
                requestSent = true
            }
        }
    }

    private var cameraSource: CameraSource? = null

    private var hasStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        setSupportActionBar(activity_main_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        appContext = applicationContext
        filesDirector = application.filesDir
        mainPresenter = MainPresenter(this, MainInteractor())

        if (!hasStarted) {
            doIfGranted(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION) {
                createCameraSource()
                hasStarted = true
            }
        }

        activity_main_imagebutton_switchcamera.setOnClickListener { switchCamera() }
        activity_main_imagebutton_adduser.setOnClickListener { addUser() }
    }

    override fun showResponse(guess: String?) {
        activity_main_progressbar.visibility = View.INVISIBLE
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Found")
        builder.setMessage(guess)
        builder.setPositiveButton("OK") { _, _ ->
            requestSent = false
        }

        val dialog: AlertDialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun showError(message: String?) {
        requestSent = false
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun addUser() {
        val intent = Intent(this, LandingActivity::class.java)
        intent.putExtra(INTENT_ADD_USER, true)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            MainActivity.REQUEST_CAMERA_PERMISSION ->
                handlePermissionsResult(permissions, grantResults,
                        onDenied = {
                            val builder = AlertDialog.Builder(this@MainActivity)
                            builder.setTitle(":(")
                            builder.setMessage(
                                    "In order to use this app, you should give permission to use camera, from Settings!"
                            )

                            val dialog: AlertDialog = builder.create()
                            dialog.setCanceledOnTouchOutside(false)
                            dialog.show()
                        },
                        onGranted = {
                            createCameraSource()
                            hasStarted = true
                        }
                )
        }
    }

    private fun createCameraSource() {
        // If there's no existing cameraSource, create one.
        val cameraSourceCopy = cameraSource
        if (cameraSourceCopy == null) {
            cameraSource = CameraSource(this, fireFaceOverlay)
        }

        cameraSource!!.setMachineLearningFrameProcessor(FaceDetectionProcessor())
    }

    private fun startCameraSource() {
        val cameraSourceCopy = cameraSource
        if (cameraSourceCopy != null) {
            try {
                if (firePreview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                if (fireFaceOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null")
                }
                firePreview.start(cameraSource, fireFaceOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource?.release()
                cameraSource = null
            }

        }
    }

    fun switchCamera() {
        if (cameraSource?.cameraFacing == CameraSource.CAMERA_FACING_BACK) {
            cameraSource?.setFacing(CameraSource.CAMERA_FACING_FRONT)
        } else {
            cameraSource?.setFacing(CameraSource.CAMERA_FACING_BACK)
        }
        firePreview.stop()
        startCameraSource()
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        startCameraSource()
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        firePreview.stop()
    }

    override fun onDestroy() {
        mainPresenter.onDestroy()
        val cameraSourceCopy = cameraSource
        if (cameraSourceCopy != null) {
            cameraSource?.release()
        }
        super.onDestroy()
    }

    override fun showLoading() {
        activity_main_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        activity_main_progressbar.visibility = View.INVISIBLE
    }
}