package com.example.test

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TestApi {

    @POST(".")
    suspend fun publishIp(@Body ipAddress: TestNetworkDataModel): Response<TestNewtorkResponseModel>

}