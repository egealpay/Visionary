package com.example.ozturkse.x.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.example.ozturkse.x.R
import com.example.ozturkse.x.api.FaceRecognitionApiService
import com.example.ozturkse.x.ui.adapter.ImagesAdapter
import com.monitise.mea.android.caki.delegates.SharedPrefDelegate
import com.monitise.mea.android.caki.extensions.doIfGranted
import com.monitise.mea.android.caki.extensions.handlePermissionsResult
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_landing.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class LandingActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 0
        const val KEY_SAMPLE_PREF = "hasRegistered"
        const val DEFAULT_SAMPLE_PREF = false
        private val PHOTOS_KEY = "easy_image_photos_list"
    }

    private val apiService by lazy {
        FaceRecognitionApiService.create()
    }

    private var samplePrefDelegate by SharedPrefDelegate(this, KEY_SAMPLE_PREF, DEFAULT_SAMPLE_PREF)

    private var imagesAdapter: ImagesAdapter? = null

    private var photos = ArrayList<File>()

    private var disposable: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        if (samplePrefDelegate == true) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        if (savedInstanceState != null) {
            photos = savedInstanceState.getSerializable(PHOTOS_KEY) as ArrayList<File>
        }

        imagesAdapter = ImagesAdapter(this, photos)
        activity_landing_recycler_view.layoutManager = LinearLayoutManager(this)
        activity_landing_recycler_view.setHasFixedSize(true)
        activity_landing_recycler_view.adapter = imagesAdapter

        EasyImage.configuration(this)
                .setImagesFolderName("EasyImage sample")
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(true)

        activity_landing_imagebutton_gallery.setOnClickListener { choosePhoto() }
        activity_landing_imagebutton_camera.setOnClickListener { takePhoto() }
        activity_landing_button_register.setOnClickListener { register() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(PHOTOS_KEY, photos)
    }

    override fun onDestroy() {
        // Clear any configuration that was done!
        EasyImage.clearConfiguration(this)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> handlePermissionsResult(permissions, grantResults,
                    onDenied = {
                        val builder = AlertDialog.Builder(this@LandingActivity)
                        builder.setTitle(":(")
                        builder.setMessage("In order to use this app, you should give permission, from Settings!")

                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    },
                    onGranted = {
                        EasyImage.openCameraForImage(this, 0)
                    }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                //Some error handling
                e!!.printStackTrace()
            }

            override fun onImagesPicked(imageFiles: List<File>, source: EasyImage.ImageSource, type: Int) {
                onPhotosReturned(imageFiles)
            }

            override fun onCanceled(source: EasyImage.ImageSource?, type: Int) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA_IMAGE) {
                    val photoFile = EasyImage.lastlyTakenButCanceledPhoto(applicationContext)
                    photoFile?.delete()
                }
            }
        })
    }

    private fun onPhotosReturned(returnedPhotos: List<File>) {
        photos.addAll(returnedPhotos)
        imagesAdapter?.notifyDataSetChanged()
        activity_landing_recycler_view.scrollToPosition(photos.size - 1)
    }

    fun choosePhoto() {
        EasyImage.openGallery(this, 0)
    }

    fun takePhoto() {
        doIfGranted(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION) {
            EasyImage.openCameraForImage(this, 0)
        }
    }

    fun register() {

        if (activity_landing_edittext_fullname.text.toString() != "" && photos.size > 0)
            meet()
    }

    fun meet() {
        val file = photos[0]

        val photo = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                RequestBody.create(MediaType.parse("image/*"), file)
        )

        val name = RequestBody.create(
                MediaType.parse("text/plain"),
                activity_landing_edittext_fullname.text.toString()
        )

        disposable = apiService.meet(photo, name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            Toast.makeText(this, result.answer, Toast.LENGTH_LONG).show()
                            samplePrefDelegate = true
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        },
                        { error ->
                            Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                        }
                )
    }

}