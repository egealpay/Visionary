package com.example.ozturkse.x.api

import com.google.gson.annotations.SerializedName

data class PredictionResponse(

        @SerializedName("status") val confidenceLevel: Double?,

        @SerializedName("guess") val prediction: String?

)