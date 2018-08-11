package com.commencisstaj18.ozturkse.visionary.ui.landing

interface LandingView{
    fun showResponse(answer: String?)
    fun showError(message: String?)
    fun showLoading()
    fun hideLoading()
}