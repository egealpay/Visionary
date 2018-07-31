package com.example.ozturkse.x.ui

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.ozturkse.x.R
import com.example.ozturkse.x.api.FaceRecognitionApiService
import com.monitise.mea.android.caki.extensions.doIfGranted
import com.monitise.mea.android.caki.extensions.handlePermissionsResult
import io.fotoapparat.Fotoapparat
import io.fotoapparat.FotoapparatSwitcher
import io.fotoapparat.facedetector.processor.FaceDetectorProcessor
import io.fotoapparat.parameter.selector.LensPositionSelectors.back
import io.fotoapparat.parameter.selector.LensPositionSelectors.front
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 0
    }

    private lateinit var fotoapparatBack: Fotoapparat
    private lateinit var fotoapparatFront: Fotoapparat
    private lateinit var fotoapparatSwitcher: FotoapparatSwitcher
    private lateinit var processor: FaceDetectorProcessor

    private var hasStarted: Boolean = false

    private var disposable: Disposable? = null

    private val apiService by lazy {
        FaceRecognitionApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createProcessor()
        createFotoApparat()
        fotoapparatSwitcher = FotoapparatSwitcher.withDefault(fotoapparatBack)  //For switching between back & front camera

        doIfGranted(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION){
            fotoapparatSwitcher.start()
            hasStarted = true
        }

        activity_main_imagebutton_switchcamera.setOnClickListener { switchCamera() }
    }

    private fun createProcessor() {
        processor = FaceDetectorProcessor.with(this)
                .listener { faces ->
                    rectanglesView.setRectangles(faces)// (Optional) Show detected faces on the view.
                    if (faces.size > 0) {

                        val photoResult = fotoapparatSwitcher.currentFotoapparat.takePicture()
                        photoResult
                                .toBitmap()
                                .whenAvailable { bitmapPhoto ->
                                    val stream = ByteArrayOutputStream()
                                    bitmapPhoto.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                                    val byteArray = stream.toByteArray()

                                    val body: RequestBody = RequestBody.create(
                                            MediaType.parse("application/octet-stream"),
                                            byteArray
                                    )

                                    disposable = apiService.predict(body)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                    { result ->
                                                        Toast.makeText(this, result.prediction, Toast.LENGTH_LONG).show()
                                                    },
                                                    { error ->
                                                        Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                                                    }
                                            )
                                }
                    }
                }
                .build()

    }


    private fun createFotoApparat() {
        fotoapparatBack = Fotoapparat.with(this)
                .into(cameraView)
                .frameProcessor(processor)
                .lensPosition(back())
                .build()

        fotoapparatFront = Fotoapparat.with(this)
                .into(cameraView)
                .frameProcessor(processor)
                .lensPosition(front())
                .build()
    }

    fun switchCamera() {
        if (fotoapparatSwitcher.currentFotoapparat == fotoapparatFront) {
            fotoapparatSwitcher.switchTo(fotoapparatBack)
        } else {
            fotoapparatSwitcher.switchTo(fotoapparatFront)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LandingActivity.REQUEST_CAMERA_PERMISSION -> handlePermissionsResult(permissions, grantResults,
                    onDenied = { val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle(":(")
                        builder.setMessage("In order to use this app, you should give permission to use camera, from Settings!")


                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    },
                    onGranted = {
                        fotoapparatSwitcher.start()
                        hasStarted = true
                    }
            )
        }
    }


    override fun onStop() {
        if(hasStarted)
            fotoapparatSwitcher.stop()
        super.onStop()
    }
}