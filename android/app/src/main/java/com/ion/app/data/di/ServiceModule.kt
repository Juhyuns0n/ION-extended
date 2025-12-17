package com.ion.app.data.di

import com.ion.app.data.service.auth.MembershipService
import com.ion.app.data.service.chatbot.ChatbotService
import com.ion.app.data.service.home.HomeService
import com.ion.app.data.service.voicereport.VoiceReportService
import com.ion.app.data.service.workbook.WorkbookService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun providesMembershipService(
        retrofit: Retrofit
    ): MembershipService = retrofit.create(MembershipService::class.java)

    @Provides
    @Singleton
    fun provideVoiceReportService(
        retrofit: Retrofit
    ): VoiceReportService = retrofit.create(VoiceReportService::class.java)

    @Provides
    @Singleton
    fun provideWorkbookService(
        retrofit: Retrofit
    ): WorkbookService = retrofit.create(WorkbookService::class.java)

    @Provides
    @Singleton
    fun provideChatbotService(
        retrofit: Retrofit
    ): ChatbotService = retrofit.create(ChatbotService::class.java)

    @Provides
    @Singleton
    fun provideHomeService(
        retrofit: Retrofit
    ): HomeService = retrofit.create(HomeService::class.java)
}
