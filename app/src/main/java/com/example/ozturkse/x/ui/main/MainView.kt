package com.example.ozturkse.x.ui.main

import com.example.ozturkse.x.api.PredictionResponse

interface MainView {
    fun showReponse(result: PredictionResponse)
    fun showError(message: String?)
}