package com.svaggy.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.svaggy.R
import com.svaggy.ui.fragments.home.model.RepeatOrderModel
import com.svaggy.ui.fragments.home.model.Search
import com.svaggy.ui.fragments.home.model.SharedPrefModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class PrefUtils(context: Context) {
    private val _pref: SharedPreferences = context.getSharedPreferences(
        "Svaggy", Context.MODE_PRIVATE
    )

    fun setString(key: String?, value: String?): Boolean {
        val editor = _pref.edit()
        editor.putString(key, value)
        return editor.commit()
    }

    fun setArray(key: String?, value: ArrayList<Int>): Boolean {
        val editor = _pref.edit()
        editor.putString(key, value.toString())
        return editor.commit()
    }

    fun clearEditor() {
        val editor = _pref.edit()
        editor.clear()
        return editor.apply()
    }

    fun getString(key: String): String? {
        return try {
            _pref.getString(key, "")
        } catch (e: Exception) {
            ""
        }
    }

    fun setBoolean(key: String, value: Boolean) {
        val editor = _pref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        return _pref.getBoolean(key, false)
    }
    fun setLang(key: String?, value: String?): Boolean {
        val editor = _pref.edit()
        editor.putString(key, value)
        return editor.commit()
    }


    fun getLang(key: String): String {
        return try {
            _pref.getString(key, "en").toString()
        } catch (e: Exception) {
            "en"
        }
    }

 fun saveMenuItemsList(key: String, menuItemsList: List<SharedPrefModel>) {
        // Get an instance of SharedPreferences.Editor
        val editor = _pref.edit()
        // Convert the ArrayList<MenuItems> to JSON string using Gson
       val gson = Gson()
        val json = gson.toJson(menuItemsList)

        // Store the JSON string
        editor.putString(key, json)

        // Commit the changes
        editor.apply()
    }

    fun getMenuItemsList(key: String): List<SharedPrefModel>? {
        // Get an instance of SharedPreferences
        // Retrieve the JSON string
        val json =  _pref.getString(key, null)

        // Convert JSON string back to ArrayList<MenuItems> using Gson
        val gson = Gson()
        val type = object : TypeToken<List<SharedPrefModel>>() {}.type
        return gson.fromJson<List<SharedPrefModel>>(json, type)
    }


    fun saveRepeatOrder(key: String, menuItemsList: List<RepeatOrderModel>) {
        // Get an instance of SharedPreferences.Editor
        val editor = _pref.edit()
        // Convert the ArrayList<MenuItems> to JSON string using Gson
        val gson = Gson()
        val json = gson.toJson(menuItemsList)

        // Store the JSON string
        editor.putString(key, json)

        // Commit the changes
        editor.apply()
    }
    fun removeRepeatOrderList(key: String) {
        val editor = _pref.edit()
        editor.remove(key)
        editor.apply()
    }

    fun getRepeatItemsList(key: String): List<RepeatOrderModel>? {
        val json =  _pref.getString(key, null)
        val gson = Gson()
        val type = object : TypeToken<List<RepeatOrderModel>>() {}.type
        return gson.fromJson<List<RepeatOrderModel>>(json, type)
    }

    fun saveSearchList(key: String,recentSearchLists: List<Search.Data>){
        val editor = _pref.edit()
        val gson = Gson()
        val json: String = gson.toJson(recentSearchLists)
        editor.putString(key, json)
        editor.apply()
    }
    fun saveSearchListNew(key: String, newSearchItem: Search.Data) {
        // Get the existing search list from SharedPreferences
        val gson = Gson()
        val json = _pref.getString(key, null)
        val type = object : TypeToken<MutableList<Search.Data>>() {}.type
        val recentSearchLists: MutableList<Search.Data> = if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }

        // Remove the new search item if it already exists in the list
        recentSearchLists.removeAll { it == newSearchItem }

        // Add the new search item to the beginning of the list
        recentSearchLists.add(0, newSearchItem)

        // Ensure the list contains only the latest 5 items
        if (recentSearchLists.size > 5) {
            recentSearchLists.removeAt(recentSearchLists.size - 1)
        }

        // Save the updated list back to SharedPreferences
        val editor = _pref.edit()
        val updatedJson: String = gson.toJson(recentSearchLists)
        editor.putString(key, updatedJson)
        editor.apply()
    }




    fun removeMenuItemsList(key: String) {
        val editor = _pref.edit()
        editor.remove(key)
        editor.apply()
    }

    fun saveBoosterList(key: String, boosterList: ArrayList<Int>) {
        // Get an instance of SharedPreferences.Editor
        val editor = _pref.edit()
        // Convert the ArrayList<MenuItems> to JSON string using Gson
        val gson = Gson()
        val json = gson.toJson(boosterList)

        // Store the JSON string
        editor.putString(key, json)

        // Commit the changes
        editor.apply()
    }

    fun getBoosterList(key: String): ArrayList<Int> {
        // Get an instance of SharedPreferences
        val json = _pref.getString(key, null) ?: return ArrayList()

        // If json is null or key not found, return an empty ArrayList<Int>

        // Convert JSON string back to ArrayList<Int> using Gson
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Int>>() {}.type
        return gson.fromJson(json, type) ?: ArrayList()
    }

    fun removeBoosterList(key: String) {
        val editor = _pref.edit()
        editor.remove(key)
        editor.apply()
    }





    fun clearCartItemsFromSharedPreferences() {
        val editor = _pref.edit()
        val allKeys = _pref.all.keys
        val cartItemKeys = allKeys.filter { key -> key.matches(Regex("\\d+")) } // Match numerical keys
        cartItemKeys.forEach { key ->
            editor.remove(key)
        }

        // Apply changes
        editor.apply()
    }
    fun isCartValue(): Boolean {
        val allKeys = _pref.all.keys
        val cartItemKeys = allKeys.filter { key -> key.matches(Regex("\\d+")) } // Match numerical keys
        return cartItemKeys.isNotEmpty()

    }


    companion object {
        lateinit var instance: PrefUtils
    }

    init {
        instance = this
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun updateTimePickup(hoursVal: Int, minsValue: Int): String {
        val hours = hoursVal

        val hoursStr: String = if (hours < 10) {
            "0$hours"
        } else {
            hours.toString()
        }

        val minutes: String = if (minsValue < 10) {
            "0$minsValue"
        } else {
            minsValue.toString()
        }

        return StringBuilder().append(hoursStr).append(":").append(minutes).toString()
    }



    fun getCurrentTime24HourFormat(): String {
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(currentTime))
    }
    fun hasInternetConnection(application: Context): Boolean {
        val connectivityManager = application.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return networkCapabilities?.run {
            when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } ?: false
    }

    fun formatDouble(d: Double): String {
        return if (d == d.toLong().toDouble()) {
            String.format("%.2f", d)
            //String.format("%.2f", value)
        }
        else {
            String.format("%s", d)
        }
    }

    fun showToast(context: Context?, message: String?)
    {
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
    }

    fun alertDialog(context: Context?, title: String?, message: String?, okAlert : (dialog : DialogInterface) -> Unit?, cancelAlert : (dialog : DialogInterface) -> Unit?, isCancelHide: Boolean = false):AlertDialog.Builder
    {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setIcon(R.mipmap.ic_launcher_round)
        builder.setMessage(message)
        builder.setPositiveButton("OK",
            { dialog, which ->
                okAlert(dialog)
            })
        if (isCancelHide){
            builder.setNegativeButton("Cancel",
                { dialog, which ->
                    cancelAlert(dialog)
                })
        }
        builder.create()
        builder.show()
        return builder
    }

    fun changeWindowNavigationBarColor(activity: Activity,color: Int) {
        activity.window.navigationBarColor = color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            activity.window.setElevation(0F)
        }
    }

    fun showCustomDialog(context: Context, message: String?, txtNegative: String?, txtPositive: String?, note: String?, isNoteShow: Boolean = false, isNegativeShow: Boolean = false, positiveAlert : (dialog : DialogInterface) -> Unit?, negativeAlert : (dialog : DialogInterface) -> Unit?,changeLayout:Boolean = false) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (changeLayout){
            dialog.setContentView(R.layout.food_preparing_alert)
        }else{
            dialog.setContentView(R.layout.custom_dialog_layout)

            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            val imgIcon: ImageView = dialog.findViewById(R.id.imgIcon)
            val txtMessage: TextView = dialog.findViewById(R.id.txtMessage)
            val btnDialogNegative: TextView = dialog.findViewById(R.id.btnDialogNegative)
            val btnDialogPositive: TextView = dialog.findViewById(R.id.btnDialogPositive)
            val txtNote : TextView = dialog.findViewById(R.id.txtNote)

            txtMessage.text = message
            btnDialogNegative.text = txtNegative
            btnDialogPositive.text = txtPositive
            txtNote.text = note

            if (isNegativeShow){
                btnDialogNegative.visibility = View.VISIBLE
            }
            else
            { btnDialogNegative.visibility = View.INVISIBLE
            }
            if (isNoteShow){
                txtNote.visibility = View.VISIBLE
            } else
            { txtNote.visibility = View.GONE
            }

            btnDialogNegative.setOnClickListener {
                negativeAlert(dialog)
            }
            btnDialogPositive.setOnClickListener {
                positiveAlert(dialog)
            }
        }
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setLayout(width, height)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)



        dialog.show()
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun showKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

}

