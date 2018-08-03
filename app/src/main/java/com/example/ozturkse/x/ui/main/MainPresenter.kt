package com.example.ozturkse.x.ui.main

import com.example.ozturkse.x.api.PredictionResponse
import com.example.ozturkse.x.util.Util
import io.fotoapparat.photo.BitmapPhoto
import java.io.File

class MainPresenter(
        var mainView: MainView?,
        val mainInteractor: MainInteractor
) : MainInteractor.onFaceRecognitionReceivedListener {
    override fun onSuccessFaceRecognition(result: PredictionResponse) {
        mainView?.showReponse(result)
    }

    override fun onErrorFaceRecognition(message: String?) {
        mainView?.showError(message)
    }

    fun recognizeFace(bitmapPhoto: BitmapPhoto, filesDir: File, angleToRotate: Float) {
        val resized = Util.compressImage(bitmapPhoto.bitmap)
        val rotatedBitmap = Util.rotateImage(resized, 360f - angleToRotate)
        val imageFile = Util.bitmapToFile(rotatedBitmap, filesDir)

        mainInteractor.recognizeFaceRequest(this, imageFile)
    }

    fun onDestroy() {
        mainView = null
    }

}