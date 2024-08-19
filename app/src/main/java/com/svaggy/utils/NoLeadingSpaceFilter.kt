package com.svaggy.utils

import android.text.InputFilter
import android.text.Spanned

class NoLeadingSpaceFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // Prevent space as the first character
        if (dstart == 0 && source?.toString()?.startsWith(" ") == true) {
            return ""
        }
        return null // Accept the input
    }
}