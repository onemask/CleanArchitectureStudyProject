package com.chazo.personal.two.cleanarchitecturestudy.application.di

import com.chazo.personal.two.cleanarchitecturestudy.controller.main.MainActivity
import com.chazo.personal.two.cleanarchitecturestudy.application.scopes.ActivityScoped
import com.chazo.personal.two.cleanarchitecturestudy.controller.main.MainModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun mainActivity(): MainActivity

}