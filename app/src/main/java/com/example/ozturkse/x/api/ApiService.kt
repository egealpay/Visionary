package com.example.ozturkse.x.api

import com.example.ozturkse.x.network.NetworkModule


interface FaceRecognitionApiService {

    companion object {
        fun create(): FaceRecognitionApiService {
            val baseUrl = "https://api.themoviedb.org/3/"
            val network = NetworkModule(baseUrl)
            return network.retrofit.create(FaceRecognitionApiService::class.java)
        }
    }
}