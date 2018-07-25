package com.example.ozturkse.x.api

import com.example.ozturkse.x.network.NetworkModule


interface FaceRecognitionApiService {

    companion object {
        val baseUrl = "https://api"
        val network = NetworkModule(baseUrl)

        fun create(): FaceRecognitionApiService {
            return network.retrofit.create(FaceRecognitionApiService::class.java)
        }
    }
}