package com.commencisstaj18.ozturkse.visionary.ui.main

interface MainView {
    fun showResponse(guess: String?)
    fun showError(message: String?)
    fun showLoading()
    fun hideLoading()
}