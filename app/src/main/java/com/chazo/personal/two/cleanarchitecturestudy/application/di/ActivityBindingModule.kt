package com.chazo.personal.two.cleanarchitecturestudy.application.di

import com.chazo.personal.two.cleanarchitecturestudy.MainActivity
import com.chazo.personal.two.cleanarchitecturestudy.application.scopes.ActivityScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector()
    abstract fun mainActivity(): MainActivity

}