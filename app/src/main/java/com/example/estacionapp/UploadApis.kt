package com.example.estacionapp

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApis {
    @Multipart
    @POST("reports")
    fun uploadImage(@Part part : MultipartBody.Part, @Part("user_id") requestBody : RequestBody) : Call<ResponseBody>

}