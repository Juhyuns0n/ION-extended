package com.ion.app.data.di

import com.ion.app.data.datasource.local.voicereport.VoiceReportLocalDataSource
import com.ion.app.data.datasource.remote.auth.AuthRemoteDataSource
import com.ion.app.data.datasource.remote.auth.SignUpRemoteDataSource
import com.ion.app.data.datasource.remote.chatbot.ChatbotRemoteDataSource
import com.ion.app.data.datasource.remote.home.HomeRemoteDataSource
import com.ion.app.data.datasource.remote.voicereport.VoiceReportRemoteDataSource
import com.ion.app.data.datasource.remote.workbook.WorkbookRemoteDataSource
import com.ion.app.data.datasourceimpl.local.VoiceReportLocalDataSourceImpl
import com.ion.app.data.datasourceimpl.remote.auth.AuthRemoteDataSourceImpl
import com.ion.app.data.datasourceimpl.remote.auth.SignUpRemoteDataSourceImpl
import com.ion.app.data.datasourceimpl.remote.chatbot.ChatbotRemoteDataSourceImpl
import com.ion.app.data.datasourceimpl.remote.home.HomeRemoteDataSourceImpl
import com.ion.app.data.datasourceimpl.remote.voicereport.VoiceReportRemoteDataSourceImpl
import com.ion.app.data.datasourceimpl.remote.workbook.WorkbookRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun signUpRemoteDataSource(impl: SignUpRemoteDataSourceImpl): SignUpRemoteDataSource

    @Binds
    @Singleton
    abstract fun authRemoteDataSource(impl: AuthRemoteDataSourceImpl): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindVoiceReportRemoteDataSource(impl: VoiceReportRemoteDataSourceImpl): VoiceReportRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindWorkbookRemoteDataSource(impl: WorkbookRemoteDataSourceImpl): WorkbookRemoteDataSource

    @Binds
    abstract fun bindVoiceReportLocalDataSource(
        impl: VoiceReportLocalDataSourceImpl
    ): VoiceReportLocalDataSource

    @Binds
    @Singleton
    abstract fun bindChatbotRemoteDataSource(
        impl: ChatbotRemoteDataSourceImpl
    ): ChatbotRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindHomeRemoteDataSource(
        impl: HomeRemoteDataSourceImpl
    ): HomeRemoteDataSource


}
