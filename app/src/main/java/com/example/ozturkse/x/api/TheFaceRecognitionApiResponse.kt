package com.example.ozturkse.x.api

import com.google.gson.annotations.SerializedName

data class TheFaceRecognitionApiResponse(

        @SerializedName("results")
        var answer: String?

)