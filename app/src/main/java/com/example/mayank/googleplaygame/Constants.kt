package com.example.mayank.googleplaygame

import android.util.Log

object Constants {

    val PLAYER_ID = "PlayerId"
    val DISPLAY_NAME = "DisplayName"

    fun logD(tag: String, message: String){
        Log.d(tag, message)
    }

    fun logE(tag: String, message: String){
        Log.d(tag, message)
    }

    // Api
    const val API_END_POINT = "http://www.alchemyeducation.org/"

    const val CONNECTION_TIMEOUT: Long = 60
    const val READ_TIMEOUT: Long = 60
}