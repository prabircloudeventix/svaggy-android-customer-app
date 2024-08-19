package com.svaggy.utils.countrycodepicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.TelephonyManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.svaggy.BuildConfig
import com.svaggy.R
import com.svaggy.ui.activities.auth.PhoneLogInActivity
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import java.util.*
import kotlin.math.roundToInt

class CountryCodePicker : ConstraintLayout {
    private val countryDefault: String? = Locale.getDefault().country
    private var mBackgroundColor: Int = defaultBackgroundColor
    private var mDefaultCountryCode = 0
    private var mDefaultCountryNameCode: String? = null
    private var mPhoneUtil: PhoneNumberUtil? = null
    private var mPhoneNumberWatcher: PhoneNumberWatcher? = null
    private val mPhoneNumberInputValidityListener: PhoneNumberInputValidityListener? = null
    private var mTvSelectedCountry: TextView? = null
    private val mRegisteredPhoneNumberTextView: TextView? = null
    private var mRlyHolder: ConstraintLayout? = null
    private var mImvArrow: ImageView? = null
    private var mImvFlag: ImageView? = null
    private var mSelectedCountry: Country? = null
    private var defaultCountry: Country? = null
    private var countryCodeHolderClickListener: OnClickListener? = null
    private var mHideNameCode = false
    private var mShowFullName = false
    private var mShowFlag = false
    var isSelectionDialogShowSearch = true
    var setCountryCode = false
    var preferredCountries: List<Country>? = null
    private lateinit var mCountryPreference: String
    private var customCountries: List<Country> = ArrayList()

    /**
     * To provide definite set of countries when selection dialog is opened.
     * Only custom master countries, if defined, will be there is selection dialog to select from.
     * To set any country in preference, it must be included in custom master countries, if defined
     * When not defined or null or blank is set, it will use library's default master list
     * Custom master list will only limit the visibility of irrelevant country from selection dialog.
     * But all other functions like setCountryForCodeName() or setFullNumber() will consider all the
     * countries.
     *
     * @param customMasterCountries is country name codes separated by comma. e.g. "us,in,nz"
     * if null or "" , will remove custom countries and library default will be used.
     */
    private var customMasterCountries: String? = null
    var isKeyboardAutoPopOnSearch = true
    private var mIsClickable = true
    private lateinit var mCountryCodeDialog: CountryCodeDialog
    private var mHidePhoneCode = false
    private var mTextColor: Int = defaultContentColor
    var dialogTextColor: Int = defaultContentColor
    var typeFace: Typeface? = null
    private var mIsHintEnabled = true
    private var mIsEnablePhoneNumberWatcher = true
    private var mSetCountryByTimeZone = true
    private val mOnCountryChangeListener: OnCountryChangeListener? = null
    private var activity: PhoneLogInActivity? = null

    constructor(context: Context) : super(context) {
        if (!isInEditMode) init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    ) {
        if (!isInEditMode) init(attrs)
        //if (context is LoginPhnNo) activity = context
    }

    constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        if (!isInEditMode) init(attrs)
    }

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        if (!isInEditMode) init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        View.inflate(context, R.layout.layout_code_picker, this)
        mTvSelectedCountry = findViewById(R.id.selected_country_tv)
        mRlyHolder = findViewById(R.id.country_code_holder_rly)
        mImvArrow = findViewById(R.id.arrow_imv)
        mImvFlag = findViewById(R.id.flag_imv)
        applyCustomProperty(attrs)
        countryCodeHolderClickListener = OnClickListener {
            if (isClickable) {
                mCountryCodeDialog = CountryCodeDialog(this)
                mCountryCodeDialog.show()
            }
        }
        mRlyHolder?.setOnClickListener(countryCodeHolderClickListener)
    }

    private fun applyCustomProperty(attrs: AttributeSet?) {
        mPhoneUtil = PhoneNumberUtil.createInstance(context)
        val theme = context.theme
        val a = theme.obtainStyledAttributes(attrs, R.styleable.CountryCodePicker, 0, 0)
        try {
            mHidePhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hidePhoneCode, false)
            mShowFullName = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFullName, false)
            mHideNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hideNameCode, false)
            mIsHintEnabled = a.getBoolean(R.styleable.CountryCodePicker_ccp_enableHint, true)
            mIsEnablePhoneNumberWatcher =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_enablePhoneAutoFormatter, true)
            isKeyboardAutoPopOnSearch =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_keyboardAutoPopOnSearch, true)
            customMasterCountries =
                a.getString(R.styleable.CountryCodePicker_ccp_customMasterCountries)
            refreshCustomMasterList()
            mCountryPreference =
                a.getString(R.styleable.CountryCodePicker_ccp_countryPreference) ?: ""
            refreshPreferredCountries()
            applyCustomPropertyOfDefaultCountryNameCode(a)
            mShowFlag = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFlag, true)
            applyCustomPropertyOfColor(a)
            val fontPath = a.getString(R.styleable.CountryCodePicker_ccp_textFont)
            if (fontPath != null && fontPath.isNotEmpty()) setTypeFace(fontPath)
            val textSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_textSize, 0)
            if (textSize > 0) {
                mTvSelectedCountry?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                setArrowSize(textSize)
            } else {
                val dm = context.resources.displayMetrics
                val defaultSize = (18 * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
                setTextSize(defaultSize)
            }
            val arrowSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_arrowSize, 0)
            if (arrowSize > 0) setArrowSize(arrowSize)
            isSelectionDialogShowSearch =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_selectionDialogShowSearch, true)
            isClickable = a.getBoolean(R.styleable.CountryCodePicker_ccp_clickable, true)
            mSetCountryByTimeZone =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_setCountryByTimeZone, true)
            if (mDefaultCountryNameCode == null || mDefaultCountryNameCode.isNullOrEmpty()) {
                setDefaultCountryFlagAndCode()
            }
        } catch (e: Exception) {
            if (isInEditMode) {
                mTvSelectedCountry?.text = context.getString(R.string.cntry_phn_code)
            } else {
                mTvSelectedCountry?.text = e.message
            }
        } finally {
            a.recycle()
        }
    }

    private fun applyCustomPropertyOfDefaultCountryNameCode(tar: TypedArray) {
        mDefaultCountryNameCode = tar.getString(R.styleable.CountryCodePicker_ccp_defaultNameCode)

        if (mDefaultCountryNameCode == null || mDefaultCountryNameCode.isNullOrEmpty()) return
        if ((mDefaultCountryNameCode ?: "").trim { it <= ' ' }.isEmpty()) {
            mDefaultCountryNameCode = null
            return
        }
        setDefaultCountryUsingNameCode(mDefaultCountryNameCode)
        selectedCountry = defaultCountry
    }


    private fun applyCustomPropertyOfColor(arr: TypedArray) {
        val textColor: Int = if (isInEditMode) {
            arr.getColor(
                R.styleable.CountryCodePicker_ccp_textColor, defaultContentColor
            )
        } else {
            arr.getColor(
                R.styleable.CountryCodePicker_ccp_textColor,
                ContextCompat.getColor(context, R.color.black)
            )
        }
        if (textColor != 0) setTextColor(textColor)
        dialogTextColor = arr.getColor(
            R.styleable.CountryCodePicker_ccp_dialogTextColor, defaultContentColor
        )
        mBackgroundColor = arr.getColor(
            R.styleable.CountryCodePicker_ccp_backgroundColor, Color.TRANSPARENT
        )
        if (mBackgroundColor != Color.TRANSPARENT) mRlyHolder?.setBackgroundColor(
            mBackgroundColor
        )
    }

    private fun setDefaultCountry(defaultCountry: Country) {
        this.defaultCountry = defaultCountry
    }

    var selectedCountry: Country?
        get() = mSelectedCountry
        set(selectedCountry) {
            var selectedCountry3 = selectedCountry
            mSelectedCountry = selectedCountry3
            val ctx = context
            if (selectedCountry3 == null) selectedCountry3 =
                CountryUtils.getByCode(ctx, mDefaultCountryCode)
            if (mRegisteredPhoneNumberTextView != null) {
                setPhoneNumberWatcherToTextView(
                    mRegisteredPhoneNumberTextView,
                    selectedCountry3.iso.uppercase(Locale.getDefault())
                )
            }
            val phoneCode = selectedCountry3.phoneCode
            if (setCountryCode) {
                mTvSelectedCountry?.setTextColor(Color.BLACK)
                mTvSelectedCountry?.text = ctx.getString(R.string.cntry_phn_code, phoneCode)
                mTvSelectedCountry?.setTypeface(mTvSelectedCountry?.typeface, Typeface.BOLD)
                mTvSelectedCountry?.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.dp13)
                )
                mImvArrow?.background = ContextCompat.getDrawable(ctx, R.color.black)
            }
            if (mHideNameCode) {
                mTvSelectedCountry?.text = ctx.getString(R.string.cntry_phn_code, phoneCode)
            } else {
                if (mShowFullName) {
                    if (mHidePhoneCode) {
                        mTvSelectedCountry?.text = phoneCode
                    } else {
                        mTvSelectedCountry?.text = ctx.getString(
                            R.string.country_name_and_code, "", phoneCode
                        )
                    }
                } else {
                    if (mHidePhoneCode) {
                        mTvSelectedCountry?.text = phoneCode
                    } else {
                        mTvSelectedCountry?.text =
                            ctx.getString(R.string.cntry_phn_code, phoneCode)
                    }
                }
            }
            if (mShowFlag) {
                mImvFlag?.setImageResource(CountryUtils.getFlagDrawableResId(selectedCountry3))
            }
            mOnCountryChangeListener?.onCountrySelected(selectedCountry3)
            activity?.onCountrySelected(selectedCountry3)
            if (mIsHintEnabled) setPhoneNumberHint()
        }

    fun refreshPreferredCountries() {
        if (mCountryPreference.isEmpty()) {
            preferredCountries = null
            return
        }
        val localCountryList: MutableList<Country> = ArrayList()
        for (nameCode in mCountryPreference.split(",".toRegex()).toTypedArray()) {
            val country = CountryUtils.getByNameCodeFromCustomCountries(
                context, customCountries, nameCode
            ) ?: continue
            if (isAlreadyInList(country, localCountryList)) continue
            localCountryList.add(country)
        }
        preferredCountries = if (localCountryList.size == 0) {
            null
        } else {
            localCountryList
        }
    }

    /**
     * this will load mPreferredCountries based on mCountryPreference
     */
    fun refreshCustomMasterList() {
        if (customMasterCountries == null || customMasterCountries.isNullOrEmpty()) {
            customCountries = emptyList()
            return
        }
        val localCountries: MutableList<Country> = ArrayList()
        val split = (customMasterCountries ?: "").split(",".toRegex()).toTypedArray()
        for (i in split.indices) {
            val nameCode = split[i]
            val country = CountryUtils.getByNameCodeFromAllCountries(context, nameCode) ?: continue
            if (isAlreadyInList(country, localCountries)) continue
            localCountries.add(country)
        }
        customCountries = if (localCountries.size == 0) {
            emptyList()
        } else {
            localCountries
        }
    }

    /**
     * Get custom country by preference
     *
     * @param codePicker picker for the source of country
     * @return List of country
     */
    fun getCustomCountries(codePicker: CountryCodePicker): List<Country> {
        codePicker.refreshCustomMasterList()
        return codePicker.customCountries.ifEmpty {
            CountryUtils.getAllCountries(codePicker.context)
        }
    }

    /**
     * This will match name code of all countries of list against the country's name code.
     *
     * @param countries list of countries against which country will be checked.
     * @return if country name code is found in list, returns true else return false
     */
    private fun isAlreadyInList(
        country: Country?, countries: List<Country>?
    ): Boolean {
        if (country == null || countries == null) return false
        for (i in countries.indices) {
            if (countries[i].iso.equals(country.iso, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * This function removes possible country code from fullNumber and set rest of the number as
     * carrier number.
     *
     * @param fullNumber combination of country code and carrier number.
     * @param country    selected country in CCP to detect country code part.
     */

    /**
     * This method is not encouraged because this might set some other country which have same country
     * code as of yours. e.g 1 is common for US and canada.
     * If you are trying to set US ( and mCountryPreference is not set) and you pass 1 as @param
     * mDefaultCountryCode, it will set canada (prior in list due to alphabetical order)
     * Rather use setDefaultCountryUsingNameCode("us"); or setDefaultCountryUsingNameCode("US");
     *
     *
     * Default country code defines your default country.
     * Whenever invalid / improper number is found in setCountryForPhoneCode() /  setFullNumber(), it
     * CCP will set to default country.
     * This function will not set default country as selected in CCP. To set default country in CCP
     * call resetToDefaultCountry() right after this call.
     * If invalid mDefaultCountryCode is applied, it won't be changed.
     *
     * @param defaultCountryCode code of your default country
     * if you want to set IN +91(India) as default country, mDefaultCountryCode =  91
     * if you want to set JP +81(Japan) as default country, mDefaultCountryCode =  81
     */
    @Deprecated("")
    fun setDefaultCountryUsingPhoneCode(defaultCountryCode: Int) {
        val defaultCountry = CountryUtils.getByCode(context, defaultCountryCode)
        mDefaultCountryCode = defaultCountryCode
        setDefaultCountry(defaultCountry)
    }

    /**
     * Default country name code defines your default country.
     * Whenever invalid / improper name code is found in setCountryForNameCode(), CCP will set to
     * default country.
     * This function will not set default country as selected in CCP. To set default country in CCP
     * call resetToDefaultCountry() right after this call.
     * If invalid mDefaultCountryCode is applied, it won't be changed.
     *
     * @param countryIso code of your default country
     * if you want to set IN +91(India) as default country, mDefaultCountryCode =  "IN" or "in"
     * if you want to set JP +81(Japan) as default country, mDefaultCountryCode =  "JP" or "jp"
     */
    fun setDefaultCountryUsingNameCode(countryIso: String?) {
        val defaultCountry =
            CountryUtils.getByNameCodeFromAllCountries(context, countryIso) ?: return
        mDefaultCountryNameCode = defaultCountry.iso
        setDefaultCountry(defaultCountry)
    }

    /**
     * Get Country Code of default country
     * i.e if default country is IN +91(India)  returns: "91"
     * if default country is JP +81(Japan) returns: "81"
     */
    private val defaultCountryCode: String
        get() = defaultCountry?.phoneCode ?: ""

    /**
     * * To get code of default country as Integer.
     *
     * @return integer value of default country code in CCP
     * i.e if default country is IN +91(India)  returns: 91
     * if default country is JP +81(Japan) returns: 81
     */

    val selectedCountryCode: String
        get() = mSelectedCountry?.phoneCode ?: ""

    private fun setPhoneNumberWatcherToTextView(
        textView: TextView, countryNameCode: String
    ) {
        if (mIsEnablePhoneNumberWatcher) {
            if (mPhoneNumberWatcher == null) {
                mPhoneNumberWatcher = PhoneNumberWatcher(countryNameCode)
                textView.addTextChangedListener(mPhoneNumberWatcher)
            } else {
                if (!mPhoneNumberWatcher?.previousCountryCode.equals(
                        countryNameCode, ignoreCase = true
                    )
                ) {
                    mPhoneNumberWatcher = PhoneNumberWatcher(countryNameCode)
                }
            }
        }
    }

    /**
     * This function combines selected country code from CCP and carrier number from @param
     * editTextCarrierNumber
     *
     * @return Full number is countryCode + carrierNumber i.e countryCode= 91 and carrier number=
     * 8866667722, this will return "918866667722"
     */
    fun setTextColor(contentColor: Int) {
        mTextColor = contentColor
        mTvSelectedCountry?.setTextColor(contentColor)
        mImvArrow?.setColorFilter(contentColor, PorterDuff.Mode.SRC_IN)
    }

    fun getBackgroundColor(): Int {
        return mBackgroundColor
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        mBackgroundColor = backgroundColor
        mRlyHolder?.setBackgroundColor(backgroundColor)
    }

    private fun setTextSize(textSize: Int) {
        if (textSize > 0) {
            mTvSelectedCountry?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
            setArrowSize(textSize)
        }
    }

    /**
     * Modifies size of downArrow in CCP view
     *
     * @param arrowSize size in pixels
     */
    private fun setArrowSize(arrowSize: Int) {
        if (arrowSize > 0) {
            val params = mImvArrow?.layoutParams as LayoutParams
            params.width = arrowSize
            params.height = arrowSize
            mImvArrow?.layoutParams = params
        }
    }

    private fun setTypeFace(fontAssetPath: String) {
        try {
            val typeFace = Typeface.createFromAsset(context.assets, fontAssetPath)
            this.typeFace = typeFace
            mTvSelectedCountry?.typeface = typeFace
        } catch (e: Exception) {
        }
    }

    override fun isClickable(): Boolean {
        return mIsClickable
    }

    override fun setClickable(isClickable: Boolean) {
        mIsClickable = isClickable
        mRlyHolder?.setOnClickListener(if (isClickable) countryCodeHolderClickListener else null)
        mRlyHolder?.isClickable = isClickable
        mRlyHolder?.isEnabled = isClickable
    }

    var isHidePhoneCode: Boolean
        get() = mHidePhoneCode
        @SuppressLint("StringFormatMatches")
        set(hidePhoneCode) {
            mHidePhoneCode = hidePhoneCode
            val ctx = context
            val phoneCode = mSelectedCountry?.phoneCode
            if (mHideNameCode) {
                mTvSelectedCountry?.text = ctx.getString(R.string.cntry_phn_code, phoneCode)
                return
            }
            if (mShowFullName) {
                if (mHidePhoneCode) {
                    mTvSelectedCountry?.text = phoneCode
                } else {
                    val country =
                        ctx.getString(R.string.country_name_and_code, "", phoneCode)
                    mTvSelectedCountry?.text = country
                }
            } else {
                val iso = mSelectedCountry?.iso ?: "".uppercase(Locale.getDefault())
                if (mHidePhoneCode) {
                    mTvSelectedCountry?.text = iso
                } else {
                    mTvSelectedCountry?.text =
                        ctx.getString(R.string.country_name_and_code, phoneCode)
                }
            }
        }

    private fun setPhoneNumberHint() {
        if (mRegisteredPhoneNumberTextView == null || mSelectedCountry == null || mSelectedCountry?.iso == null) {
            return
        }
        val iso = mSelectedCountry?.iso ?: "".uppercase(Locale.getDefault())
        val mobile = PhoneNumberUtil.PhoneNumberType.MOBILE
        val phoneNumber = mPhoneUtil?.getExampleNumberForType(iso, mobile)
        if (phoneNumber == null) {
            mRegisteredPhoneNumberTextView.hint = ""
            return
        }

        val hint = mPhoneUtil?.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
        mRegisteredPhoneNumberTextView.hint = hint
    }

    private val phoneNumber: Phonenumber.PhoneNumber?
        get() = try {
            var iso: String? = null
            if (mSelectedCountry != null) iso =
                mSelectedCountry?.iso ?: "".uppercase(Locale.getDefault())
            if (mRegisteredPhoneNumberTextView == null) {
                Log.w(
                    TAG, context.getString(R.string.went_wrong)
                )

            }
            mPhoneUtil?.parse(mRegisteredPhoneNumberTextView?.text.toString(), iso)
        } catch (ignored: NumberParseException) {
            null
        }

    val isValid: Boolean
        get() {
            val phoneNumber = phoneNumber
            return phoneNumber != null && mPhoneUtil?.isValidNumber(phoneNumber) ?: false
        }

    private fun setDefaultCountryFlagAndCode() {
        val ctx = context
        val manager: TelephonyManager =
            ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (manager == null) {
            Log.e(TAG, "Can't access TelephonyManager. Using default county code")
            setEmptyDefault(defaultCountryCode)
            return
        }
        try {
            val simCountryIso = manager.simCountryIso
            if (simCountryIso == null || simCountryIso.isEmpty()) {
                val iso = manager.networkCountryIso
                if (iso == null || iso.isEmpty()) {
                    enableSetCountryByTimeZone()
                } else {
                    setEmptyDefault(iso)
                }
            } else {
                setEmptyDefault(simCountryIso)


            }
        } catch (e: Exception) {
            Log.e(
                TAG, "Error when getting sim country, error = $e"
            )
            setEmptyDefault(defaultCountryCode)
        }
    }

    private fun setEmptyDefault() {
        setEmptyDefault(null)
    }

    private fun setEmptyDefault(countryCode: String?) {
        var countryCode1 = countryCode
        if (countryCode1 == null || countryCode1.isEmpty()) {
            countryCode1 =
                if (mDefaultCountryNameCode == null || mDefaultCountryNameCode.isNullOrEmpty()) {
                    if (countryDefault == null || countryDefault.isEmpty()) {
                        DEFAULT_ISO_COUNTRY
                    } else {
                        countryDefault
                    }
                } else {
                    mDefaultCountryNameCode
                }
        }
        if (mIsEnablePhoneNumberWatcher && mPhoneNumberWatcher == null) {
            mPhoneNumberWatcher = PhoneNumberWatcher(countryCode)
        }
        setDefaultCountryUsingNameCode(countryCode)
        selectedCountry = defaultCountry
    }

    private fun enableSetCountryByTimeZone() {
        if (mDefaultCountryNameCode != null && mDefaultCountryNameCode.isNullOrEmpty()) return
        if (mRegisteredPhoneNumberTextView != null) return
        if (mSetCountryByTimeZone) {
            val tz = TimeZone.getDefault()
            val countryIsos = CountryUtils.getCountryIsoByTimeZone(context, tz.id)
            if (countryIsos == null) {
                setEmptyDefault()
            } else {
                setDefaultCountryUsingNameCode(countryIsos[0])
                selectedCountry = defaultCountry
            }
        }
        mSetCountryByTimeZone = true
    }

    interface OnCountryChangeListener {
        fun onCountrySelected(selectedCountry: Country?)
    }

    interface OnCountryRegister {
        fun onCountrySelected(selectedCountry: Country?)
    }

    private interface PhoneNumberInputValidityListener {
        fun onFinish(ccp: CountryCodePicker?, isValid: Boolean)
    }

    private inner class PhoneNumberWatcher constructor(
        countryCode: String?
    ) : PhoneNumberFormattingTextWatcher(countryCode) {
        private var lastValidity = false
        var previousCountryCode: String? = ""

        override fun onTextChanged(
            s: CharSequence, start: Int, before: Int, count: Int
        ) {
            super.onTextChanged(s, start, before, count)
            try {
                var iso: String? = null
                if (mSelectedCountry != null) iso =
                    mSelectedCountry?.phoneCode ?: "".uppercase(Locale.getDefault())
                val phoneNumber = mPhoneUtil?.parse(s.toString(), iso)
                mPhoneUtil?.getRegionCodeForNumber(phoneNumber)
            } catch (ignored: NumberParseException) {
            }
            if (mPhoneNumberInputValidityListener != null) {
                val validity = isValid
                if (validity != lastValidity) {
                    mPhoneNumberInputValidityListener.onFinish(this@CountryCodePicker, validity)
                }
                lastValidity = validity
            }
        }

        init {
            previousCountryCode = countryCode
        }
    }

    companion object {
        private val TAG = CountryCodePicker::class.java.simpleName
        private const val DEFAULT_ISO_COUNTRY = "ID"
        const val defaultContentColor = 0
        const val defaultBackgroundColor = Color.TRANSPARENT

        @RequiresApi(Build.VERSION_CODES.M)
        fun getColor(context: Context, id: Int): Int {
            val version = Build.VERSION.SDK_INT
            return if (version >= 23) {
                context.getColor(id)
            } else {
                ContextCompat.getColor(context, id)
            }
        }
    }
}