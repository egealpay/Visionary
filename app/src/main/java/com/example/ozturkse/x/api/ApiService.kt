package com.example.ozturkse.x.api

import com.example.ozturkse.x.network.NetworkModule
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*


interface FaceRecognitionApiService {

    @Multipart
    @POST("meet")
    fun meet(
            @Part file: MultipartBody.Part,
            @Part("name") name: RequestBody
    ): Observable<TheFaceRecognitionApiResponse>

    @Multipart
    @POST("predict")
    fun predict(
            @Part file: MultipartBody.Part
    ): Observable<PredictionResponse>

    companion object {
        val baseUrl = "http://poker-face-api.herokuapp.com"
        val network = NetworkModule(baseUrl)

        fun create(): FaceRecognitionApiService {
            return network.retrofit.create(FaceRecognitionApiService::class.java)
        }
    }
}