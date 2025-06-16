package com.example.weatherapp

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") appId: String,
        @Query("units") units: String
    ): WeatherResponse
}