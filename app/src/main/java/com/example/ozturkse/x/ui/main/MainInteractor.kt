package com.example.ozturkse.x.ui.main

import com.example.ozturkse.x.api.FaceRecognitionApiService
import com.example.ozturkse.x.api.PredictionResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class MainInteractor {

    interface OnFaceRecognitionReceivedListener {
        fun onSuccessFaceRecognition(result: PredictionResponse)
        fun onErrorFaceRecognition(message: String?)
    }

    private var disposable: Disposable? = null

    private val apiService by lazy {
        FaceRecognitionApiService.create()
    }

    fun recognizeFaceRequest(listener: OnFaceRecognitionReceivedListener, imageFile: File) {
        val photo = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                RequestBody.create(MediaType.parse("image/*"), imageFile)
        )

        disposable = apiService.predict(photo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            listener.onSuccessFaceRecognition(result)
                        },
                        { error ->
                            listener.onErrorFaceRecognition(error.message)
                        }
                )
    }
}