package com.example.test

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://s7om3fdgbt7lcvqdnxitjmtiim0uczux.lambda-url.us-east-2.on.aws/"


    val loggingInterceptor = HttpLoggingInterceptor()

    val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor)
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .build()



    val apiService: TestApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TestApi::class.java)
    }
}