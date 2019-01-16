package com.chazo.personal.two.cleanarchitecturestudy.data.google_calender.remote

import com.chazo.personal.two.cleanarchitecturestudy.data.google_calender.GoogleCalendarDataSource
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GoogleCalendarRemoteDataSource @Inject constructor(
    googleAccountCredential: GoogleAccountCredential,
    transport: HttpTransport,
    jsonFactory: JsonFactory
) : GoogleCalendarDataSource {
    private val calendar: Calendar = Calendar.Builder(transport, jsonFactory, googleAccountCredential)
        .setApplicationName("Google Calendar Api MVC")
        .build()
    override fun getCalendarList(): Single<CalendarList> {
        return Single.fromCallable { calendar.CalendarList().list().execute() }
            .subscribeOn(Schedulers.io())
    }

    override fun getEvents(calendarId: String): Single<List<Event>> {
        return Single.fromCallable {
            calendar.events()
                .list(calendarId)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute() }
            .subscribeOn(Schedulers.io())
            .map { it.items }
    }
}