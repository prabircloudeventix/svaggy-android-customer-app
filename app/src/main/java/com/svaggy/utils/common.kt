package com.svaggy.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.svaggy.R
import com.svaggy.app.SvaggyApplication
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Common Alert dialog app extension function
 */

//fun Context.alertDialog(
//    title: String? = "",
//    message: String? = "",
//    okAlert: (dialog: DialogInterface) -> Unit? = {},
//    cancelAlert: (dialog: DialogInterface) -> Unit? = {},
//    isCancelHide: Boolean = false
//): AlertDialog.Builder {
//    val builder = AlertDialog.Builder(this)
//    builder.setTitle(title)
//    builder.setIcon(R.mipmap.ic_launcher_round)
//    builder.setMessage(message)
//    builder.setPositiveButton(
//        "OK"
//    ) { dialog, _ ->
//        okAlert(dialog)
//    }
//    if (isCancelHide) {
//        builder.setNegativeButton(
//            "Cancel"
//        ) { dialog, _ ->
//            cancelAlert(dialog)
//        }
//    }
//    builder.create()
//    builder.show()
//    return builder
//}




fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}

fun String?.defaultIfNullOrNullString(defaultValue : String = ""): String {
    return if (this == null || this.equals("null", ignoreCase = true)) defaultValue else this
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

fun updateDateInReturnDate(datePicker: DatePicker): String? {
    var strDate: String? = null
    datePicker.setMaxDate(System.currentTimeMillis());
    try {
        val day: Int = datePicker.dayOfMonth
        val month: Int = datePicker.month
        val year: Int = datePicker.year
        val calendar = Calendar.getInstance()
        calendar[year, month] = day
        val format = SimpleDateFormat("dd-MM-yyyy")
        strDate = format.format(calendar.time)
    } catch (
        e: Exception
    ) {
    }
    return strDate
}

/*
private fun showDialogCalendar(){
    val viewDialog = layoutInflater.inflate(R.layout.sheet_delivery_date, null)
    val dialog = BottomSheetDialog(requireContext())
    dialog.setOnShowListener {
        val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        (viewDialog.getParent() as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
        val behavior = bottomSheetDialogFragment?.let {
            BottomSheetBehavior.from(it)
        }
        behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }
    val datePicker = viewDialog.findViewById<DatePicker>(R.id.datePicker)
    val btnCancel = viewDialog.findViewById<ImageView>(R.id.cancelImage)
    btnCancel.setOnClickListener {
        binding?.dateSelect?.text = prefUtils.updateDateInReturnDate(datePicker)
        dialog.dismiss()
    }
    dialog.setContentView(viewDialog)
    dialog.show()
}
 */
fun getCurrentTime24HourFormat(): String {
    val currentTime = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date(currentTime))
}
fun hasInternetConnection(application: SvaggyApplication): Boolean {
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

fun showCircularImageUsingGlide(
    imageUrl: String,
    loading: ProgressBar? = null,
    photoView: ImageView
) {
    val circularProgressDrawable = CircularProgressDrawable(photoView.context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 20f
    circularProgressDrawable.start()
    val options = RequestOptions().frame(1000).format(DecodeFormat.PREFER_RGB_565)
    Glide.with(photoView)
        .load(imageUrl)
        .format(DecodeFormat.PREFER_RGB_565)
        .circleCrop()
        .apply(options)
        .placeholder(circularProgressDrawable)
        .error(R.drawable.placeholder_cuisine)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .into(photoView)
}

fun formatDouble(d: Double): String {
    return if (d == d.toLong().toDouble()) {
        String.format("%.2f", d)
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
    builder.setPositiveButton("OK"
    ) { dialog, _ ->
        okAlert(dialog)
    }
    if (isCancelHide){
        builder.setNegativeButton("Cancel"
        ) { dialog, _ ->
            cancelAlert(dialog)
        }
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




fun Fragment.navigateToBack(onBackCall: () -> Unit = {}) {
    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackCall()
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
}