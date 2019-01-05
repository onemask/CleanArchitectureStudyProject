package com.chazo.personal.two.cleanarchitecturestudy

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.chazo.personal.two.cleanarchitecturestudy.constant.*
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.Arrays.asList


class MainActivity : AppCompatActivity() {

    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var googleAccountCredential: GoogleAccountCredential

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        compositeDisposable = CompositeDisposable()
        setupGoogleAccountCredential()

        button_sign_in.setOnClickListener {
            getCalendarList()
        }
    }

    private fun setupGoogleAccountCredential() {
        googleAccountCredential = GoogleAccountCredential
            .usingOAuth2(applicationContext, asList(CalendarScopes.CALENDAR))
            .setBackOff(ExponentialBackOff())
    }

    private fun getCalendarList() {
        if (isAccountName()) {
            requestGetCalendarListEntry(createGoogleCalendarService())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.forEach { item ->
                        val button = Button(this)
                        button.text = item.summary
                        button.setOnClickListener {
                            getEvents(item.id)
                        }
                        layout_kind_of_calendar.addView(button)
                    }
                }, {
                    when (it) {
                        is UserRecoverableAuthIOException -> startActivityForResult(it.intent, RC_AUTH_PERMISSION)
                        else -> it.printStackTrace()
                    }
                }).apply { compositeDisposable.add(this) }
        }
    }

    private fun isAccountName(): Boolean {
        googleAccountCredential.selectedAccountName?.let {
            return true
        }.let {
            selectAccount()
            return false
        }
    }


    private fun requestGetCalendarListEntry(googleCalendar: Calendar): Single<List<CalendarListEntry>> =
        Single.fromCallable { googleCalendar.CalendarList().list().execute() }
            .subscribeOn(Schedulers.io())
            .map {
                Log.d("!!!", it.etag)
                it.items
            }

    private fun createGoogleCalendarService(): Calendar {
        val transport: HttpTransport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

        return Calendar.Builder(transport, jsonFactory, googleAccountCredential)
            .setApplicationName("Google Calendar Api MVC")
            .build()
    }

    @AfterPermissionGranted(RP_GET_ACCOUNTS)
    private fun selectAccount() {
        val perms = arrayOf(Manifest.permission.GET_ACCOUNTS)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            with(googleAccountCredential) {
                val accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_GOOGLE_ACCOUNT_NAME, null)

                accountName?.let {
                    selectedAccountName = accountName
                    getCalendarList()
                } ?: startActivityForResult(newChooseAccountIntent(), RC_ACCOUNT_PICKER)
            }
        } else {
            requestPermission(perms)
        }
    }

    private fun getEvents(calendarId: String) {
        if (isAccountName()) {
            requestEvents(calendarId, createGoogleCalendarService())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    text_calendar_data.text = it.fold("") { acc, event ->
                        acc + "date=${event.start.date} summary=${event.summary}\n"
                    }
                }, { it.printStackTrace() })
                .apply { compositeDisposable.add(this) }
        }
    }

    private fun requestEvents(calendarId: String, googleCalendar: Calendar): Single<List<Event>> =
        Single.fromCallable {
            googleCalendar.events()
                .list(calendarId)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute() }
            .subscribeOn(Schedulers.io())
            .map { it.items }


    private fun requestPermission(permissions: Array<String>) =
        EasyPermissions.requestPermissions(
            this,
            "구글 계정을 가져오기 위해 필요합니다.",
            RP_GET_ACCOUNTS, *permissions
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_ACCOUNT_PICKER -> data?.let {
                    val accountName = it.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    accountName?.let {
                        getPreferences(Context.MODE_PRIVATE).edit().apply {
                            putString(PREF_GOOGLE_ACCOUNT_NAME, it)
                            apply()
                        }
                        googleAccountCredential.selectedAccountName = it
                        getCalendarList()
                    }
                }
                RC_AUTH_PERMISSION -> getCalendarList()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
