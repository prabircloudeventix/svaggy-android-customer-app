package com.svaggy.ui.fragments.profile.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentHelpScreenBinding

class HelpScreen : Fragment()
{
    lateinit var binding: FragmentHelpScreenBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentHelpScreenBinding.inflate(inflater, container, false)

        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = ContextCompat.getString(requireContext(), R.string.help)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
            )
        binding.imgHide.setOnClickListener {
            if (binding.txtHelpDesc1.visibility == 0)
            {
                binding.txtHelpDesc1.visibility = View.GONE
                binding.imgHide.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_add))
            }
            else {
                binding.txtHelpDesc1.visibility = View.VISIBLE
                binding.imgHide.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_minus))
            }
        }

        binding.imgHide2.setOnClickListener {
            if (binding.txtHelpDesc2.visibility == 0)
            {
                binding.txtHelpDesc2.visibility = View.GONE
                binding.imgHide2.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_add))
            }
            else {
                binding.txtHelpDesc2.visibility = View.VISIBLE
                binding.imgHide2.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_minus))
            }
        }

        binding.imgHide3.setOnClickListener {
            if (binding.txtHelpDesc3.visibility == 0)
            {
                binding.txtHelpDesc3.visibility = View.GONE
                binding.imgHide3.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_add))
            }
            else {
                binding.txtHelpDesc3.visibility = View.VISIBLE
                binding.imgHide3.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_minus))
            }
        }

        binding.imgHide4.setOnClickListener {
            if (binding.txtHelpDesc4.visibility == 0)
            {
                binding.txtHelpDesc4.visibility = View.GONE
                binding.imgHide4.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_add))
            }
            else {
                binding.txtHelpDesc4.visibility = View.VISIBLE
                binding.imgHide4.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_minus))
            }
        }
    }
}