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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import android.graphics.Bitmap.CompressFormat
import android.R.attr.bitmap
import android.view.View
import io.fotoapparat.photo.BitmapPhoto
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
                    if (faces.size > 0 && hasStarted) {

                        val photoResult = fotoapparatSwitcher.currentFotoapparat.takePicture()

                        fotoapparatSwitcher.stop()
                        hasStarted = false
                        activity_main_progressbar.visibility = View.VISIBLE
                        activity_main_cameraoverlaylayout.visibility = View.GONE
                        activity_main_imagebutton_switchcamera.isClickable = false

                        photoResult
                                .toBitmap()
                                .whenAvailable { bitmapPhoto ->

                                    val aspectRatio = bitmapPhoto.bitmap.width /  bitmapPhoto.bitmap.height.toFloat()
                                    val width = 480
                                    val height = Math.round(width / aspectRatio)
                                    val resized = Bitmap.createScaledBitmap(bitmapPhoto.bitmap, width, height, true)

                                    val filesDir = applicationContext.filesDir
                                    val imageFile = File(filesDir, "rectangleface.jpg")
                                    imageFile.createNewFile()

                                    val bos = ByteArrayOutputStream()
                                    resized.compress(CompressFormat.JPEG, 50 /*ignored for PNG*/, bos)
                                    val bitmapdata = bos.toByteArray()

                                    val fos = FileOutputStream(imageFile)
                                    fos.write(bitmapdata)
                                    fos.flush()
                                    fos.close()

                                    val photo = MultipartBody.Part.createFormData(
                                            "photo",
                                            imageFile.name,
                                            RequestBody.create(MediaType.parse("image/*"), imageFile)
                                    )

                                    disposable = apiService.predict(photo)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                    { result ->
                                                        // Toast.makeText(this, result.prediction, Toast.LENGTH_LONG).show()

                                                        activity_main_progressbar.visibility = View.GONE
                                                        val builder = AlertDialog.Builder(this@MainActivity)
                                                        builder.setTitle("Result")
                                                        builder.setMessage("${result.guess} \n ${result.confidenceLevel}")
                                                        builder.setPositiveButton("OK") { _, _ ->
                                                            activity_main_cameraoverlaylayout.visibility = View.VISIBLE
                                                            activity_main_imagebutton_switchcamera.isClickable = true
                                                            if (!hasStarted) {
                                                                fotoapparatSwitcher.start()
                                                                hasStarted = true
                                                            }
                                                        }

                                                        val dialog: AlertDialog = builder.create()
                                                        dialog.setCanceledOnTouchOutside(false)
                                                        dialog.show()
                                                    },
                                                    { error ->
                                                        activity_main_progressbar.visibility = View.GONE
                                                        activity_main_cameraoverlaylayout.visibility = View.VISIBLE
                                                        activity_main_imagebutton_switchcamera.isClickable = true
                                                        if (!hasStarted) {
                                                            fotoapparatSwitcher.start()
                                                            hasStarted = true
                                                        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LandingActivity.REQUEST_CAMERA_PERMISSION -> handlePermissionsResult(permissions, grantResults,
                    onDenied = {
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle(":(")
                        builder.setMessage("In order to use this app, you should give permission to use camera, from Settings!")


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
}