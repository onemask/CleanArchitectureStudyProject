package com.chazo.personal.two.cleanarchitecturestudy.application



import com.chazo.personal.two.cleanarchitecturestudy.application.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class GlobalApplication: DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.builder().application(this).build()
}