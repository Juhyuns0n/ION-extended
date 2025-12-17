package com.ion.app.data.datasource.remote.auth

import com.ion.app.data.dto.request.auth.MembershipRequestDto
import com.ion.app.data.dto.response.auth.MembershipResponseDto
import com.ion.app.data.dto.response.auth.MembershipResultResponseDto
import com.ion.app.data.dto.response.auth.PropensityTestResponseDto
import okhttp3.MultipartBody

interface SignUpRemoteDataSource {
    suspend fun getPropensityTests(): List<PropensityTestResponseDto>
    suspend fun getTestResult(userId: String): MembershipResultResponseDto
    suspend fun registerMembership(
        request: MembershipRequestDto,
        userImage: MultipartBody.Part? = null
    ): MembershipResponseDto
}