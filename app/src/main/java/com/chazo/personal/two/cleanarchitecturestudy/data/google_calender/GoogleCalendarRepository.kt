package com.chazo.personal.two.cleanarchitecturestudy.data.google_calender

import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import io.reactivex.Single

class GoogleCalendarRepository(private val remote: GoogleCalendarRemoteDataSource): GoogleCalendarDataSource {
    override fun getCalendarList(): Single<CalendarList> {
        return remote.getCalendarList()
    }

    override fun getEvents(calendarId: String): Single<List<Event>> {
        return remote.getEvents(calendarId)
    }
}