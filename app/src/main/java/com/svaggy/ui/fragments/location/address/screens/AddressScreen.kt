package com.svaggy.ui.fragments.location.address.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.app.SvaggyApplication
import com.svaggy.databinding.FragmentAddressScreenBinding
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.ui.fragments.location.adapter.AddressAdapter
import com.svaggy.ui.fragments.location.model.GetAddress
import com.svaggy.ui.fragments.location.viewmodel.AddressViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.onBackPressedDispatcher
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressScreen : Fragment() {
    lateinit var binding: FragmentAddressScreenBinding
    private var addressList: ArrayList<GetAddress.Data>? = null
    private var addressAdapter: AddressAdapter? = null
    private var deletePosition:Int?=0
    private val mViewModel by viewModels<AddressViewModel>()
    private val mviewModel by viewModels<CartViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddressScreenBinding.inflate(inflater, container, false)
        initObserver()

        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = ContextCompat.getString(requireContext(), R.string.addresses)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        onBackPressedDispatcher {
            findNavController().popBackStack(R.id.profileScreen,false)

        }


        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack(R.id.profileScreen,false)
        }

        binding.txtAddNewAddress.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("isFrom", "AddAddress")
            PrefUtils.instance.setString("isFrom","AddAddress")
            findNavController().navigate(R.id.action_addressScreen_to_currentLocationFragment,bundle)
        }
    }

    private fun initObserver() {

        mViewModel.getAddressDataLive.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
               binding.progressBarMenu.hide()
                if (it.data.size > 0) {
                    binding.recyclerAddress.visibility = View.VISIBLE
                    binding.consEmptyAddress.visibility = View.GONE
                    binding.recyclerAddress.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    binding.recyclerAddress.isNestedScrollingEnabled = true
                    addressList = it.data
                  //  addressAdapter = AddressAdapter(requireContext(), "AddressScreen",addressList!!, ::getSelectAddressData, ::editAddressItem)
                    binding.recyclerAddress.adapter = addressAdapter
                }
                else {
                    PrefUtils.instance.setString(Constants.AddressId,"")
                    binding.recyclerAddress.visibility = View.GONE
                    binding.consEmptyAddress.visibility = View.VISIBLE
                }
            } else {
//                (activity as MainActivity).hideLoader()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        mViewModel.deleteAddressDataLive.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
                binding.progressBarMenu.hide()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
//                addressAdapter?.deleteItem(addressList!!, deletePosition!!)
                addressList?.removeAt(deletePosition!!)
                addressAdapter?.notifyItemRangeRemoved(deletePosition!!, addressList?.size!!-1)
                mViewModel.getAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")

            } else {
//                (activity as MainActivity).hideLoader()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        mViewModel.editAddressDataSingle.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
                addressAdapter?.notifyDataSetChanged()
            }
            else
            {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        mviewModel.setCurrentAddressDataLive.observe(viewLifecycleOwner) {
            if (it.isSuccess == true) {
              binding.progressBarMenu.hide()
                findNavController().popBackStack()
            } else {
               binding.progressBarMenu.hide()
                //Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSelectAddressData(position:Int, addressId:Int, address:String, isClicked:String){
        deletePosition = position
        if (isClicked == "ForDelete") {
            if (addressId != 0) {
                PrefUtils.instance.showCustomDialog(
                    requireContext(),
                    "Are You Sure You Want Delete This Address?",
                    "No",
                    "Yes",
                    "",
                    false,
                    true,
                    negativeAlert = {
                        it.dismiss()
                    },
                    positiveAlert = {
                        mViewModel.deleteAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}", addressId)
                        it.dismiss()
                    }
                )
            }
        }
        else if (isClicked == "ForDefault") {
            mViewModel.editAddress(
                "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                addressId,
                false,
                true,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                true
            )
        }
        else
        {
            mviewModel.setCurrentAddress(
                "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                addressId
            )
            findNavController().previousBackStackEntry?.savedStateHandle?.set("current_address", address)
        }
    }

    private fun editAddressItem(position:Int, isClicked:Boolean, addressId:Int, residence: String, region: String, postal: String, latitude: String, longitude: String, addressType: String) {
        val bundle = Bundle()
        bundle.putInt("position", position)
        bundle.putBoolean("isClicked", isClicked)
        bundle.putInt("addressId", addressId)
        bundle.putString("isFrom", "EditAddress")
        bundle.putString("address", "$residence $region $postal")
        bundle.putString("residence", residence)
        bundle.putString("region", region)
        bundle.putString("postal", postal)
        bundle.putString("latitude", latitude)
        bundle.putString("longitude", longitude)
        bundle.putString("addressType", addressType)
        findNavController().navigate(R.id.action_addressScreen_to_currentLocationFragment,bundle)
    }
}