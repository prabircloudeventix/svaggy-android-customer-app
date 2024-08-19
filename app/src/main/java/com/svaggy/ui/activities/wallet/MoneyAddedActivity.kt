package com.svaggy.ui.activities.wallet

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityMoneyAddedBinding

class MoneyAddedActivity :BaseActivity<ActivityMoneyAddedBinding>(ActivityMoneyAddedBinding::inflate){
  //  private lateinit var isFrom:String


    override fun ActivityMoneyAddedBinding.initialize(){
     //   isFrom = intent.getStringExtra("isFrom").toString()
        binding.orderConfirmGif.let {
            Glide.with(this@MoneyAddedActivity)
                .asGif()
                .load(R.drawable.order_confirm)
                .listener(object : RequestListener<GifDrawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<GifDrawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false

                    }

                    override fun onResourceReady(
                        resource: GifDrawable,
                        model: Any,
                        target: Target<GifDrawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource.setLoopCount(2)
                        resource.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                             Intent.FLAG_ACTIVITY_CLEAR_TOP
                                finish()

                            }
                        })
                        return false


                    }

                })
                .into(it)

        }

        binding.orderConfirmMessage.text= (getString(R.string.update_wallet_text,
            "${intent?.getStringExtra("added_amount")}","${intent?.getStringExtra("updated_balance")}"))

    }

}