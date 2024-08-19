package com.svaggy.utils.countrycodepicker

import java.util.*

class Country(
    val iso: String, val phoneCode: String, val name: String
) {
    fun isEligibleForQuery(qry: String): Boolean {
        var query = qry
        query = query.lowercase(Locale.getDefault())
        return name.lowercase(Locale.getDefault())
            .contains(query) || iso.lowercase(Locale.getDefault())
            .contains(query) || phoneCode.lowercase(Locale.getDefault()).contains(query)
    }
}