package com.svaggy.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.svaggy.R

class ProgressLoaderNew {
    private var dialog: Dialog? = null

    fun start(
        context: Context,
        cancelable: Boolean = false,
        cancelListener: DialogInterface.OnCancelListener? = null
    ) {
        // Dismiss the old dialog if it's showing
        dialog?.dismiss()

        // Create a new dialog
        dialog = Dialog(context,R.style.customDialogTheme)
        dialog?.setContentView(LayoutInflater.from(context).inflate(R.layout.progress_bar, FrameLayout(context), false))
        dialog?.setCancelable(cancelable)
        dialog?.setOnCancelListener(cancelListener)
        dialog?.show()
    }

    fun stop() {
        dialog?.dismiss()
        dialog = null
    }
}