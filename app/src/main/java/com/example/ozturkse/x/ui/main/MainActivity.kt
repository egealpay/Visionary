package com.example.ozturkse.x.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.ozturkse.x.AboutActivity
import com.example.ozturkse.x.CameraSource
import com.example.ozturkse.x.R
import com.example.ozturkse.x.barcode.BarcodeScanningProcessor
import com.example.ozturkse.x.face.FaceDetectionProcessor
import com.example.ozturkse.x.imagelabeling.ImageLabelingProcessor
import com.example.ozturkse.x.textrecognition.TextRecognitionProcessor
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
        const val FACE_DETECTION = "Face Detection"
        const val BARCODE_DETECTION = "Barcode Detection"
        const val IMAGE_LABEL_DETECTION = "Label Detection"
        const val TEXT_RECOGNITION = "Text Recognition"

        var responseName = ""

        private lateinit var bitmap: Bitmap
        private lateinit var filesDirector: File
        private lateinit var appContext: Context
        private lateinit var mainPresenter: MainPresenter

        private var requestSent = false

        fun updateBitmap(bitmap: Bitmap) {
            this.bitmap = bitmap
        }

        fun recognizeFace() {
            if (!Util.isInternetAvailable(appContext)) {
                Toast.makeText(appContext, R.string.no_connection, Toast.LENGTH_SHORT).show()
                return
            }

            if (!requestSent) {
                mainPresenter.recognizeFace(bitmap, filesDirector, 0f)
                requestSent = true
            }
        }

    }

    private var cameraSource: CameraSource? = null

    private var selectedModel = FACE_DETECTION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        setSupportActionBar(activity_main_toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)
        }

        appContext = applicationContext
        filesDirector = application.filesDir
        mainPresenter = MainPresenter(this, MainInteractor())

        doIfGranted(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION) {
            createCameraSource(selectedModel)
        }

        activity_main_imagebutton_switchcamera.setOnClickListener { switchCamera() }
        activity_main_imagebutton_adduser.setOnClickListener { addUser() }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        BottomNavigationViewHelper.removeShiftMode(navigation)

        activity_main_nav_view.setNavigationItemSelectedListener(drawerItemSelectedListener)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                activity_main_drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        firePreview.stop()
        when (item.itemId) {
            R.id.navigation_face -> {
                selectedModel = FACE_DETECTION
                doIfGranted(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION) {
                    createCameraSource(selectedModel)
                    startCameraSource()
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_barcode -> {
                selectedModel = BARCODE_DETECTION
                doIfGranted(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION) {
                    createCameraSource(selectedModel)
                    startCameraSource()
                }
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_image -> {
                selectedModel = IMAGE_LABEL_DETECTION
                doIfGranted(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION) {
                    createCameraSource(selectedModel)
                    startCameraSource()
                }
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_text -> {
                selectedModel = TEXT_RECOGNITION
                doIfGranted(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION) {
                    createCameraSource(selectedModel)
                    startCameraSource()
                }
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val drawerItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId){
            R.id.nav_about -> {
                activity_main_drawer_layout.closeDrawers()
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }
        false
    }

    override fun showResponse(guess: String?) {
        activity_main_progressbar.visibility = View.INVISIBLE

        if (guess == "anyone") {
            responseName = ""
        } else {
            responseName = guess!!
        }

        Handler().postDelayed(
                {
                    requestSent = false
                }, 1500
        )
    }

    override fun showError(message: String?) {
        requestSent = false
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun addUser() {
        val intent = Intent(this, LandingActivity::class.java)
        intent.putExtra(INTENT_ADD_USER, true)
        startActivity(intent)
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
                                    R.string.give_camera_permission
                            )

                            val dialog: AlertDialog = builder.create()
                            dialog.setCanceledOnTouchOutside(false)
                            dialog.show()
                        },
                        onGranted = {
                            createCameraSource(selectedModel)
                        }
                )
        }
    }

    private fun createCameraSource(model: String) {
        // If there's no existing cameraSource, create one.
        val cameraSourceCopy = cameraSource
        if (cameraSourceCopy == null) {
            cameraSource = CameraSource(this, fireFaceOverlay)
        }

        when (model) {
            FACE_DETECTION -> {
                Log.i(TAG, "Using Face Detector Processor")
                cameraSource!!.setMachineLearningFrameProcessor(FaceDetectionProcessor())
            }
            BARCODE_DETECTION -> {
                Log.i(TAG, "Using Barcode Detector Processor")
                cameraSource!!.setMachineLearningFrameProcessor(BarcodeScanningProcessor())
            }
            IMAGE_LABEL_DETECTION -> {
                Log.i(TAG, "Using Image Label Detector Processor")
                cameraSource!!.setMachineLearningFrameProcessor(ImageLabelingProcessor())
            }
            TEXT_RECOGNITION -> {
                Log.i(TAG, "Using Text Detector Processor")
                cameraSource!!.setMachineLearningFrameProcessor(TextRecognitionProcessor())
            }
            else -> Log.e(TAG, "Unknown model: $model")
        }
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
        requestSent = false
        responseName = ""
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