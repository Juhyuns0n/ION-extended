package com.ion.app.data.datasourceimpl.remote.home

import com.ion.app.data.datasource.remote.home.HomeRemoteDataSource
import com.ion.app.data.dto.response.home.FailMainResponseDto
import com.ion.app.data.dto.response.home.HomeResponseDto
import com.ion.app.data.service.home.HomeService
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import javax.inject.Inject

class HomeRemoteDataSourceImpl @Inject constructor(
    private val service: HomeService
) : HomeRemoteDataSource {

    override suspend fun getHome(): HomeResponseDto {
        val response = service.getHome()

        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("메인 정보를 불러올 수 없습니다.")
        } else {
            val msg = parseError(response.errorBody())
            throw IllegalStateException(msg)
        }
    }

    private fun parseError(errorBody: ResponseBody?): String {
        if (errorBody == null) return "메인 정보를 불러오지 못했습니다."

        return try {
            val str = errorBody.string()
            val parsed = Json.decodeFromString<FailMainResponseDto>(str)
            parsed.msg
        } catch (e: Exception) {
            "메인 정보를 불러오지 못했습니다."
        }
    }
}