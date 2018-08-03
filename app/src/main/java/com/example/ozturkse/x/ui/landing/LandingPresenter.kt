package com.example.ozturkse.x.ui.landing

import android.graphics.Bitmap
import com.example.ozturkse.x.util.Util
import java.io.File

class LandingPresenter(
        var landingView: LandingView?,
        val landingInteractor: LandingInteractor
) : LandingInteractor.onRegisterReceivedListener {
    override fun onSuccessRegister(answer: String?) {
        landingView?.showResponse(answer)
    }

    override fun onErrorRegister(error: String?) {
        landingView?.showError(error)
    }

    fun register(fullname: String, bitmap: Bitmap, filesDir: File, angleToRotate: Float) {
        val resized = Util.compressImage(bitmap)
        val rotatedBitmap = Util.rotateImage(resized, angleToRotate)
        val imageFile = Util.bitmapToFile(rotatedBitmap, filesDir)

        landingInteractor.registerRequest(this, fullname, imageFile)
    }

}