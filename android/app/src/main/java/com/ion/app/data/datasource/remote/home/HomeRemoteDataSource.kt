package com.ion.app.data.datasource.remote.home

import com.ion.app.data.dto.response.home.HomeResponseDto

interface HomeRemoteDataSource {
    suspend fun getHome(): HomeResponseDto
}