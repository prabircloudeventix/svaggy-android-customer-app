package com.svaggy.ui.activities.intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityFinalIntroBinding
import com.svaggy.databinding.ActivityNextIntroBinding
import com.svaggy.ui.activities.auth.PhoneLogInActivity
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.updateStatusBarColor

class FinalIntroActivity : BaseActivity<ActivityFinalIntroBinding>(ActivityFinalIntroBinding::inflate)  {




    override fun ActivityFinalIntroBinding.initialize() {
        this@FinalIntroActivity.updateStatusBarColor("#000000",false)


        binding.txtSkip.setOnClickListener {
            PrefUtils.instance.setBoolean(Constants.IsNotFirstOpen, true)
            startActivity(Intent(this@FinalIntroActivity, PhoneLogInActivity::class.java))
            finish()
        }

        binding.backOnbodingIv.setOnClickListener {
            startActivity(Intent(this@FinalIntroActivity, NextIntroActivity::class.java))
         finish()
        }

        binding.boardingNextbt.setOnClickListener {
            PrefUtils.instance.setBoolean(Constants.IsNotFirstOpen, true)
            startActivity(Intent(this@FinalIntroActivity, PhoneLogInActivity::class.java))
            finish()

        }

    }
}