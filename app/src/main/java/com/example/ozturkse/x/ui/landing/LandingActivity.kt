package com.example.ozturkse.x.ui.landing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.ozturkse.x.AboutActivity
import com.example.ozturkse.x.R
import com.example.ozturkse.x.ui.main.MainActivity
import com.example.ozturkse.x.util.Util
import com.monitise.mea.android.caki.extensions.doIfGranted
import com.monitise.mea.android.caki.extensions.edit
import com.monitise.mea.android.caki.extensions.handlePermissionsResult
import kotlinx.android.synthetic.main.activity_landing.*
import kotlinx.android.synthetic.main.activity_main.*
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

        setSupportActionBar(activity_landing_toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }

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

        activity_landing_nav_view.setNavigationItemSelectedListener(drawerItemSelectedListener)

        activity_landing_imagebutton_menu.setOnClickListener {
            activity_landing_drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    private val drawerItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId){
            R.id.nav_about -> {
                activity_landing_drawer_layout.closeDrawers()
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }
        false
    }

    override fun onDestroy() {
        // Clear any configuration that was done!
        EasyImage.clearConfiguration(this)
        super.onDestroy()
    }

    fun openCameraActivity() {
        if (activity_landing_edittext_fullname.text.toString().trim() == "") {
            Toast.makeText(applicationContext, R.string.enter_name, Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this@LandingActivity)
        builder.setTitle("${R.string.welcome} ${activity_landing_edittext_fullname.text}")
        builder.setMessage(getString(R.string.photo_not_stored))
        builder.setPositiveButton(getString(R.string.take_selfie)) { _, _ ->
            doIfGranted(Manifest.permission.CAMERA, LandingActivity.REQUEST_CAMERA_PERMISSION) {
                EasyImage.openCameraForImage(this, 0)
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                //Some error handling
                e!!.printStackTrace()
            }

            override fun onImagesPicked(imageFiles: List<File>, source: EasyImage.ImageSource, type: Int) {
                if (!Util.isInternetAvailable(applicationContext)) {
                    Toast.makeText(applicationContext, R.string.no_connection, Toast.LENGTH_SHORT).show()
                    return
                }

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
                        builder.setMessage(R.string.give_camera_permission)

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
        val responseText = "${R.string.hello} $answer"
        Toast.makeText(this, responseText, Toast.LENGTH_LONG).show()

        sharedPreferences.edit {
            putBoolean(HAS_REGISTERED_KEY, true)
            apply()
        }

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

    override fun onBackPressed() {
        super.onBackPressed()
        if (!sharedPreferences.getBoolean(HAS_REGISTERED_KEY, false))
            Toast.makeText(applicationContext, R.string.not_registered, Toast.LENGTH_LONG).show()
        finish()
    }

}