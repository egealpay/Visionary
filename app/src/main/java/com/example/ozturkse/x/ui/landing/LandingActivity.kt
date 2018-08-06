package com.example.ozturkse.x.ui.landing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.example.ozturkse.x.R
import com.example.ozturkse.x.ui.main.MainActivity
import com.monitise.mea.android.caki.extensions.doIfGranted
import com.monitise.mea.android.caki.extensions.edit
import com.monitise.mea.android.caki.extensions.handlePermissionsResult
import kotlinx.android.synthetic.main.activity_landing.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File


class LandingActivity : AppCompatActivity(), LandingView {

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 0
        const val HAS_REGISTERED_KEY = "has_registered"
        const val INTENT_ADD_USER = "add_user"
    }

    val landingPresenter: LandingPresenter = LandingPresenter(this, LandingInteractor())

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val addUser = intent.getBooleanExtra(INTENT_ADD_USER, false)

        sharedPreferences = getPreferences(Context.MODE_PRIVATE) ?: return
        if (sharedPreferences.getBoolean(HAS_REGISTERED_KEY, false) && !addUser) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        EasyImage.configuration(this)
                .setImagesFolderName("EasyImage sample")
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(true)

        activity_landing_button_continue.setOnClickListener { openCameraActivity() }
    }

    override fun onDestroy() {
        // Clear any configuration that was done!
        EasyImage.clearConfiguration(this)
        super.onDestroy()
    }

    fun openCameraActivity() {
        if (activity_landing_edittext_fullname.text.toString() == "") {
            Toast.makeText(applicationContext, "Please specify a name", Toast.LENGTH_SHORT).show()
            return
        }

        doIfGranted(Manifest.permission.CAMERA, LandingActivity.REQUEST_CAMERA_PERMISSION) {
            EasyImage.openCameraForImage(this, 0)
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
                val file = imageFiles[0]
                val filesDir = applicationContext.filesDir
                landingPresenter.register(activity_landing_edittext_fullname.text.toString(), file, filesDir)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LandingActivity.REQUEST_CAMERA_PERMISSION -> handlePermissionsResult(permissions, grantResults,
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

    override fun showResponse(answer: String?) {
        Toast.makeText(this, answer, Toast.LENGTH_LONG).show()

        sharedPreferences.edit {
            putBoolean(HAS_REGISTERED_KEY, true)
            apply()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun showError(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showLoading() {
        activity_landing_progressbar.visibility = View.VISIBLE
        activity_landing_button_continue.visibility = View.GONE
        activity_landing_edittext_fullname.visibility = View.GONE
    }

    override fun hideLoading() {
        activity_landing_progressbar.visibility = View.GONE
        activity_landing_button_continue.visibility = View.VISIBLE
        activity_landing_edittext_fullname.visibility = View.VISIBLE
    }


}