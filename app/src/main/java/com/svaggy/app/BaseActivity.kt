package com.svaggy.app

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.svaggy.utils.ProgressBarLoader

abstract class BaseActivity<T : ViewBinding>(private val inflateMethod: (LayoutInflater) -> T) : AppCompatActivity() {


    private var _binding: T? = null
    val binding: T get() = _binding!!

    open fun T.initialize() {}

    private val progressBarLoader by lazy {
        ProgressBarLoader()
    }

    open fun mProgressBar() : ProgressBarLoader {
        return progressBarLoader
    }






    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SvaggyApplication.localeManager!!.setLocale(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (_binding == null) {
            _binding = inflateMethod(layoutInflater)
            setContentView(binding.root)
            binding.initialize()
        }
    }

    private fun resetTitle() {
        try {
            val label = packageManager.getActivityInfo(
                componentName,
                PackageManager.GET_META_DATA
            ).labelRes
            if (label != 0) {
                setTitle(label)
            }
        } catch (_: PackageManager.NameNotFoundException) {
        }
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    fun setNewLocale(language: String): Boolean {
       SvaggyApplication.localeManager!!.setNewLocale(this, language)
        recreate()
//        val i = Intent(this, MainActivity::class.java)
//        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
//        if (restartProcess) {
//            System.exit(0)
//        } else {
//            Toast.makeText(this, "Activity restarted", Toast.LENGTH_LONG).show()
//        }
        return true
    }
}