package com.chazo.personal.two.cleanarchitecturestudy.data.google_calender

import android.content.Context
import com.chazo.personal.two.cleanarchitecturestudy.data.google_calender.remote.GoogleCalendarRemoteDataSource
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Singleton

@Module
abstract class GoogleCalendarDataModule {

    @Module
    companion object {
        @JvmStatic
        @Singleton
        @Provides
        fun provideGoogleAccountCredential(context: Context): GoogleAccountCredential {
            return GoogleAccountCredential
                .usingOAuth2(context, Arrays.asList(CalendarScopes.CALENDAR))
                .setBackOff(ExponentialBackOff())
        }

        @JvmStatic
        @Singleton
        @Provides
        fun provideGoogleCalendarRemoteDataSource(googleAccountCredential: GoogleAccountCredential): GoogleCalendarRemoteDataSource {
            val transport: HttpTransport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            return GoogleCalendarRemoteDataSource(googleAccountCredential, transport, jsonFactory)
        }
    }

    @Singleton
    @Binds
    abstract fun provideGoogleCalendarRepository(googleCalendarRepository: GoogleCalendarRepository): GoogleCalendarDataSource
}