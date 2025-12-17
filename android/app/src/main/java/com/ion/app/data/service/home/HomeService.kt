package com.ion.app.data.service.home

import com.ion.app.data.dto.response.home.HomeResponseDto
import retrofit2.Response
import retrofit2.http.GET


interface HomeService {
    @GET("api/home")
    suspend fun getHome(): Response<HomeResponseDto>
}