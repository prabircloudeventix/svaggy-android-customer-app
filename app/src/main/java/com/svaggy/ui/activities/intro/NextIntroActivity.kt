package com.svaggy.ui.activities.intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityFirstIntroBinding
import com.svaggy.databinding.ActivityNextIntroBinding
import com.svaggy.ui.activities.auth.PhoneLogInActivity
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.updateStatusBarColor

class NextIntroActivity : BaseActivity<ActivityNextIntroBinding>(ActivityNextIntroBinding::inflate)  {




    override fun ActivityNextIntroBinding.initialize() {
      this@NextIntroActivity.updateStatusBarColor("#000000",false)


        binding.txtSkip.setOnClickListener {
            PrefUtils.instance.setBoolean(Constants.IsNotFirstOpen, true)
            startActivity(Intent(this@NextIntroActivity, PhoneLogInActivity::class.java))
            finish()
        }

        binding.backOnbodingIv.setOnClickListener {
            startActivity(Intent(this@NextIntroActivity,FirstIntroActivity::class.java))
            finish()
        }

        binding.boardingNextbt.setOnClickListener {
            startActivity(Intent(this@NextIntroActivity,FinalIntroActivity::class.java))
            finish()
        }

    }


    override fun onResume() {
        super.onResume()
    }

}