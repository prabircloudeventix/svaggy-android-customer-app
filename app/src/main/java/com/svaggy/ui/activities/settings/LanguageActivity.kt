package com.svaggy.ui.activities.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityLanguageBinding
import com.svaggy.databinding.FragmentLanguageScreenBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.profile.viewmodel.ProfileViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {
    private val mViewModel by viewModels<ProfileViewModel>()
    private var getLang = ""
    private var setLang = ""


    override fun ActivityLanguageBinding.initialize(){
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        getLang = intent?.getStringExtra(Constants.en).toString()
        initBinding()


    }
    private fun initBinding() {
        binding.apply {
            setEnglish.setOnClickListener {
                setLang = "en"
                setEnglish.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_checbox, 0, 0, 0)
                setCzech.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checbox, 0, 0, 0)
            }
            setCzech.setOnClickListener {
                setLang = "cs"
                setEnglish.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checbox, 0, 0, 0)
                setCzech.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_checbox, 0, 0, 0)
            }
            saveBtn.setOnClickListener {
                if (setLang.isNotEmpty()){
                    val map = mapOf(Constants.LANGUAGE to setLang)
                    updateLang(map)

                }
            }
            if (getLang.isNotEmpty()){
                if (getLang == Constants.en){
                    setEnglish.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_checbox, 0, 0, 0)
                    setCzech.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checbox, 0, 0, 0)
                }else{
                    setEnglish.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checbox, 0, 0, 0)
                    setCzech.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_checbox, 0, 0, 0)
                }
            }
        }
    }


    private fun updateLang(map: Map<String, String>){
        lifecycleScope.launch {
            mViewModel.setSettings(authToken = "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}", map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        binding.progressBarMenu.show()

                    }
                    is ApiResponse.Success -> {
                        binding.progressBarMenu.hide()
                        if (setLang == "en")
                            PrefUtils.instance.setLang(Constants.currentLocale,"en")
                        else
                            PrefUtils.instance.setLang(Constants.currentLocale,"cs")

                        val intent = Intent(this@LanguageActivity, MainActivity::class.java).apply {
                            putExtra(Constants.PATH, true)
                            Constants.isInitBottomNav = false
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                    is ApiResponse.Failure -> {
                        binding.progressBarMenu.hide()
                        Toast.makeText(this@LanguageActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }

}