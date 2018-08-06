package com.example.ozturkse.x.ui.landing

import android.graphics.BitmapFactory
import com.example.ozturkse.x.util.Util
import java.io.File

class LandingPresenter(
        var landingView: LandingView?,
        val landingInteractor: LandingInteractor
) : LandingInteractor.OnRegisterReceivedListener {
    override fun onSuccessRegister(answer: String?) {
        landingView?.showResponse(answer)
    }

    override fun onErrorRegister(error: String?) {
        landingView?.hideLoading()
        landingView?.showError(error)
    }

    fun register(fullname: String, file: File, filesDir: File) {
        landingView?.showLoading()

        val angleToRotate = Util.getCameraPhotoOrientation(file.absolutePath)
        val bitmap = BitmapFactory.decodeFile(file.path)

        val resized = Util.compressImage(bitmap)
        val rotatedBitmap = Util.rotateImage(resized, angleToRotate.toFloat())
        val grayScaleBitmap = Util.toGrayscale(rotatedBitmap)
        val imageFile = Util.bitmapToFile(grayScaleBitmap, filesDir)

        landingInteractor.registerRequest(this, fullname, imageFile)
    }

}