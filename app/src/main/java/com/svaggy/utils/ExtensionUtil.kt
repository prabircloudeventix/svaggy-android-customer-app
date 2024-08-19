package com.svaggy.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonParser
import com.svaggy.R
import com.svaggy.databinding.SnackbarBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Response

fun Activity.updateStatusBarColor(color: String?, isLightText: Boolean = false) {
    val window: Window = window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = Color.parseColor(color)

    val decor = window.decorView
    var flags = decor.systemUiVisibility

    flags = if (isLightText) {
        flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else {
        flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }

    decor.systemUiVisibility = flags
}

fun View.showSnackBar(title: String, cancelFun: () -> Unit = {}) {
    val snackView = View.inflate(context, R.layout.snackbar, null)
    val binding = SnackbarBinding.bind(snackView)
    val snackBar = Snackbar.make(this, "", Snackbar.LENGTH_SHORT)
    (snackBar.view as ViewGroup).removeAllViews()
    (snackBar.view as ViewGroup).addView(binding.root)
    snackBar.view.setPadding(0, 0, 0, 0)
    snackBar.view.elevation = 0f

    snackBar.setBackgroundTint(
        ContextCompat.getColor(
            context,
            R.color.primaryColor
        )
    )
    binding.txtMessage.text = title
    binding.btnOk.setOnClickListener {
        cancelFun()
        snackBar.dismiss()
    }
    snackBar.show()
}

fun validateCardExpiryDate(expiryDate: String): Boolean {
    return expiryDate.matches("(?:0[1-9]|1[0-2])/[0-9]{2}".toRegex())
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}
fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}


fun EditText.isNotNullOrEmpty(errorString: String): Boolean {
    this.onChange { this.error = null }
    return if (this.text.toString().trim().isNullOrEmpty()) {
        this.error = errorString
        requestFocus()
        false
    } else {
        true
    }
}

fun EditText.onChange(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}




//fun <T> result(call: suspend () -> Response<T>): Flow<ApiResponse<T>> = flow {
//
//    emit(ApiResponse.Loading)
//    try {
//        val c = call()
//        c.let {
//            if (c.isSuccessful) {
//                emit(ApiResponse.Success(it.body()))
//
//            } else {
//               c.errorBody()?.let { errorBody ->
//                   errorBody.close()
//                   emit(ApiResponse.Failure(it.message()))
//                }
//            }
//        }
//
//    } catch (t:Throwable){
//        t.printStackTrace()
//        emit(ApiResponse.Failure(t.message.toString()))
//
//
//    }
//}
fun <T> result(call: suspend () -> Response<T>): Flow<ApiResponse<T>> = flow {
    emit(ApiResponse.Loading)
    try {
        val response = call()
        if (response.isSuccessful) {
            emit(ApiResponse.Success(response.body()))
        } else {
            response.errorBody()?.let { errorBody ->
                errorBody.close()
                emit(ApiResponse.Failure(response.message()))
            }
        }
    } catch (t: Throwable) {
        t.printStackTrace()
        // Instead of emitting the error directly here, use the catch operator
        throw t
    }
}.catch { t: Throwable ->
    // Handle the error emission in the catch operator
    emit(ApiResponse.Failure(t.message.toString()))
}
fun <T> result2(call: suspend () -> Response<T>): Flow<ApiResponse<T>> = flow {

    emit(ApiResponse.Loading)
    try {
        val c = call()
        c.let {
            if (c.isSuccessful) {
                emit(ApiResponse.Success(it.body()))

            } else {
                c.errorBody()?.let { errorBody ->
                    val errorMessage = errorBody.string()
                    val jsonObject = JsonParser.parseString(errorMessage).asJsonObject
                    val mess = jsonObject.get("message").asString
                    emit(ApiResponse.Failure(mess))
                    errorBody.close()
                } ?: run {
                    emit(ApiResponse.Failure("Unknown error occurred"))
                }
            }
        }

    } catch (t: Throwable) {
        t.printStackTrace()
        if (t.message != null)
            emit(ApiResponse.Failure(t.message))
    }
}

fun Fragment.setPOPUpToInclusiveFalse(navigateToId: Int, setUpDestinationId: Int, args: Bundle? = null) {
    findNavController().navigate(
        navigateToId,
        args,
        NavOptions.Builder().setPopUpTo(setUpDestinationId, false).build()
    )
}

fun Fragment.setPOPUpToInclusiveTrue(
    navigateToId: Int,
    setUpDestinationId: Int,
    args: Bundle? = null
) {
    findNavController().navigate(
        navigateToId,
        args,
        NavOptions.Builder().setPopUpTo(setUpDestinationId, true).build()
    )
}

fun Fragment.onBackPressedDispatcher(onBackCall: () -> Unit = {}) {
    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackCall()
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
}


fun AppCompatActivity.onBackPressedDispatcher(onBackPressed: () -> Unit = { finish() }) {
    onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed.invoke()
            }
        }
    )
}

fun Context.createCustomDialog(layoutResId: Int, builderFunction: AlertDialog.Builder.() -> Unit): AlertDialog {
    val builder = AlertDialog.Builder(this)
    val view = LayoutInflater.from(this).inflate(layoutResId, null)

    // Customize the dialog builder
    builder.builderFunction()

    // Set the custom view
    builder.setView(view)

    return builder.create()
}




