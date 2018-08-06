package com.example.ozturkse.x.ui.main

import com.example.ozturkse.x.api.PredictionResponse
import com.example.ozturkse.x.util.Util
import io.fotoapparat.photo.BitmapPhoto
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
            mainView?.showError("please try again")
    }

    override fun onErrorFaceRecognition(message: String?) {
        mainView?.showError(message)
    }

    fun recognizeFace(bitmapPhoto: BitmapPhoto, filesDir: File, angleToRotate: Float) {
        mainView?.showLoading()

        val resized = Util.compressImage(bitmapPhoto.bitmap)
        val rotatedBitmap = Util.rotateImage(resized, 360f - angleToRotate)
        val grayScaleBitmap = Util.toGrayscale(rotatedBitmap)
        val imageFile = Util.bitmapToFile(grayScaleBitmap, filesDir)

        mainInteractor.recognizeFaceRequest(this, imageFile)
    }

    fun onDestroy() {
        mainView = null
    }

}