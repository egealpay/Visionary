package com.example.ozturkse.x.ui.landing

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.example.ozturkse.x.R
import com.example.ozturkse.x.ui.adapter.ImagesAdapter
import com.example.ozturkse.x.ui.main.MainActivity
import com.example.ozturkse.x.util.Util
import com.monitise.mea.android.caki.extensions.doIfGranted
import com.monitise.mea.android.caki.extensions.handlePermissionsResult
import kotlinx.android.synthetic.main.activity_landing.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*


class LandingActivity : AppCompatActivity(), LandingView {

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 0
        const val DEFAULT_SAMPLE_PREF = false
        private val PHOTOS_KEY = "easy_image_photos_list"
    }

    val landingPresenter: LandingPresenter = LandingPresenter(this, LandingInteractor())

    private var imagesAdapter: ImagesAdapter? = null

    private var photos = ArrayList<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

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
                        dialog.setCanceledOnTouchOutside(false)
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

    override fun showResponse(answer: String?) {
        Toast.makeText(this, answer, Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun showError(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        activity_landing_linearlayout.visibility = View.VISIBLE
        activity_landing_progressbar.visibility = View.GONE
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
        if (activity_landing_edittext_fullname.text.toString() == "")
            Toast.makeText(applicationContext, "You should specify a name!", Toast.LENGTH_SHORT).show()
        else if (photos.size == 0)
            Toast.makeText(applicationContext, "You should choose photo", Toast.LENGTH_SHORT).show()
        else
            meet()
    }

    fun meet() {
        activity_landing_progressbar.visibility = View.VISIBLE
        activity_landing_linearlayout.visibility = View.GONE

        val file = photos[0]
        val angleToRotate = Util.getCameraPhotoOrientation(file.absolutePath)

        val bitmap = BitmapFactory.decodeFile(file.path)
        val filesDir = applicationContext.filesDir

        landingPresenter.register(activity_landing_edittext_fullname.text.toString(), bitmap, filesDir, angleToRotate.toFloat())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}