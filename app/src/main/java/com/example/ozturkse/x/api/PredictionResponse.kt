package com.example.ozturkse.x.api

import com.google.gson.annotations.SerializedName

data class PredictionResponse(

        @SerializedName("guess")
        val guess: String?,

        @SerializedName("status")
        val status: String?

)