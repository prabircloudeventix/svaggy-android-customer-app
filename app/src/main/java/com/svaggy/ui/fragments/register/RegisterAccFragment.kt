package com.svaggy.ui.fragments.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.svaggy.ui.activities.MainActivity
import com.svaggy.databinding.FragmentRegisterAccBinding
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils

class RegisterAccFragment : Fragment()
{
    private var fmbinding: FragmentRegisterAccBinding?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        fmbinding = FragmentRegisterAccBinding.inflate(inflater, container, false)
      //  PrefUtils.instance.setString(Constants.CurrentDestinationId,(activity as MainActivity).navController?.currentDestination?.id.toString())
        return fmbinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*fmbinding?.continueBt?.setOnClickListener {
            findNavController().navigate(R.id.action_registerAccFragment_to_preferencesFragment)
        }*/

        /*fmbinding?.signIn?.setOnClickListener {
            findNavController().navigate(R.id.action_registerAccFragment_to_loginPhnNo)
        }*/
    }

}