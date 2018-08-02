package com.example.ozturkse.x.ui.main

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.example.ozturkse.x.R
import com.example.ozturkse.x.api.PredictionResponse
import com.example.ozturkse.x.ui.landing.LandingActivity
import com.monitise.mea.android.caki.extensions.doIfGranted
import com.monitise.mea.android.caki.extensions.handlePermissionsResult
import io.fotoapparat.Fotoapparat
import io.fotoapparat.FotoapparatSwitcher
import io.fotoapparat.facedetector.processor.FaceDetectorProcessor
import io.fotoapparat.parameter.selector.LensPositionSelectors.back
import io.fotoapparat.parameter.selector.LensPositionSelectors.front
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MainView {

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 0
    }

    private lateinit var fotoapparatBack: Fotoapparat
    private lateinit var fotoapparatFront: Fotoapparat
    private lateinit var fotoapparatSwitcher: FotoapparatSwitcher
    private lateinit var processor: FaceDetectorProcessor

    private var hasStarted: Boolean = false
    private var requestSent = false

    val mainPresenter: MainPresenter = MainPresenter(this, MainInteractor())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createProcessor()
        createFotoApparat()
        fotoapparatSwitcher = FotoapparatSwitcher.withDefault(fotoapparatBack)  //For switching between back & front camera

        if (!hasStarted) {
            doIfGranted(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION) {
                fotoapparatSwitcher.start()
                hasStarted = true
            }
        }

        activity_main_imagebutton_switchcamera.setOnClickListener { switchCamera() }
    }

    private fun createProcessor() {
        processor = FaceDetectorProcessor.with(this)
                .listener { faces ->
                    rectanglesView.setRectangles(faces)// (Optional) Show detected faces on the view.
                    if (faces.size > 0 && hasStarted && !requestSent) {

                        requestSent = true

                        val photoResult = fotoapparatSwitcher.currentFotoapparat.takePicture()

                        activity_main_progressbar.visibility = View.VISIBLE

                        photoResult
                                .toBitmap()
                                .whenAvailable { bitmapPhoto ->
                                    val filesDir = applicationContext.filesDir
                                    mainPresenter.recognizeFace(bitmapPhoto, filesDir)
                                }
                    }
                }
                .build()

    }

    override fun showReponse(result: PredictionResponse) {
        activity_main_progressbar.visibility = View.INVISIBLE
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Result")
        builder.setMessage("${result.guess} \n ${result.confidenceLevel}")
        builder.setPositiveButton("OK") { _, _ ->
            requestSent = false
        }

        val dialog: AlertDialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun showError(message: String?) {
        activity_main_progressbar.visibility = View.INVISIBLE
        requestSent = false
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun createFotoApparat() {
        fotoapparatBack = Fotoapparat.with(this)
                .into(cameraView)
                .frameProcessor(processor)
                .lensPosition(back())
                .jpegQuality(50)
                .build()

        fotoapparatFront = Fotoapparat.with(this)
                .into(cameraView)
                .frameProcessor(processor)
                .lensPosition(front())
                .jpegQuality(50)
                .build()
    }

    fun switchCamera() {
        if (fotoapparatSwitcher.currentFotoapparat == fotoapparatFront) {
            fotoapparatSwitcher.switchTo(fotoapparatBack)
        } else {
            fotoapparatSwitcher.switchTo(fotoapparatFront)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            LandingActivity.REQUEST_CAMERA_PERMISSION ->
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
                        fotoapparatSwitcher.start()
                        hasStarted = true
                    }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        if (!hasStarted) {
            doIfGranted(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION) {
                fotoapparatSwitcher.start()
                hasStarted = true
            }
        }
    }

    override fun onStop() {
        if (hasStarted) {
            fotoapparatSwitcher.stop()
            hasStarted = false
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainPresenter.onDestroy()
    }
}