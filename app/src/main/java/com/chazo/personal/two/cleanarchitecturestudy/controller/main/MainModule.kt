package com.chazo.personal.two.cleanarchitecturestudy.controller.main

import com.chazo.personal.two.cleanarchitecturestudy.application.scopes.ActivityScoped
import com.chazo.personal.two.cleanarchitecturestudy.application.scopes.FragmentScoped
import com.chazo.personal.two.cleanarchitecturestudy.controller.auth.AuthFragment
import com.chazo.personal.two.cleanarchitecturestudy.controller.calendarview.CalendarFragment
import com.chazo.personal.two.cleanarchitecturestudy.controller.calendarview.CalendarSelectFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainModule {

    @FragmentScoped
    @ContributesAndroidInjector()
    abstract fun authFragment(): AuthFragment

    @FragmentScoped
    @ContributesAndroidInjector()
    abstract fun calendarSelectFragment(): CalendarSelectFragment

    @FragmentScoped
    @ContributesAndroidInjector()
    abstract fun calendarFragment(): CalendarFragment
}