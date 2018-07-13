package com.example.mayank.googleplaygame.network.wallet

import com.example.mayank.myplaygame.network.Question
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Itransaction {

    @FormUrlEncoded
    @POST("payu/addPayment.php")
    fun addTransactionDetails(
            @Field("firstName") firstName: String,
            @Field("lastName") lastName: String,
            @Field("playGameName") playGameName: String,
            @Field("mobileNumber") mobileNumber: String,
            @Field("transferTo") transferTo: String,
            @Field("receivedFrom") receivedFrom: String,
            @Field("email") email: String,
            @Field("productInfo") productInfo: String,
            @Field("amount") amount: String,
            @Field("txnId") txnId: String,
            @Field("paymentId") paymentId: String,
            @Field("addedOn") addedOn: String,
            @Field("createdOn") createdOn: String,
            @Field("bankRefNumber") bankRefNumber: String,
            @Field("bankCode") bankCode: String,
            @Field("transactionType") transactionType: String,
            @Field("status") status: String): Call<Transactions>

    @FormUrlEncoded
    @POST("payu/get_question.php")
    fun getQuestion(
            @Field("mobileNumber") mobileNumber: String): Call<Question>

    @FormUrlEncoded
    @POST("payu/withdrawalPayment.php")
    fun withdrawalPayments(
            @Field("firstName") firstName: String,
            @Field("lastName") lastName: String,
            @Field("playGameName") playGameName: String,
            @Field("mobileNumber") mobileNumber: String,
            @Field("transferTo") transferTo: String,
            @Field("receivedFrom") receivedFrom: String,
            @Field("email") email: String,
            @Field("productInfo") productInfo: String,
            @Field("amount") amount: String,
            @Field("txnId") txnId: String,
            @Field("paymentId") paymentId: String,
            @Field("addedOn") addedOn: String,
            @Field("createdOn") createdOn: String,
            @Field("bankRefNumber") bankRefNumber: String,
            @Field("bankCode") bankCode: String,
            @Field("transactionType") transactionType: String,
            @Field("accountNumber") accountNumber : String,
            @Field("ifscCode") ifscCode : String,
            @Field("status") status: String): Call<Transactions>


    @FormUrlEncoded
    @POST("payu/transferPayment.php")
    fun transferPoints(
            @Field("firstName") firstName: String,
            @Field("lastName") lastName: String,
            @Field("playGameName") playGameName: String,
            @Field("mobileNumber") mobileNumber: String,
            @Field("transferTo") transferTo: String,
            @Field("receivedFrom") receivedFrom: String,
            @Field("email") email: String,
            @Field("productInfo") productInfo: String,
            @Field("amount") amount: String,
            @Field("txnId") txnId: String,
            @Field("paymentId") paymentId: String,
            @Field("addedOn") addedOn: String,
            @Field("createdOn") createdOn: String,
            @Field("bankRefNumber") bankRefNumber: String,
            @Field("bankCode") bankCode: String,
            @Field("transactionType") transactionType: String,
            @Field("status") status: String): Call<Transactions>



    @FormUrlEncoded
    @POST("payu/checkBalance.php")
    fun checkBalance(
            @Field("mobileNumber") mobileNumber: String): Call<Transactions>

    @FormUrlEncoded
    @POST("payu/addResultBalance.php")
    fun addResultBalance(
            @Field("displayName") displayName: String,
            @Field("amount") amount: Float,
            @Field("timeStamp") timeStamp: String): Call<Transactions>


    @FormUrlEncoded
    @POST("payu/subtractResultBalance.php")
    fun subtractResultBalance(
            @Field("displayName") displayName: String,
            @Field("amount") amount: Float,
            @Field("timeStamp") timeStamp: String): Call<Transactions>

    @FormUrlEncoded
    @POST("payu/insertDetails.php")
    fun addDetails(
            @Field("mobileNumber") mobileNumber: String,
            @Field("email") email: String,
            @Field("displayName") displayName: String): Call<Transactions>





}