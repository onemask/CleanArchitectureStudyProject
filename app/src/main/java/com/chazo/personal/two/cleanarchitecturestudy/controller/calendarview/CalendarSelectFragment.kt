package com.chazo.personal.two.cleanarchitecturestudy.controller.calendarview


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.chazo.personal.two.cleanarchitecturestudy.R
import com.chazo.personal.two.cleanarchitecturestudy.constant.RC_AUTH_PERMISSION
import com.chazo.personal.two.cleanarchitecturestudy.data.google_calender.GoogleCalendarRepository
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.calendar.model.CalendarListEntry
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_calendar_select.*
import javax.inject.Inject

class CalendarSelectFragment : DaggerFragment() {

    @Inject
    lateinit var googleCalendarRepository: GoogleCalendarRepository

    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable = CompositeDisposable()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar_select, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showCalendarButtons()
    }

    private fun showCalendarButtons() {
        getCalendars().subscribe({
            layout_calendars.removeAllViews()
            addCalendarButtons(it)
        }, {
            when (it) {
                is UserRecoverableAuthIOException -> startActivityForResult(it.intent, RC_AUTH_PERMISSION)
                else -> it.printStackTrace()
            }
        }).apply { compositeDisposable.add(this) }
    }

    private fun getCalendars(): Single<List<CalendarListEntry>> =
        googleCalendarRepository.getCalendarList()
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.items }
            .doFinally { progress_loading.visibility = View.GONE }

    private fun addCalendarButtons(calendars: List<CalendarListEntry>) {
        calendars.forEach {
            layout_calendars.addView(createCalendarButton(it))
        }
    }

    private fun createCalendarButton(calendar: CalendarListEntry): Button {
        val button = Button(requireContext())
        button.text = calendar.summary
        button.setOnClickListener {
            moveToCalendarFragment(calendar.id)
        }
        return button
    }

    private fun moveToCalendarFragment(calendarId: String) {
        CalendarSelectFragmentDirections.actionDestCalendarSelectToDestCalendar().apply {
            this.calendarId = calendarId
            findNavController().navigate(this)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_AUTH_PERMISSION && resultCode == Activity.RESULT_OK) {
            getCalendars()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

}
