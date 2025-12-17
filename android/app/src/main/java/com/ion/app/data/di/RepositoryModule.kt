package com.ion.app.data.di

import com.ion.app.data.repositoryimpl.auth.AuthRepositoryImpl
import com.ion.app.data.repositoryimpl.auth.SignUpRepositoryImpl
import com.ion.app.data.repositoryimpl.auth.TokenRepositoryImpl
import com.ion.app.data.repositoryimpl.chatbot.ChatRepositoryImpl
import com.ion.app.data.repositoryimpl.home.HomeRepositoryImpl
import com.ion.app.data.repositoryimpl.voicereport.VoiceReportRepositoryImpl
import com.ion.app.data.repositoryimpl.workbook.WorkbookRepositoryImpl
import com.ion.app.domain.repository.auth.AuthRepository
import com.ion.app.domain.repository.auth.SignUpRepository
import com.ion.app.domain.repository.auth.TokenRepository
import com.ion.app.domain.repository.voicereport.VoiceReportRepository
import com.ion.app.domain.repository.workbook.WorkbookRepository
import com.ion.app.domain.repository.chatbot.ChatRepository
import com.ion.app.domain.repository.home.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun signUpRepository(signUpRepositoryImpl: SignUpRepositoryImpl): SignUpRepository

    @Binds
    @Singleton
    abstract fun authRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindVoiceReportRepository(impl: VoiceReportRepositoryImpl): VoiceReportRepository

    @Binds
    @Singleton
    abstract fun bindWorkbookRepository(impl: WorkbookRepositoryImpl): WorkbookRepository

    @Binds
    @Singleton
    abstract fun bindChatbotRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun homeRepository(impl: HomeRepositoryImpl): HomeRepository

    @Binds
    @Singleton
    abstract fun bindTokenRepository(
        impl: TokenRepositoryImpl
    ): TokenRepository

}
