package com.commencisstaj18.ozturkse.visionary.api

import com.google.gson.annotations.SerializedName

data class PredictionResponse(

        @SerializedName("guess")
        val guess: String?,

        @SerializedName("status")
        val status: String?

)