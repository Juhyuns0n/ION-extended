package com.ion.app.data.datasourceimpl.remote.auth

import android.content.Context
import com.ion.app.R
import com.ion.app.data.datasource.remote.auth.SignUpRemoteDataSource
import com.ion.app.data.dto.request.auth.MembershipRequestDto
import com.ion.app.data.dto.response.auth.MembershipResponseDto
import com.ion.app.data.dto.response.auth.MembershipResultResponseDto
import com.ion.app.data.dto.response.auth.PropensityTestResponseDto
import com.ion.app.data.service.auth.MembershipService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class SignUpRemoteDataSourceImpl @Inject constructor(
    private val service: MembershipService,
    @ApplicationContext private val context: Context
) : SignUpRemoteDataSource {

    override suspend fun getPropensityTests(): List<PropensityTestResponseDto> =
        service.getPropensityTests()

    override suspend fun getTestResult(userId: String): MembershipResultResponseDto =
        service.getTestResult(userId)

    override suspend fun registerMembership(
        request: MembershipRequestDto,
        userImage: MultipartBody.Part?
    ): MembershipResponseDto {
        val jsonString = Json.encodeToString(request)

        val prettyJson = kotlinx.serialization.json.Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }.encodeToString(request)

        android.util.Log.d("SignUpRemote", "===== /api/membership user JSON =====\n$prettyJson")

        val requestBody = jsonString.toRequestBody(
            "application/json; charset=utf-8".toMediaType()
        )

        val finalUserImage = userImage ?: createDefaultUserImagePart()

        return service.registerMembership(
            userJson = requestBody,
            userImage = finalUserImage
        )
    }

    // 기본 프로필 이미지를 user_image 파트로
    private fun createDefaultUserImagePart(): MultipartBody.Part {
        val inputStream = context.resources.openRawResource(R.raw.ic_default_profile)
        val bytes = inputStream.use { it.readBytes() }

        val requestBody = bytes.toRequestBody("image/png".toMediaType())

        return MultipartBody.Part.createFormData(
            name = "user_image",
            filename = "default_profile.png",
            body = requestBody
        )
    }
}
