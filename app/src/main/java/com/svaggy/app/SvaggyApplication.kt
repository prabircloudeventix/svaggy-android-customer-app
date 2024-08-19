package com.svaggy.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.google.android.libraries.places.api.Places
import com.stripe.android.PaymentConfiguration
import com.svaggy.R
import com.svaggy.local.LocaleManager
import com.svaggy.utils.ProgressLoaderNew
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SvaggyApplication : Application() {
    companion object {
        lateinit var instance: SvaggyApplication
        var localeManager: LocaleManager? = null
        var progressBarLoader =  ProgressLoaderNew()


    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        //setLocale()
       // initPusher()
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        PaymentConfiguration.init(applicationContext, getString(R.string.stripe_payment_key))
        // Initialize the progress bar loader with the application context
    }

    override fun attachBaseContext(base: Context) {
        localeManager = LocaleManager(base)
        super.attachBaseContext(localeManager!!.setLocale(base))
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeManager?.setLocale(this)
    }




}