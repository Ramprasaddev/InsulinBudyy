package com.simats.insulinbuddy

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("signup.php")
    fun signup(@Body request: SignUpRequest): Call<SignUpResponse>
}
