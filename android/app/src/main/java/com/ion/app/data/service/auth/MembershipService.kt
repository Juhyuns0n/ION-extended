package com.ion.app.data.service.auth

import com.ion.app.data.dto.request.auth.LoginRequestDto
import com.ion.app.data.dto.response.auth.LoginResponseDto
import com.ion.app.data.dto.response.auth.MembershipResponseDto
import com.ion.app.data.dto.response.auth.MembershipResultResponseDto
import com.ion.app.data.dto.response.auth.PropensityTestResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface MembershipService {
    @GET("/api/membership")
    suspend fun getPropensityTests(): List<PropensityTestResponseDto>

    @GET("/api/membership/test-result/{userId}")
    suspend fun getTestResult(
        @Path("userId") userId: String
    ): MembershipResultResponseDto

    @Multipart
    @POST("/api/membership")
    suspend fun registerMembership(
        @Part("user") userJson: RequestBody,
        @Part userImage: MultipartBody.Part? = null
    ): MembershipResponseDto

    @POST("api/membership/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto
}