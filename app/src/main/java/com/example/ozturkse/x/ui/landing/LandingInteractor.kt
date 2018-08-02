package com.example.ozturkse.x.ui.landing

import com.example.ozturkse.x.api.FaceRecognitionApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class LandingInteractor {

    interface onRegisterReceivedListener {
        fun onSuccessRegister(answer: String?)
        fun onErrorRegister(error: String?)
    }

    private var disposable: Disposable? = null

    private val apiService by lazy {
        FaceRecognitionApiService.create()
    }

    fun registerRequest(listener: onRegisterReceivedListener, fullname: String, imageFile: File) {
        val photo = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                RequestBody.create(MediaType.parse("image/*"), imageFile)
        )

        val name = RequestBody.create(
                MediaType.parse("text/plain"),
                fullname
        )

        disposable = apiService.meet(photo, name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            listener.onSuccessRegister(result.answer)
                        },
                        { error ->
                            listener.onErrorRegister(error.message)
                        }
                )
    }

}