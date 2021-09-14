package com.example.vodamanewakala.network

import com.example.vodamanewakala.db.*
import retrofit2.Call
import retrofit2.http.GET

interface RetroService {

    @GET("zoneonewakala")
   fun getDataWakala():Call<List<Wakala>>

    @GET("zoneonewakalamkuu")
    fun getDataWakalaMkuu():Call<List<WakalaMkuu>>

    @GET("testbalance")
    fun getDataBalance():Call<List<Balance>>

    @GET("testfloatout")
    fun getDataFloatOut():Call<List<FloatOut>>

    @GET("testfloatin")
    fun getDataFloatIn():Call<List<FloatIn>>
}