package com.svaggy.ui.fragments.profile.screens

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.app.SvaggyApplication
import com.svaggy.databinding.FragmentEditProfileBinding
import com.svaggy.ui.fragments.profile.viewmodel.ProfileViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditProfileScreen : Fragment() {
    lateinit var binding: FragmentEditProfileBinding

    private val mViewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
     //   PrefUtils.instance.setString(Constants.CurrentDestinationId, (activity as MainActivity).navController.currentDestination?.id.toString())
        initObserver()

        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
    //    (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE
        (activity as MainActivity).binding.toolbar.titleTv.text = context?.getString(R.string.edit_profile)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (arguments?.getString("cartBundle") == "CartMoreItemScreen") {
                        findNavController().popBackStack(R.id.cartMoreItemScreen, false)
                    } else if (arguments?.getString("cartBundle") == "ProfileScreen") {
                        findNavController().popBackStack()
                    } else {
                        findNavController().popBackStack(R.id.cardPaymentScreen, false)
                    }
                }
            }
            )


        //  First Type Spacing Remove Automatic
        binding.edtFirstName.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtLastName.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtEmail.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtMobile.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())


        binding.edtFirstName.setText(PrefUtils.instance.getString(Constants.UserFirstName) ?: "")
        binding.edtLastName.setText(PrefUtils.instance.getString(Constants.UserLastName) ?: "")
        binding.edtEmail.text = PrefUtils.instance.getString(Constants.UserEmail) ?: ""
        binding.edtMobile.text = PrefUtils.instance.getString(Constants.UserMobile) ?: ""

        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.edtEmail.setOnClickListener {
            val routes = EditProfileScreenDirections.actionEditProfileToEditEmailFragment()
            findNavController().navigate(routes)

        }

        binding.edtMobile.setOnClickListener {
            val routes = EditProfileScreenDirections.actionEditProfileToEditPhoneFragment()
            findNavController().navigate(routes)

        }

        binding.btnSaveChange.setOnClickListener {
            if (binding.edtFirstName.text.isNullOrEmpty()) {
                Toast.makeText(context, "First Name Empty", Toast.LENGTH_SHORT).show()
            }
            else if (binding.edtLastName.text.isNullOrEmpty())
            {
                Toast.makeText(context, "Last Name Empty", Toast.LENGTH_SHORT).show()
            }else if (binding.edtEmail.text.isNullOrEmpty()){
                Toast.makeText(context, "Please Add Mail", Toast.LENGTH_SHORT).show()
            }
            else {
                mViewModel.editProfile(
                    "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    binding.edtFirstName.text.toString(),
                    binding.edtLastName.text.toString()
                )
            }
        }

    }

    private fun initObserver() {
        mViewModel.editProfileDataSingle.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
                PrefUtils.instance.setString(Constants.UserFirstName, it.data?.firstName)
                PrefUtils.instance.setString(Constants.UserLastName, it.data?.lastName)
                PrefUtils.instance.setString(Constants.UserEmail, it.data?.email)
                PrefUtils.instance.setString(Constants.UserMobile, it.data?.phoneNumber)
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                binding.edtFirstName.setText(
                    PrefUtils.instance.getString(Constants.UserFirstName) ?: ""
                )
                binding.edtLastName.setText(
                    PrefUtils.instance.getString(Constants.UserLastName) ?: ""
                )
                binding.edtEmail.text = PrefUtils.instance.getString(Constants.UserEmail) ?: ""
                binding.edtMobile.text = PrefUtils.instance.getString(Constants.UserMobile) ?: ""
                if (arguments?.getString("cartBundle") == "ProfileScreen") {
                    findNavController().navigate(R.id.profileScreen)
                    findNavController().popBackStack(R.id.profileScreen,false)
                } else if (arguments?.getString("cartBundle") == "CartMoreItemScreen") {
                   // findNavController().navigate(R.id.action_editProfile_to_cardPaymentScreen, cartBundle)
                    findNavController().popBackStack(R.id.cartMoreItemScreen,false)
                }
            } else {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }

    }
}