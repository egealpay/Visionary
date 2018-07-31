package com.example.ozturkse.x.api

import com.google.gson.annotations.SerializedName

data class PredictionResponse(

        @SerializedName("confidence") val confidenceLevel: Double?,

        @SerializedName("prediction") val prediction: String?

)