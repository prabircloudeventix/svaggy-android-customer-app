package com.svaggy.ui.activities.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityHelpBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.utils.hide
import com.svaggy.utils.show

class HelpActivity :BaseActivity<ActivityHelpBinding>(ActivityHelpBinding::inflate) {


    override fun ActivityHelpBinding.initialize(){
      binding.backButton.setOnClickListener {
           onBackPressed()
        }



        initBinding()




    }
    fun setPlusIcon(view: TextView){
        val endDrawable = ContextCompat.getDrawable(this@HelpActivity, R.drawable.ic_add)
        view.setCompoundDrawablesWithIntrinsicBounds(null, null, endDrawable, null)

    }
    fun setMinus(view: TextView){
        val endDrawable = ContextCompat.getDrawable(this@HelpActivity, R.drawable.ic_minus)
        view.setCompoundDrawablesWithIntrinsicBounds(null, null, endDrawable, null)

    }

    private fun initBinding() {
        binding.apply {
            q1.setOnClickListener {
                if (textHelpDesc1.isVisible){
                    textHelpDesc1.hide()
                    setPlusIcon(q1)

                }else{
                    setMinus(q1)
                    textHelpDesc1.show()
                }
            }
            ///
            q2.setOnClickListener {
                if (txtHelpDesc2.isVisible){
                    setPlusIcon(q2)
                    txtHelpDesc2.hide()
                }else{
                    setMinus(q2)
                    txtHelpDesc2.show()
                }

            }
            ////
            q3.setOnClickListener {
                if (textHelpDesc3.isVisible){
                    setPlusIcon(q3)
                    textHelpDesc3.hide()
                }else{
                    setMinus(q3)
                    textHelpDesc3.show()
                }

            }
            ////
            q4.setOnClickListener {
                if (textHelpDesc4.isVisible){
                    textHelpDesc4.hide()
                    setPlusIcon(q4)
                }else{
                    setMinus(q4)
                    textHelpDesc4.show()
                }

            }
            ///
            q5.setOnClickListener {
                if (textHelp5.isVisible){
                    setPlusIcon(q5)
                    textHelp5.hide()
                }else{
                    setMinus(q5)
                    textHelp5.show()
                }

            }
            ///
            q6.setOnClickListener {
                if (textHelp6.isVisible){
                    setPlusIcon(q6)
                    textHelp6.hide()
                }else{
                    setMinus(q6)
                    textHelp6.show()
                }

            }
            ///
            q7.setOnClickListener {
                if (textHelpDesc7.isVisible){
                    setPlusIcon(q7)
                    textHelpDesc7.hide()
                }else{
                    setMinus(q7)
                    textHelpDesc7.show()
                }

            }
            ///
            q8.setOnClickListener {
                if (textHelpDesc8.isVisible){
                    setPlusIcon(q8)
                    textHelpDesc8.hide()
                }else{
                    setMinus(q8)
                    textHelpDesc8.show()
                }

            }
            //
            q9.setOnClickListener {
                if (textHelpDesc9.isVisible){
                    setPlusIcon(q9)
                    textHelpDesc9.hide()
                }else{
                    setMinus(q9)
                    textHelpDesc9.show()
                }

            }
            //
            q10.setOnClickListener {
                if (textHelpDesc10.isVisible){
                    setPlusIcon(q10)
                    textHelpDesc10.hide()
                }else{
                    setMinus(q10)
                    textHelpDesc10.show()
                }

            }
            ////
            q11.setOnClickListener {
                if (textHelpDesc11.isVisible){
                    setPlusIcon(q11)
                    textHelpDesc11.hide()
                }else{
                    setMinus(q11)
                    textHelpDesc11.show()
                }

            }
            ///
            q12.setOnClickListener {
                if (textHelpDesc12.isVisible){
                    setPlusIcon(q12)
                    textHelpDesc12.hide()
                }else{
                    setMinus(q12)
                    textHelpDesc12.show()
                }

            }
            ///
            q13.setOnClickListener {
                if (textHelpDesc13.isVisible){
                    setPlusIcon(q13)
                    textHelpDesc13.hide()
                }else{
                    setMinus(q13)
                    textHelpDesc13.show()
                }

            }
            ///
            q14.setOnClickListener {
                if (textHelpDesc14.isVisible){
                    setPlusIcon(q14)
                    textHelpDesc14.hide()
                }else{
                    setMinus(q14)
                    textHelpDesc14.show()
                }

            }
            ///
            q15.setOnClickListener {
                if (textHelpDesc15.isVisible){
                    setPlusIcon(q15)
                    textHelpDesc15.hide()
                }else{
                    setMinus(q15)
                    textHelpDesc15.show()
                }

            }

            //
            goMail.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@svaggy.com")
                    // If you want to pre-fill the subject or body, you can add them as extras
                    // putExtra(Intent.EXTRA_SUBJECT, "Your Subject Here")
                    // putExtra(Intent.EXTRA_TEXT, "Your Message Here")
                }

                try {
                    startActivity(emailIntent)
                } catch (e: ActivityNotFoundException) {
                    // Handle case where no email app is available
                    Toast.makeText(this@HelpActivity, "No email app found", Toast.LENGTH_SHORT).show()
                }

            }



        }

    }


}