package com.svaggy.ui.fragments.profile.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.databinding.FragmentLanguageScreenBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.profile.viewmodel.ProfileViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.LANGUAGE
import com.svaggy.utils.Constants.PATH
import com.svaggy.utils.Constants.en
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguageScreen : Fragment() {
    private var _binding: FragmentLanguageScreenBinding? = null
    private val binding get() = _binding!!
    private val mViewModel by viewModels<ProfileViewModel>()
    private var getLang = ""
    private var setLang = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLanguageScreenBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = requireContext().getText(R.string.Choose_Language)
 //       (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE
       // initObserver()
        getLang = arguments?.getString(en).toString()
        initBinding()
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

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
                    val map = mapOf(LANGUAGE to setLang)
                    updateLang(map)

                }
            }
            if (getLang.isNotEmpty()){
                if (getLang == en){
                     setEnglish.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_checbox, 0, 0, 0)
                    setCzech.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checbox, 0, 0, 0)
                }else{
                    setEnglish.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checbox, 0, 0, 0)
                   setCzech.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_checbox, 0, 0, 0)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                        activity?.finish()
                        val intent = Intent(Intent(requireContext(),MainActivity::class.java))
                        intent.putExtra(PATH,true)
                        startActivity(intent)
                    }
                    is ApiResponse.Failure -> {
                       binding.progressBarMenu.hide()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }
}