package com.svaggy.utils

import android.content.Context
import android.widget.Toast
import java.math.BigDecimal
import java.math.RoundingMode

fun String.toast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun String.prettyPrint(d: Double): String {
    val i = d.toInt()
    return if (d == i.toDouble()) i.toString() else BigDecimal(d).setScale(
        2, RoundingMode.HALF_EVEN
    ).toString()
}
