package com.svaggy.utils.countrycodepicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.svaggy.R
import java.util.*

internal class CountryCodeArrayAdapter(
    ctx: Context, countries: List<Country?>, private val mCountryCodePicker: CountryCodePicker
) : ArrayAdapter<Country?>(ctx, 0, countries) {
    override fun getView(
        position: Int, convertView: View?, parent: ViewGroup
    ): View {
        var viewConvert = convertView
        val country = getItem(position)
        val viewHolder: ViewHolder
        if (viewConvert == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            viewConvert = inflater.inflate(R.layout.item_country, parent, false)
            viewHolder.rlyMain = viewConvert.findViewById(R.id.item_country_rly)
            viewHolder.tvName = viewConvert.findViewById(R.id.country_name_tv)
            viewHolder.tvCode = viewConvert.findViewById(R.id.code_tv)
            viewHolder.imvFlag = viewConvert.findViewById(R.id.flag_imv)
            viewHolder.viewDivider = viewConvert.findViewById(R.id.preference_divider_view)
            viewConvert.tag = viewHolder
            setData(country, viewHolder)
            return viewConvert
        } else {
            viewHolder = viewConvert.tag as ViewHolder
            setData(country, viewHolder)
            return viewConvert
        }
    }

    private fun setData(
        country: Country?, viewHolder: ViewHolder
    ) {
        if (country == null) {
            viewHolder.viewDivider?.visibility = View.VISIBLE
            viewHolder.tvName?.visibility = View.GONE
            viewHolder.tvCode?.visibility = View.GONE
            viewHolder.imvFlag?.visibility = View.GONE
        } else {
            viewHolder.viewDivider?.visibility = View.GONE
            viewHolder.tvName?.visibility = View.VISIBLE
            viewHolder.tvCode?.visibility = View.VISIBLE
            viewHolder.imvFlag?.visibility = View.VISIBLE
            val ctx = viewHolder.tvName?.context
            val name = country.name
            val iso = country.iso.uppercase(Locale.getDefault())
            val countryNameAndCode = ctx?.getString(R.string.country_name_and_code, name, iso)
            viewHolder.tvName?.text = countryNameAndCode
            if (mCountryCodePicker.isHidePhoneCode) {
                viewHolder.tvCode?.visibility = View.GONE
            } else {
                viewHolder.tvCode?.text = ctx?.getString(R.string.cntry_phn_code, country.phoneCode)
            }
            val typeface = mCountryCodePicker.typeFace
            if (typeface != null) {
                viewHolder.tvCode?.typeface = typeface
                viewHolder.tvName?.typeface = typeface
            }
            viewHolder.imvFlag?.setImageResource(CountryUtils.getFlagDrawableResId(country))
            val color = mCountryCodePicker.dialogTextColor
            if (color != mCountryCodePicker.dialogTextColor) {
                viewHolder.tvCode?.setTextColor(color)
                viewHolder.tvName?.setTextColor(color)
            }
        }
    }

    private class ViewHolder {
        var rlyMain: ConstraintLayout? = null
        var tvName: TextView? = null
        var tvCode: TextView? = null
        var imvFlag: ImageView? = null
        var viewDivider: View? = null
    }
}