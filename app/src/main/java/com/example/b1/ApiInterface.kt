package com.example.b1

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {

    @GET("frames_birthday.json")
    fun getData(): Call<ApiResponse>
}