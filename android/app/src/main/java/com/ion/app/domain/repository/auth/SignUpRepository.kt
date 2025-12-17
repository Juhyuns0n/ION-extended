package com.ion.app.domain.repository.auth

import com.ion.app.domain.model.auth.MembershipRequestModel
import com.ion.app.domain.model.auth.MembershipResponseModel
import com.ion.app.domain.model.auth.MembershipResultModel
import com.ion.app.domain.model.auth.PropensityQuestionModel
import okhttp3.MultipartBody

interface SignUpRepository {
    suspend fun getPropensityTests(): Result<List<PropensityQuestionModel>>
    suspend fun getTestResult(userId: String): Result<MembershipResultModel>
    suspend fun signUp(
        request: MembershipRequestModel,
        userImage: MultipartBody.Part? = null
    ): Result<MembershipResponseModel>
}