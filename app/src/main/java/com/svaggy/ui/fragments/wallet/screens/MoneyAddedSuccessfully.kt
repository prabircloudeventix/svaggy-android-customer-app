package com.svaggy.ui.fragments.wallet.screens

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.svaggy.R
import com.svaggy.databinding.FragmentMoneyAddedSuccessfullyBinding

class MoneyAddedSuccessfully : Fragment() {
    lateinit var binding: FragmentMoneyAddedSuccessfullyBinding
    private var isFrom = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMoneyAddedSuccessfullyBinding.inflate(inflater, container, false)
//        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
//        (activity as MainActivity).binding.toolbar.titleTv.text = requireContext().getString(R.string.add_Funds)
//        (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility=View.GONE
        isFrom = arguments?.getString("isFrom").toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isFrom.isNotEmpty()){
                        findNavController().popBackStack(R.id.cartMoreItemScreen,false)
                    }else{
                        findNavController().popBackStack(R.id.myWalletFragment,false)
                    }
                }
            })

        binding.orderConfirmGif.let {
            Glide.with(this)
                .asGif()
                .load(R.drawable.order_confirm)
                .listener(object :RequestListener<GifDrawable>{
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
                                if (isFrom != "null"){
                                    findNavController().popBackStack(R.id.cartMoreItemScreen,false)
                                }else{
                                    findNavController().popBackStack(R.id.myWalletFragment,false)

                                }
                            }
                        })
                        return false


                    }

                })
                .into(it)

        }

        binding.orderConfirmMessage.text= requireContext().getString(R.string.update_wallet_text,"${arguments?.getString("added_amount")}","${arguments?.getString("updated_balance")}")


    }
}