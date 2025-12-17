package com.ion.app.data.repositoryimpl.home

import android.util.Log
import com.ion.app.data.datasource.remote.home.HomeRemoteDataSource
import com.ion.app.data.mapper.home.toDomain
import com.ion.app.domain.model.home.HomeMainModel
import com.ion.app.domain.repository.home.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val remoteDataSource: HomeRemoteDataSource
) : HomeRepository {

    override suspend fun loadHome(): Result<HomeMainModel> =
        runCatching {
            remoteDataSource.getHome().toDomain()
        }.onFailure { e ->
            Log.e("HomeRepository", "loadHome failed", e)
        }
}