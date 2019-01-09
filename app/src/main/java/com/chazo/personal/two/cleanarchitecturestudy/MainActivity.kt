package com.chazo.personal.two.cleanarchitecturestudy

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.chazo.personal.two.cleanarchitecturestudy.constant.PREF_GOOGLE_ACCOUNT_NAME
import com.chazo.personal.two.cleanarchitecturestudy.constant.RC_ACCOUNT_PICKER
import com.chazo.personal.two.cleanarchitecturestudy.constant.RC_AUTH_PERMISSION
import com.chazo.personal.two.cleanarchitecturestudy.constant.RP_GET_ACCOUNTS
import com.chazo.personal.two.cleanarchitecturestudy.data.google_calender.GoogleCalendarRepository
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    private lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var googleAccountCredential: GoogleAccountCredential

    @Inject
    lateinit var googleCalendarRepository: GoogleCalendarRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        compositeDisposable = CompositeDisposable()

        button_sign_in.setOnClickListener {
            isAccountName()
        }
    }

    private fun isAccountName(): Boolean {
        googleAccountCredential.selectedAccountName?.let {
            getCalendarList()
            return true
        }.let{
            selectAccount()
            return false
        }
    }

    private fun getCalendarList() {
        googleCalendarRepository.getCalendarList()
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.items }
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
        googleCalendarRepository.getEvents(calendarId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                text_calendar_data.text = it.fold("") { acc, event ->
                    acc + "date=${event.start.date} summary=${event.summary}\n"
                }
            }, { it.printStackTrace() })
            .apply { compositeDisposable.add(this) }
    }

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
                RC_AUTH_PERMISSION -> isAccountName()
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
