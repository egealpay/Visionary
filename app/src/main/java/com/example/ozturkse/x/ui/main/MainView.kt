package com.example.ozturkse.x.ui.main

interface MainView {
    fun showResponse(guess: String?)
    fun showError(message: String?)
    fun showLoading()
    fun hideLoading()
}