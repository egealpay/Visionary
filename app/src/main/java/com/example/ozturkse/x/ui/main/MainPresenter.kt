package com.example.ozturkse.x.ui.main

import android.graphics.Bitmap
import com.example.ozturkse.x.api.PredictionResponse
import com.example.ozturkse.x.util.Util
import java.io.File

class MainPresenter(
        var mainView: MainView?,
        val mainInteractor: MainInteractor
) : MainInteractor.OnFaceRecognitionReceivedListener {
    override fun onSuccessFaceRecognition(result: PredictionResponse) {
        mainView?.hideLoading()

        if (result.status == "found")
            mainView?.showResponse(result.guess)
        else
            mainView?.showError("I don't know who you are. Please try again")
    }

    override fun onErrorFaceRecognition(message: String?) {
        mainView?.showError(message)
    }

    fun recognizeFace(bitmap: Bitmap, filesDir: File, angleToRotate: Float) {
        mainView?.showLoading()

        val resized = Util.compressImage(bitmap)
        val rotatedBitmap = Util.rotateImage(resized, 360f - angleToRotate)
        val grayScaleBitmap = Util.toGrayscale(rotatedBitmap)
        val imageFile = Util.bitmapToFile(grayScaleBitmap, filesDir)

        mainInteractor.recognizeFaceRequest(this, imageFile)
    }

    fun onDestroy() {
        mainView = null
    }

}