package com.chazo.personal.two.cleanarchitecturestudy.application.di

import android.app.Application
import com.chazo.personal.two.cleanarchitecturestudy.application.GlobalApplication
import com.chazo.personal.two.cleanarchitecturestudy.data.google_calender.GoogleCalendarDataModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        ActivityBindingModule::class,
        GoogleCalendarDataModule::class
    ]
)
interface AppComponent : AndroidInjector<GlobalApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): AppComponent.Builder
        fun build(): AppComponent
    }
}