package com.chazo.personal.two.cleanarchitecturestudy.application.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module

@Module()
abstract class ApplicationModule {

    @Binds
    abstract fun provideContext(app: Application): Context
}