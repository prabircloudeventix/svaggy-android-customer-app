package com.svaggy.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import java.util.Locale

class LocaleManager(context: Context) {
    private val prefs =  PrefUtils(context)
    fun setLocale(c: Context): Context {
        return updateResources(c, language)
    }

    fun setNewLocale(c: Context, language: String): Context {
        persistLanguage(language)
        return updateResources(c, language)
    }

    private val language: String?
        get() = prefs.getLang(Constants.currentLocale)

    @SuppressLint("ApplySharedPref")
    private fun persistLanguage(language: String) {
        // use commit() instead of apply(), because sometimes we kill the application process immediately
// which will prevent apply() to finish
        prefs.setString(Constants.currentLocale,language)
    }

    private fun updateResources(
        context: Context,
        language: String?
    ): Context {
        var context = context
        val locale = Locale(language!!)
        Locale.setDefault(locale)
        val res = context.resources
        val config =
            Configuration(res.configuration)
        config.setLocale(locale)
        context = context.createConfigurationContext(config)
        return context
    }

    companion object {
        fun getLocale(res: Resources): Locale {
            val config = res.configuration
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) config.locales[0] else config.locale
        }
    }


}