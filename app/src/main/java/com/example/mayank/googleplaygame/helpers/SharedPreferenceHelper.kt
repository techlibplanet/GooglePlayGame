package com.example.mayank.myplaygame.helpers

import android.content.Context
import android.preference.PreferenceManager
import android.content.SharedPreferences
import android.text.TextUtils





class SharedPreferenceHelper {


    fun getStringPreference(context: Context, key : String): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (preferences != null) {
            return preferences.getString(key, null);
        }
        return null
    }

    fun setStringPreference(context: Context, key: String, value: String): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (preferences != null && !TextUtils.isEmpty(key)) {
            val editor = preferences.edit()
            editor.putString(key, value)
            return editor.commit()
        }
        return false
    }

    fun deletePreferences(context: Context): Boolean? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (preferences!=null) {
            return preferences.edit().clear().commit()
        }else {
            return null
        }
    }

}