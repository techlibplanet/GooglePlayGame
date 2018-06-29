package com.example.mayank.googleplaygame

import android.app.Application
import com.example.mayank.myplaygame.helpers.SharedPreferenceHelper

class PlayGameApplication : Application() {

    companion object {
        var sharedPrefs : SharedPreferenceHelper?= null

    }

    override fun onCreate() {
        super.onCreate()

        sharedPrefs = SharedPreferenceHelper()



    }
}