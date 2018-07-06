package com.example.mayank.googleplaygame

import android.util.Log

object Constants {

    val PLAYER_ID = "PlayerId"
    val DISPLAY_NAME = "DisplayName"
    val FIRST_NAME = "FirstName"
    val LAST_NAME = "LastName"
    val EMAIL = "Email"
    val MOBILE_NUMBER = "MobileNumber"
    val PRODUCT_RECHARGE_POINTS = "Recharge Points"
    val ACCOUNT_NUMBER = "AccountNumber"
    val IFSC_CODE= "IfscCode"

    val MERCHANT_ID = "4873218"
    val MERCHANT_KEY = "HqRplS"
    val surl = "https://www.payumoney.com/mobileapp/payumoney/success.php"
    val furl = "https://www.payumoney.com/mobileapp/payumoney/failure.php"
    val URL = "http://alchemyeducation.org/payu/getHashCode.php"
    val WITHDRAWAL = "Withdrawal"


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