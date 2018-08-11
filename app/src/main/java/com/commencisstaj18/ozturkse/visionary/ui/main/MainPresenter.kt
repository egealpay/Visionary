package com.commencisstaj18.ozturkse.visionary.ui.main

import android.graphics.Bitmap
import com.commencisstaj18.ozturkse.visionary.api.PredictionResponse
import com.commencisstaj18.ozturkse.visionary.util.Util
import java.io.File

class MainPresenter(
        var mainView: MainView?,
        val mainInteractor: MainInteractor
) : MainInteractor.OnFaceRecognitionReceivedListener {
    override fun onSuccessFaceRecognition(result: PredictionResponse) {
        mainView?.hideLoading()
        mainView?.showResponse(result.guess)
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