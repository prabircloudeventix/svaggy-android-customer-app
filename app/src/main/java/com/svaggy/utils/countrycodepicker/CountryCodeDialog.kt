package com.svaggy.utils.countrycodepicker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.svaggy.R
import com.svaggy.databinding.LayoutPickerDialogBinding
import java.util.*
import kotlin.math.roundToInt

class CountryCodeDialog(countryCodePicker: CountryCodePicker) : Dialog(countryCodePicker.context) {
    private var mEdtSearch: EditText? = null
    private var mTvNoResult: TextView? = null
    private var mTvTitle: TextView? = null
    private var codeTv: TextView? = null
    private var mLvCountryDialog: ListView? = null
    private var mRlyDialog: ConstraintLayout? = null
    private var masterCountries: List<Country> = ArrayList()
    private var mFilteredCountries: List<Country?> = ArrayList()
    private var mInputMethodManager: InputMethodManager? = null
    private var mArrayAdapter: CountryCodeArrayAdapter? = null
    private var mTempCountries: MutableList<Country?> = ArrayList()
    private var mCountryCodePicker: CountryCodePicker = countryCodePicker
    private lateinit var countryCodeDialogBinding: LayoutPickerDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_picker_dialog)
        countryCodeDialogBinding = LayoutPickerDialogBinding.inflate(layoutInflater)
        setContentView(countryCodeDialogBinding.root)
        setupUI()
        setupData()
    }

    private fun setupUI() {
        mRlyDialog = findViewById(R.id.dialog_rly)
        mLvCountryDialog = findViewById(R.id.country_dialog_lv)
        mTvTitle = findViewById(R.id.title_tv)
        mEdtSearch = findViewById(R.id.search_edt)
        mTvNoResult = findViewById(R.id.no_result_tv)
        codeTv = findViewById(R.id.code_tv)
    }

    private fun setupData() {
        if (mCountryCodePicker.typeFace != null) {
            val typeface = mCountryCodePicker.typeFace
            mTvTitle?.typeface = typeface
            mEdtSearch?.typeface = typeface
            mTvNoResult?.typeface = typeface
        }
        if (mCountryCodePicker.getBackgroundColor() != mCountryCodePicker.getBackgroundColor()) {
            mRlyDialog?.setBackgroundColor(mCountryCodePicker.getBackgroundColor())
        }
        if (mCountryCodePicker.dialogTextColor != mCountryCodePicker.dialogTextColor) {
            val color = mCountryCodePicker.dialogTextColor
            mTvTitle?.setTextColor(color)
            mTvNoResult?.setTextColor(color)
            mEdtSearch?.setTextColor(color)
            mEdtSearch?.setHintTextColor(adjustAlpha(color))
        }
        mCountryCodePicker.refreshCustomMasterList()
        mCountryCodePicker.refreshPreferredCountries()
        masterCountries = mCountryCodePicker.getCustomCountries(mCountryCodePicker)
        mFilteredCountries = filteredCountries
        setupListView(mLvCountryDialog)
        val ctx = mCountryCodePicker.context
        mInputMethodManager =
            ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setSearchBar()
    }

    private fun setupListView(listView: ListView?) {
        mArrayAdapter = CountryCodeArrayAdapter(
            context, mFilteredCountries, mCountryCodePicker
        )
        if (!mCountryCodePicker.isSelectionDialogShowSearch) {
            val params = listView?.layoutParams as RelativeLayout.LayoutParams
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            listView.layoutParams = params
        }
        val listener = OnItemClickListener { _, _, position, _ ->
            if (mFilteredCountries == null) {
                Log.e(
                    TAG, "no filtered countries found! This should not be happened, Please report!"
                )
                return@OnItemClickListener
            }
            if (mFilteredCountries.size < position || position < 0) {
                Log.e(
                    TAG, "Something wrong with the ListView. Please report this!"
                )
                return@OnItemClickListener
            }
            val country = mFilteredCountries[position] ?: return@OnItemClickListener
            mCountryCodePicker.selectedCountry = country
            mInputMethodManager?.hideSoftInputFromWindow(mEdtSearch?.windowToken, 0)
            dismiss()
        }
        listView?.onItemClickListener = listener
        listView?.adapter = mArrayAdapter
    }

    private fun adjustAlpha(color: Int): Int {
        val alpha = (Color.alpha(color) * 0.7.toFloat()).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun setSearchBar() {
        if (mCountryCodePicker.isSelectionDialogShowSearch) {
            setTextWatcher()
        } else {
            mEdtSearch?.visibility = View.GONE
        }
    }

    /**
     * add textChangeListener, to apply new query each time editText get text changed.
     */
    private fun setTextWatcher() {
        if (mEdtSearch == null) return
        mEdtSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                applyQuery(s.toString())
            }
        })
        if (mCountryCodePicker.isKeyboardAutoPopOnSearch) {
            if (mInputMethodManager != null) {
                mInputMethodManager?.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED, 0
                )
            }
        }
    }

    /**
     * Filter country list for given keyWord / query.
     * Lists all countries that contains @param query in country's name, name code or phone code.
     *
     * @param query : text to match against country name, name code or phone code
     */
    private fun applyQuery(query: String) {
        var query2 = query
        mTvNoResult?.visibility = View.GONE
        query2 = query2.lowercase(Locale.getDefault())
        if (query2.isNotEmpty() && query2[0] == '+') {
            query2 = query2.substring(1)
        }
        mFilteredCountries = getFilteredCountries(query2)
        if (mFilteredCountries.isEmpty()) {
            mTvNoResult?.visibility = View.VISIBLE
        }
        mArrayAdapter?.notifyDataSetChanged()
    }

    private val filteredCountries: List<Country?>
        get() = getFilteredCountries("")

    private fun getFilteredCountries(query: String): List<Country?> {
        if (mTempCountries == null) {
            mTempCountries = ArrayList()
        } else {
            mTempCountries.clear()
        }
        val preferredCountries = mCountryCodePicker.preferredCountries
        if (preferredCountries != null && preferredCountries.isNotEmpty()) {
            for (country in preferredCountries) {
                if (country.isEligibleForQuery(query)) {
                    mTempCountries.add(country)
                }
            }
            if (mTempCountries.size > 0) {
                mTempCountries.add(null)
            }
        }
        for (country in masterCountries) {
            if (country.isEligibleForQuery(query)) {
                mTempCountries.add(country)
            }
        }
        return mTempCountries
    }

    companion object {
        private const val TAG = "CountryCodeDialog"
    }
}