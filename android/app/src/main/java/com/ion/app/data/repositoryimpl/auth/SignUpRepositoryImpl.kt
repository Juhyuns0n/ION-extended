package com.ion.app.data.repositoryimpl.auth

import android.util.Log
import com.ion.app.data.datasource.remote.auth.SignUpRemoteDataSource
import com.ion.app.data.datasource.remote.workbook.WorkbookRemoteDataSource
import com.ion.app.data.mapper.auth.toDomain
import com.ion.app.data.mapper.auth.toDomainList
import com.ion.app.data.mapper.auth.toDto
import com.ion.app.domain.model.auth.MembershipRequestModel
import com.ion.app.domain.model.auth.MembershipResponseModel
import com.ion.app.domain.model.auth.MembershipResultModel
import com.ion.app.domain.model.auth.PropensityQuestionModel
import com.ion.app.domain.repository.auth.SignUpRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class SignUpRepositoryImpl @Inject constructor(
    private val remoteDataSource: SignUpRemoteDataSource
) : SignUpRepository {

    override suspend fun getPropensityTests(): Result<List<PropensityQuestionModel>> =
        runCatching {
            remoteDataSource.getPropensityTests().toDomainList()
        }
    override suspend fun getTestResult(userId: String): Result<MembershipResultModel> =
        runCatching {
            remoteDataSource.getTestResult(userId).toDomain()
        }
    override suspend fun signUp(
        request: MembershipRequestModel,
        userImage: MultipartBody.Part?
    ): Result<MembershipResponseModel> =
        runCatching {
            Log.d("SignUpRepository", "signUp() called")
            remoteDataSource.registerMembership(
                request = request.toDto(),
                userImage = userImage
            ).toDomain()
        }
}