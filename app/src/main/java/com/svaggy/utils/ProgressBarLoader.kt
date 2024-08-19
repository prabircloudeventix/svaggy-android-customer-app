package com.svaggy.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.svaggy.R
import java.lang.ref.WeakReference

class ProgressBarLoader {
    var dialog: Dialog? = null
        private set

    @JvmOverloads
    fun showProgressBar(
        context: Context,
        cancelable: Boolean = false,
        cancelListener: DialogInterface.OnCancelListener? = null
    ): Dialog {
        if (dialog == null)
            dialog =Dialog(context, R.style.customDialogTheme)
        dialog?.setContentView(LayoutInflater.from(context).inflate(R.layout.progress_bar, FrameLayout(context), false))
        dialog?.setCancelable(cancelable)
        dialog?.setOnCancelListener(cancelListener)
        dialog?.show()
        return dialog!!
    }







}





