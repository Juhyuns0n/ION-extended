package com.ion.app.domain.repository.home

import com.ion.app.domain.model.home.HomeMainModel


interface HomeRepository {
    suspend fun loadHome(): Result<HomeMainModel>
}