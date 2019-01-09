package com.chazo.personal.two.cleanarchitecturestudy.controller.calendarview


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chazo.personal.two.cleanarchitecturestudy.R
import com.chazo.personal.two.cleanarchitecturestudy.data.google_calender.GoogleCalendarRepository
import com.google.api.services.calendar.model.Event
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_calendar.*
import javax.inject.Inject

class CalendarFragment : DaggerFragment() {

    private lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var googleCalendarRepository: GoogleCalendarRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable = CompositeDisposable()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let {
            val safeArgs = CalendarFragmentArgs.fromBundle(it)
            if (safeArgs.calendarId != getString(R.string.calendarId_default)) {
                setupCalendarData(safeArgs.calendarId)
            }
        }
    }

    private fun setupCalendarData(calendarId: String) {
        getEvents(calendarId).subscribe({
            text_calendar_data.text = createEventsText(it)
        }, {
            it.printStackTrace()
        }).apply { compositeDisposable.add(this) }
    }

    private fun getEvents(calendarId: String): Single<List<Event>> =
        googleCalendarRepository.getEvents(calendarId)
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { progress_loading.visibility = View.GONE }

    private fun createEventsText(events: List<Event>): String {
        val size = events.size
        return events.foldIndexed("") { index, acc, event ->
            acc + "date=${event.start.date} summary=${event.summary}" + if(index == size-1) "" else "\n"
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
