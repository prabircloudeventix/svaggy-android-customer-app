package com.svaggy.utils.countrycodepicker

import android.content.Context
import com.svaggy.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

internal object CountryUtils {
    private var countries: ArrayList<Country>? = null
    private lateinit var timeZoneAndCountryISOs: MutableMap<String, List<String>>

    @JvmStatic
    fun getFlagDrawableResId(country: Country): Int {
        return when (country.iso) {
            "af" -> R.drawable.flag_afghanistan
            "al" -> R.drawable.flag_albania
            "dz" -> R.drawable.flag_algeria
            "ad" -> R.drawable.flag_andorra
            "ao" -> R.drawable.flag_angola
            "aq" -> R.drawable.flag_antarctica
            "ar" -> R.drawable.flag_argentina
            "am" -> R.drawable.flag_armenia
            "aw" -> R.drawable.flag_aruba
            "au" -> R.drawable.flag_australia
            "at" -> R.drawable.flag_austria
            "az" -> R.drawable.flag_azerbaijan
            "bh" -> R.drawable.flag_bahrain
            "bd" -> R.drawable.flag_bangladesh
            "by" -> R.drawable.flag_belarus
            "be" -> R.drawable.flag_belgium
            "bz" -> R.drawable.flag_belize
            "bj" -> R.drawable.flag_benin
            "bt" -> R.drawable.flag_bhutan
            "bo" -> R.drawable.flag_bolivia
            "ba" -> R.drawable.flag_bosnia
            "bw" -> R.drawable.flag_botswana
            "br" -> R.drawable.flag_brazil
            "bn" -> R.drawable.flag_brunei
            "bg" -> R.drawable.flag_bulgaria
            "bf" -> R.drawable.flag_burkina_faso
            "mm" -> R.drawable.flag_myanmar
            "bi" -> R.drawable.flag_burundi
            "kh" -> R.drawable.flag_cambodia
            "cm" -> R.drawable.flag_cameroon
            "ca" -> R.drawable.flag_canada
            "cv" -> R.drawable.flag_cape_verde
            "cf" -> R.drawable.flag_central_african_republic
            "td" -> R.drawable.flag_chad
            "cl" -> R.drawable.flag_chile
            "cn" -> R.drawable.flag_china
            "cx" -> R.drawable.flag_christmas_island
            "cc" -> R.drawable.flag_cocos
            "co" -> R.drawable.flag_colombia
            "km" -> R.drawable.flag_comoros
            "cg" -> R.drawable.flag_republic_of_the_congo
            "cd" -> R.drawable.flag_democratic_republic_of_the_congo
            "ck" -> R.drawable.flag_cook_islands
            "cr" -> R.drawable.flag_costa_rica
            "hr" -> R.drawable.flag_croatia
            "cu" -> R.drawable.flag_cuba
            "cy" -> R.drawable.flag_cyprus
            "cz" -> R.drawable.flag_czech_republic
            "dk" -> R.drawable.flag_denmark
            "dj" -> R.drawable.flag_djibouti
            "tl" -> R.drawable.flag_timor_leste
            "ec" -> R.drawable.flag_ecuador
            "eg" -> R.drawable.flag_egypt
            "sv" -> R.drawable.flag_el_salvador
            "gq" -> R.drawable.flag_equatorial_guinea
            "er" -> R.drawable.flag_eritrea
            "ee" -> R.drawable.flag_estonia
            "et" -> R.drawable.flag_ethiopia
            "fk" -> R.drawable.flag_falkland_islands
            "fo" -> R.drawable.flag_faroe_islands
            "fj" -> R.drawable.flag_fiji
            "fi" -> R.drawable.flag_finland
            "fr" -> R.drawable.flag_france
            "pf" -> R.drawable.flag_french_polynesia
            "ga" -> R.drawable.flag_gabon
            "gm" -> R.drawable.flag_gambia
            "ge" -> R.drawable.flag_georgia
            "de" -> R.drawable.flag_germany
            "gh" -> R.drawable.flag_ghana
            "gi" -> R.drawable.flag_gibraltar
            "gr" -> R.drawable.flag_greece
            "gl" -> R.drawable.flag_greenland
            "gt" -> R.drawable.flag_guatemala
            "gn" -> R.drawable.flag_guinea
            "gw" -> R.drawable.flag_guinea_bissau
            "gy" -> R.drawable.flag_guyana
            "gf" -> R.drawable.flag_guyane
            "ht" -> R.drawable.flag_haiti
            "hn" -> R.drawable.flag_honduras
            "hk" -> R.drawable.flag_hong_kong
            "hu" -> R.drawable.flag_hungary
            "in" -> R.drawable.flag_india
            "id" -> R.drawable.flag_indonesia
            "ir" -> R.drawable.flag_iran
            "iq" -> R.drawable.flag_iraq
            "ie" -> R.drawable.flag_ireland
            "im" -> R.drawable.flag_isleof_man
            "il" -> R.drawable.flag_israel
            "it" -> R.drawable.flag_italy
            "ci" -> R.drawable.flag_cote_divoire
            "jp" -> R.drawable.flag_japan
            "jo" -> R.drawable.flag_jordan
            "kz" -> R.drawable.flag_kazakhstan
            "ke" -> R.drawable.flag_kenya
            "ki" -> R.drawable.flag_kiribati
            "kw" -> R.drawable.flag_kuwait
            "kg" -> R.drawable.flag_kyrgyzstan
            "ky" -> R.drawable.flag_cayman_islands
            "la" -> R.drawable.flag_laos
            "lv" -> R.drawable.flag_latvia
            "lb" -> R.drawable.flag_lebanon
            "ls" -> R.drawable.flag_lesotho
            "lr" -> R.drawable.flag_liberia
            "ly" -> R.drawable.flag_libya
            "li" -> R.drawable.flag_liechtenstein
            "lt" -> R.drawable.flag_lithuania
            "lu" -> R.drawable.flag_luxembourg
            "mo" -> R.drawable.flag_macao
            "mk" -> R.drawable.flag_macedonia
            "mg" -> R.drawable.flag_madagascar
            "mw" -> R.drawable.flag_malawi
            "my" -> R.drawable.flag_malaysia
            "mv" -> R.drawable.flag_maldives
            "ml" -> R.drawable.flag_mali
            "mt" -> R.drawable.flag_malta
            "mh" -> R.drawable.flag_marshall_islands
            "mr" -> R.drawable.flag_mauritania
            "mu" -> R.drawable.flag_mauritius
            "yt" -> R.drawable.flag_france
            "re" -> R.drawable.flag_france
            "mq" -> R.drawable.flag_france
            "mx" -> R.drawable.flag_mexico
            "fm" -> R.drawable.flag_micronesia
            "md" -> R.drawable.flag_moldova
            "mc" -> R.drawable.flag_monaco
            "mn" -> R.drawable.flag_mongolia
            "me" -> R.drawable.flag_of_montenegro
            "ma" -> R.drawable.flag_morocco
            "mz" -> R.drawable.flag_mozambique
            "na" -> R.drawable.flag_namibia
            "nr" -> R.drawable.flag_nauru
            "np" -> R.drawable.flag_nepal
            "nl" -> R.drawable.flag_netherlands
            "nc" -> R.drawable.flag_new_caledonia
            "nz" -> R.drawable.flag_new_zealand
            "ni" -> R.drawable.flag_nicaragua
            "ne" -> R.drawable.flag_niger
            "ng" -> R.drawable.flag_nigeria
            "nu" -> R.drawable.flag_niue
            "kp" -> R.drawable.flag_north_korea
            "no" -> R.drawable.flag_norway
            "om" -> R.drawable.flag_oman
            "pk" -> R.drawable.flag_pakistan
            "pw" -> R.drawable.flag_palau
            "pa" -> R.drawable.flag_panama
            "pg" -> R.drawable.flag_papua_new_guinea
            "py" -> R.drawable.flag_paraguay
            "pe" -> R.drawable.flag_peru
            "ph" -> R.drawable.flag_philippines
            "pn" -> R.drawable.flag_pitcairn_islands
            "pl" -> R.drawable.flag_poland
            "pt" -> R.drawable.flag_portugal
            "pr" -> R.drawable.flag_puerto_rico
            "qa" -> R.drawable.flag_qatar
            "ro" -> R.drawable.flag_romania
            "ru" -> R.drawable.flag_russian_federation
            "rw" -> R.drawable.flag_rwanda
            "bl" -> R.drawable.flag_saint_barthelemy
            "ws" -> R.drawable.flag_samoa
            "sm" -> R.drawable.flag_san_marino
            "st" -> R.drawable.flag_sao_tome_and_principe
            "sa" -> R.drawable.flag_saudi_arabia
            "sn" -> R.drawable.flag_senegal
            "rs" -> R.drawable.flag_serbia
            "sc" -> R.drawable.flag_seychelles
            "sl" -> R.drawable.flag_sierra_leone
            "sg" -> R.drawable.flag_singapore
            "sx" -> 0
            "sk" -> R.drawable.flag_slovakia
            "si" -> R.drawable.flag_slovenia
            "sb" -> R.drawable.flag_soloman_islands
            "so" -> R.drawable.flag_somalia
            "za" -> R.drawable.flag_south_africa
            "kr" -> R.drawable.flag_south_korea
            "es" -> R.drawable.flag_spain
            "lk" -> R.drawable.flag_sri_lanka
            "sh" -> R.drawable.flag_saint_helena
            "pm" -> R.drawable.flag_saint_pierre
            "sd" -> R.drawable.flag_sudan
            "sr" -> R.drawable.flag_suriname
            "sz" -> R.drawable.flag_swaziland
            "se" -> R.drawable.flag_sweden
            "ch" -> R.drawable.flag_switzerland
            "sy" -> R.drawable.flag_syria
            "tw" -> R.drawable.flag_taiwan
            "tj" -> R.drawable.flag_tajikistan
            "tz" -> R.drawable.flag_tanzania
            "th" -> R.drawable.flag_thailand
            "tg" -> R.drawable.flag_togo
            "tk" -> R.drawable.flag_tokelau
            "to" -> R.drawable.flag_tonga
            "tn" -> R.drawable.flag_tunisia
            "tr" -> R.drawable.flag_turkey
            "tm" -> R.drawable.flag_turkmenistan
            "tv" -> R.drawable.flag_tuvalu
            "ae" -> R.drawable.flag_uae
            "ug" -> R.drawable.flag_uganda
            "gb" -> R.drawable.flag_united_kingdom
            "ua" -> R.drawable.flag_ukraine
            "uy" -> R.drawable.flag_uruguay
            "us" -> R.drawable.flag_united_states_of_america
            "uz" -> R.drawable.flag_uzbekistan
            "vu" -> R.drawable.flag_vanuatu
            "va" -> R.drawable.flag_vatican_city
            "ve" -> R.drawable.flag_venezuela
            "vn" -> R.drawable.flag_vietnam
            "wf" -> R.drawable.flag_wallis_and_futuna
            "ye" -> R.drawable.flag_yemen
            "zm" -> R.drawable.flag_zambia
            "zw" -> R.drawable.flag_zimbabwe
            "ai" -> R.drawable.flag_anguilla
            "ag" -> R.drawable.flag_antigua_and_barbuda
            "bs" -> R.drawable.flag_bahamas
            "bb" -> R.drawable.flag_barbados
            "bm" -> R.drawable.flag_bermuda
            "vg" -> R.drawable.flag_british_virgin_islands
            "dm" -> R.drawable.flag_dominica
            "do" -> R.drawable.flag_dominican_republic
            "gd" -> R.drawable.flag_grenada
            "jm" -> R.drawable.flag_jamaica
            "ms" -> R.drawable.flag_montserrat
            "kn" -> R.drawable.flag_saint_kitts_and_nevis
            "lc" -> R.drawable.flag_saint_lucia
            "vc" -> R.drawable.flag_saint_vicent_and_the_grenadines
            "tt" -> R.drawable.flag_trinidad_and_tobago
            "tc" -> R.drawable.flag_turks_and_caicos_islands
            "vi" -> R.drawable.flag_us_virgin_islands
            "ss" -> R.drawable.flag_south_sudan
            "xk" -> R.drawable.flag_kosovo
            "is" -> R.drawable.flag_iceland
            "ax" -> R.drawable.flag_aland_islands
            "as" -> R.drawable.flag_american_samoa
            "io" -> R.drawable.flag_british_indian_ocean_territory
            "gp" -> R.drawable.flag_guadeloupe
            "gu" -> R.drawable.flag_guam
            "gg" -> R.drawable.flag_guernsey
            "je" -> R.drawable.flag_jersey
            "nf" -> R.drawable.flag_norfolk_island
            "mp" -> R.drawable.flag_northern_mariana_islands
            "ps" -> R.drawable.flag_palestian_territory
            "mf" -> R.drawable.flag_saint_martin
            "gs" -> R.drawable.flag_south_georgia
            else -> R.drawable.flag_transparent
        }
    }

    /**
     * Get all countries
     *
     * @param context caller context
     * @return List of Country
     */
    @JvmStatic
    fun getAllCountries(context: Context): List<Country> {
        if (countries != null) return countries as ArrayList<Country>
        countries = ArrayList()
        countries?.add(
            Country(
                context.getString(R.string.country_afghanistan_code),
                context.getString(R.string.country_afghanistan_number),
                context.getString(R.string.country_afghanistan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_albania_code),
                context.getString(R.string.country_albania_number),
                context.getString(R.string.country_albania_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_algeria_code),
                context.getString(R.string.country_algeria_number),
                context.getString(R.string.country_algeria_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_andorra_code),
                context.getString(R.string.country_andorra_number),
                context.getString(R.string.country_andorra_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_angola_code),
                context.getString(R.string.country_angola_number),
                context.getString(R.string.country_angola_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_anguilla_code),
                context.getString(R.string.country_anguilla_number),
                context.getString(R.string.country_anguilla_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_antarctica_code),
                context.getString(R.string.country_antarctica_number),
                context.getString(R.string.country_antarctica_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_antigua_and_barbuda_code),
                context.getString(R.string.country_antigua_and_barbuda_number),
                context.getString(R.string.country_antigua_and_barbuda_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_argentina_code),
                context.getString(R.string.country_argentina_number),
                context.getString(R.string.country_argentina_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_armenia_code),
                context.getString(R.string.country_armenia_number),
                context.getString(R.string.country_armenia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_aruba_code),
                context.getString(R.string.country_aruba_number),
                context.getString(R.string.country_aruba_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_australia_code),
                context.getString(R.string.country_australia_number),
                context.getString(R.string.country_australia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_austria_code),
                context.getString(R.string.country_austria_number),
                context.getString(R.string.country_austria_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_azerbaijan_code),
                context.getString(R.string.country_azerbaijan_number),
                context.getString(R.string.country_azerbaijan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_bahamas_code),
                context.getString(R.string.country_bahamas_number),
                context.getString(R.string.country_bahamas_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_bahrain_code),
                context.getString(R.string.country_bahrain_number),
                context.getString(R.string.country_bahrain_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_bangladesh_code),
                context.getString(R.string.country_bangladesh_number),
                context.getString(R.string.country_bangladesh_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_barbados_code),
                context.getString(R.string.country_barbados_number),
                context.getString(R.string.country_barbados_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_belarus_code),
                context.getString(R.string.country_belarus_number),
                context.getString(R.string.country_belarus_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_belgium_code),
                context.getString(R.string.country_belgium_number),
                context.getString(R.string.country_belgium_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_belize_code),
                context.getString(R.string.country_belize_number),
                context.getString(R.string.country_belize_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_benin_code),
                context.getString(R.string.country_benin_number),
                context.getString(R.string.country_benin_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_bermuda_code),
                context.getString(R.string.country_bermuda_number),
                context.getString(R.string.country_bermuda_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_bhutan_code),
                context.getString(R.string.country_bhutan_number),
                context.getString(R.string.country_bhutan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_bolivia_code),
                context.getString(R.string.country_bolivia_number),
                context.getString(R.string.country_bolivia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_bosnia_and_herzegovina_code),
                context.getString(R.string.country_bosnia_and_herzegovina_number),
                context.getString(R.string.country_bosnia_and_herzegovina_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_botswana_code),
                context.getString(R.string.country_botswana_number),
                context.getString(R.string.country_botswana_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_brazil_code),
                context.getString(R.string.country_brazil_number),
                context.getString(R.string.country_brazil_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_british_virgin_islands_code),
                context.getString(R.string.country_british_virgin_islands_number),
                context.getString(R.string.country_british_virgin_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_brunei_darussalam_code),
                context.getString(R.string.country_brunei_darussalam_number),
                context.getString(R.string.country_brunei_darussalam_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_bulgaria_code),
                context.getString(R.string.country_bulgaria_number),
                context.getString(R.string.country_bulgaria_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_burkina_faso_code),
                context.getString(R.string.country_burkina_faso_number),
                context.getString(R.string.country_burkina_faso_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_burundi_code),
                context.getString(R.string.country_burundi_number),
                context.getString(R.string.country_burundi_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_cambodia_code),
                context.getString(R.string.country_cambodia_number),
                context.getString(R.string.country_cambodia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_cameroon_code),
                context.getString(R.string.country_cameroon_number),
                context.getString(R.string.country_cameroon_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_canada_code),
                context.getString(R.string.country_canada_number),
                context.getString(R.string.country_canada_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_cape_verde_code),
                context.getString(R.string.country_cape_verde_number),
                context.getString(R.string.country_cape_verde_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_cayman_islands_code),
                context.getString(R.string.country_cayman_islands_number),
                context.getString(R.string.country_cayman_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_central_african_republic_code),
                context.getString(R.string.country_central_african_republic_number),
                context.getString(R.string.country_central_african_republic_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_chad_code),
                context.getString(R.string.country_chad_number),
                context.getString(R.string.country_chad_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_chile_code),
                context.getString(R.string.country_chile_number),
                context.getString(R.string.country_chile_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_china_code),
                context.getString(R.string.country_china_number),
                context.getString(R.string.country_china_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_christmas_island_code),
                context.getString(R.string.country_christmas_island_number),
                context.getString(R.string.country_christmas_island_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_cocos_keeling_islands_code),
                context.getString(R.string.country_cocos_keeling_islands_number),
                context.getString(R.string.country_cocos_keeling_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_colombia_code),
                context.getString(R.string.country_colombia_number),
                context.getString(R.string.country_colombia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_comoros_code),
                context.getString(R.string.country_comoros_number),
                context.getString(R.string.country_comoros_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_congo_code),
                context.getString(R.string.country_congo_number),
                context.getString(R.string.country_congo_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_the_democratic_republic_of_congo_code),
                context.getString(R.string.country_the_democratic_republic_of_congo_number),
                context.getString(R.string.country_the_democratic_republic_of_congo_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_cook_islands_code),
                context.getString(R.string.country_cook_islands_number),
                context.getString(R.string.country_cook_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_costa_rica_code),
                context.getString(R.string.country_costa_rica_number),
                context.getString(R.string.country_costa_rica_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_croatia_code),
                context.getString(R.string.country_croatia_number),
                context.getString(R.string.country_croatia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_cuba_code),
                context.getString(R.string.country_cuba_number),
                context.getString(R.string.country_cuba_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_cyprus_code),
                context.getString(R.string.country_cyprus_number),
                context.getString(R.string.country_cyprus_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_czech_republic_code),
                context.getString(R.string.country_czech_republic_number),
                context.getString(R.string.country_czech_republic_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_denmark_code),
                context.getString(R.string.country_denmark_number),
                context.getString(R.string.country_denmark_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_djibouti_code),
                context.getString(R.string.country_djibouti_number),
                context.getString(R.string.country_djibouti_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_dominica_code),
                context.getString(R.string.country_dominica_number),
                context.getString(R.string.country_dominica_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_dominican_republic_code),
                context.getString(R.string.country_dominican_republic_number),
                context.getString(R.string.country_dominican_republic_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_timor_leste_code),
                context.getString(R.string.country_timor_leste_number),
                context.getString(R.string.country_timor_leste_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_ecuador_code),
                context.getString(R.string.country_ecuador_number),
                context.getString(R.string.country_ecuador_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_egypt_code),
                context.getString(R.string.country_egypt_number),
                context.getString(R.string.country_egypt_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_el_salvador_code),
                context.getString(R.string.country_el_salvador_number),
                context.getString(R.string.country_el_salvador_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_equatorial_guinea_code),
                context.getString(R.string.country_equatorial_guinea_number),
                context.getString(R.string.country_equatorial_guinea_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_eritrea_code),
                context.getString(R.string.country_eritrea_number),
                context.getString(R.string.country_eritrea_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_estonia_code),
                context.getString(R.string.country_estonia_number),
                context.getString(R.string.country_estonia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_ethiopia_code),
                context.getString(R.string.country_ethiopia_number),
                context.getString(R.string.country_ethiopia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_falkland_islands_malvinas_code),
                context.getString(R.string.country_falkland_islands_malvinas_number),
                context.getString(R.string.country_falkland_islands_malvinas_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_faroe_islands_code),
                context.getString(R.string.country_faroe_islands_number),
                context.getString(R.string.country_faroe_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_fiji_code),
                context.getString(R.string.country_fiji_number),
                context.getString(R.string.country_fiji_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_finland_code),
                context.getString(R.string.country_finland_number),
                context.getString(R.string.country_finland_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_france_code),
                context.getString(R.string.country_france_number),
                context.getString(R.string.country_france_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_french_guyana_code),
                context.getString(R.string.country_french_guyana_number),
                context.getString(R.string.country_french_guyana_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_french_polynesia_code),
                context.getString(R.string.country_french_polynesia_number),
                context.getString(R.string.country_french_polynesia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_gabon_code),
                context.getString(R.string.country_gabon_number),
                context.getString(R.string.country_gabon_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_gambia_code),
                context.getString(R.string.country_gambia_number),
                context.getString(R.string.country_gambia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_georgia_code),
                context.getString(R.string.country_georgia_number),
                context.getString(R.string.country_georgia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_germany_code),
                context.getString(R.string.country_germany_number),
                context.getString(R.string.country_germany_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_ghana_code),
                context.getString(R.string.country_ghana_number),
                context.getString(R.string.country_ghana_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_gibraltar_code),
                context.getString(R.string.country_gibraltar_number),
                context.getString(R.string.country_gibraltar_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_greece_code),
                context.getString(R.string.country_greece_number),
                context.getString(R.string.country_greece_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_greenland_code),
                context.getString(R.string.country_greenland_number),
                context.getString(R.string.country_greenland_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_grenada_code),
                context.getString(R.string.country_grenada_number),
                context.getString(R.string.country_grenada_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_guatemala_code),
                context.getString(R.string.country_guatemala_number),
                context.getString(R.string.country_guatemala_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_guinea_code),
                context.getString(R.string.country_guinea_number),
                context.getString(R.string.country_guinea_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_guinea_bissau_code),
                context.getString(R.string.country_guinea_bissau_number),
                context.getString(R.string.country_guinea_bissau_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_guyana_code),
                context.getString(R.string.country_guyana_number),
                context.getString(R.string.country_guyana_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_haiti_code),
                context.getString(R.string.country_haiti_number),
                context.getString(R.string.country_haiti_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_honduras_code),
                context.getString(R.string.country_honduras_number),
                context.getString(R.string.country_honduras_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_hong_kong_code),
                context.getString(R.string.country_hong_kong_number),
                context.getString(R.string.country_hong_kong_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_hungary_code),
                context.getString(R.string.country_hungary_number),
                context.getString(R.string.country_hungary_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_iceland_code),
                context.getString(R.string.country_iceland_number),
                context.getString(R.string.country_iceland_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_india_code),
                context.getString(R.string.country_india_number),
                context.getString(R.string.country_india_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_indonesia_code),
                context.getString(R.string.country_indonesia_number),
                context.getString(R.string.country_indonesia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_iran_code),
                context.getString(R.string.country_iran_number),
                context.getString(R.string.country_iran_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_iraq_code),
                context.getString(R.string.country_iraq_number),
                context.getString(R.string.country_iraq_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_ireland_code),
                context.getString(R.string.country_ireland_number),
                context.getString(R.string.country_ireland_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_isle_of_man_code),
                context.getString(R.string.country_isle_of_man_number),
                context.getString(R.string.country_isle_of_man_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_israel_code),
                context.getString(R.string.country_israel_number),
                context.getString(R.string.country_israel_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_italy_code),
                context.getString(R.string.country_italy_number),
                context.getString(R.string.country_italy_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_cote_d_ivoire_code),
                context.getString(R.string.country_cote_d_ivoire_number),
                context.getString(R.string.country_cote_d_ivoire_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_jamaica_code),
                context.getString(R.string.country_jamaica_number),
                context.getString(R.string.country_jamaica_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_japan_code),
                context.getString(R.string.country_japan_number),
                context.getString(R.string.country_japan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_jordan_code),
                context.getString(R.string.country_jordan_number),
                context.getString(R.string.country_jordan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_kazakhstan_code),
                context.getString(R.string.country_kazakhstan_number),
                context.getString(R.string.country_kazakhstan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_kenya_code),
                context.getString(R.string.country_kenya_number),
                context.getString(R.string.country_kenya_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_kiribati_code),
                context.getString(R.string.country_kiribati_number),
                context.getString(R.string.country_kiribati_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_kosovo_code),
                context.getString(R.string.country_kosovo_number),
                context.getString(R.string.country_kosovo_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_kuwait_code),
                context.getString(R.string.country_kuwait_number),
                context.getString(R.string.country_kuwait_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_kyrgyzstan_code),
                context.getString(R.string.country_kyrgyzstan_number),
                context.getString(R.string.country_kyrgyzstan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_lao_peoples_democratic_republic_code),
                context.getString(R.string.country_lao_peoples_democratic_republic_number),
                context.getString(R.string.country_lao_peoples_democratic_republic_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_latvia_code),
                context.getString(R.string.country_latvia_number),
                context.getString(R.string.country_latvia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_lebanon_code),
                context.getString(R.string.country_lebanon_number),
                context.getString(R.string.country_lebanon_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_lesotho_code),
                context.getString(R.string.country_lesotho_number),
                context.getString(R.string.country_lesotho_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_liberia_code),
                context.getString(R.string.country_liberia_number),
                context.getString(R.string.country_liberia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_libya_code),
                context.getString(R.string.country_libya_number),
                context.getString(R.string.country_libya_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_liechtenstein_code),
                context.getString(R.string.country_liechtenstein_number),
                context.getString(R.string.country_liechtenstein_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_lithuania_code),
                context.getString(R.string.country_lithuania_number),
                context.getString(R.string.country_lithuania_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_luxembourg_code),
                context.getString(R.string.country_luxembourg_number),
                context.getString(R.string.country_luxembourg_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_macao_code),
                context.getString(R.string.country_macao_number),
                context.getString(R.string.country_macao_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_macedonia_code),
                context.getString(R.string.country_macedonia_number),
                context.getString(R.string.country_macedonia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_madagascar_code),
                context.getString(R.string.country_madagascar_number),
                context.getString(R.string.country_madagascar_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_malawi_code),
                context.getString(R.string.country_malawi_number),
                context.getString(R.string.country_malawi_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_malaysia_code),
                context.getString(R.string.country_malaysia_number),
                context.getString(R.string.country_malaysia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_maldives_code),
                context.getString(R.string.country_maldives_number),
                context.getString(R.string.country_maldives_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_mali_code),
                context.getString(R.string.country_mali_number),
                context.getString(R.string.country_mali_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_malta_code),
                context.getString(R.string.country_malta_number),
                context.getString(R.string.country_malta_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_marshall_islands_code),
                context.getString(R.string.country_marshall_islands_number),
                context.getString(R.string.country_marshall_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_martinique_code),
                context.getString(R.string.country_martinique_number),
                context.getString(R.string.country_martinique_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_mauritania_code),
                context.getString(R.string.country_mauritania_number),
                context.getString(R.string.country_mauritania_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_mauritius_code),
                context.getString(R.string.country_mauritius_number),
                context.getString(R.string.country_mauritius_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_mayotte_code),
                context.getString(R.string.country_mayotte_number),
                context.getString(R.string.country_mayotte_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_mexico_code),
                context.getString(R.string.country_mexico_number),
                context.getString(R.string.country_mexico_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_micronesia_code),
                context.getString(R.string.country_micronesia_number),
                context.getString(R.string.country_micronesia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_moldova_code),
                context.getString(R.string.country_moldova_number),
                context.getString(R.string.country_moldova_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_monaco_code),
                context.getString(R.string.country_monaco_number),
                context.getString(R.string.country_monaco_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_mongolia_code),
                context.getString(R.string.country_mongolia_number),
                context.getString(R.string.country_mongolia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_montserrat_code),
                context.getString(R.string.country_montserrat_number),
                context.getString(R.string.country_montserrat_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_montenegro_code),
                context.getString(R.string.country_montenegro_number),
                context.getString(R.string.country_montenegro_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_morocco_code),
                context.getString(R.string.country_morocco_number),
                context.getString(R.string.country_morocco_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_myanmar_code),
                context.getString(R.string.country_myanmar_number),
                context.getString(R.string.country_myanmar_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_mozambique_code),
                context.getString(R.string.country_mozambique_number),
                context.getString(R.string.country_mozambique_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_namibia_code),
                context.getString(R.string.country_namibia_number),
                context.getString(R.string.country_namibia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_nauru_code),
                context.getString(R.string.country_nauru_number),
                context.getString(R.string.country_nauru_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_nepal_code),
                context.getString(R.string.country_nepal_number),
                context.getString(R.string.country_nepal_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_netherlands_code),
                context.getString(R.string.country_netherlands_number),
                context.getString(R.string.country_netherlands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_new_caledonia_code),
                context.getString(R.string.country_new_caledonia_number),
                context.getString(R.string.country_new_caledonia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_new_zealand_code),
                context.getString(R.string.country_new_zealand_number),
                context.getString(R.string.country_new_zealand_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_nicaragua_code),
                context.getString(R.string.country_nicaragua_number),
                context.getString(R.string.country_nicaragua_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_niger_code),
                context.getString(R.string.country_niger_number),
                context.getString(R.string.country_niger_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_nigeria_code),
                context.getString(R.string.country_nigeria_number),
                context.getString(R.string.country_nigeria_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_niue_code),
                context.getString(R.string.country_niue_number),
                context.getString(R.string.country_niue_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_north_korea_code),
                context.getString(R.string.country_north_korea_number),
                context.getString(R.string.country_north_korea_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_norway_code),
                context.getString(R.string.country_norway_number),
                context.getString(R.string.country_norway_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_oman_code),
                context.getString(R.string.country_oman_number),
                context.getString(R.string.country_oman_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_pakistan_code),
                context.getString(R.string.country_pakistan_number),
                context.getString(R.string.country_pakistan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_palau_code),
                context.getString(R.string.country_palau_number),
                context.getString(R.string.country_palau_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_panama_code),
                context.getString(R.string.country_panama_number),
                context.getString(R.string.country_panama_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_papua_new_guinea_code),
                context.getString(R.string.country_papua_new_guinea_number),
                context.getString(R.string.country_papua_new_guinea_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_paraguay_code),
                context.getString(R.string.country_paraguay_number),
                context.getString(R.string.country_paraguay_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_peru_code),
                context.getString(R.string.country_peru_number),
                context.getString(R.string.country_peru_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_philippines_code),
                context.getString(R.string.country_philippines_number),
                context.getString(R.string.country_philippines_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_pitcairn_code),
                context.getString(R.string.country_pitcairn_number),
                context.getString(R.string.country_pitcairn_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_poland_code),
                context.getString(R.string.country_poland_number),
                context.getString(R.string.country_poland_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_portugal_code),
                context.getString(R.string.country_portugal_number),
                context.getString(R.string.country_portugal_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_puerto_rico_code),
                context.getString(R.string.country_puerto_rico_number),
                context.getString(R.string.country_puerto_rico_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_qatar_code),
                context.getString(R.string.country_qatar_number),
                context.getString(R.string.country_qatar_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_reunion_code),
                context.getString(R.string.country_reunion_number),
                context.getString(R.string.country_reunion_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_romania_code),
                context.getString(R.string.country_romania_number),
                context.getString(R.string.country_romania_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_russian_federation_code),
                context.getString(R.string.country_russian_federation_number),
                context.getString(R.string.country_russian_federation_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_rwanda_code),
                context.getString(R.string.country_rwanda_number),
                context.getString(R.string.country_rwanda_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_saint_barthelemy_code),
                context.getString(R.string.country_saint_barthelemy_number),
                context.getString(R.string.country_saint_barthelemy_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_saint_kitts_and_nevis_code),
                context.getString(R.string.country_saint_kitts_and_nevis_number),
                context.getString(R.string.country_saint_kitts_and_nevis_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_saint_lucia_code),
                context.getString(R.string.country_saint_lucia_number),
                context.getString(R.string.country_saint_lucia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_saint_vincent_the_grenadines_code),
                context.getString(R.string.country_saint_vincent_the_grenadines_number),
                context.getString(R.string.country_saint_vincent_the_grenadines_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_samoa_code),
                context.getString(R.string.country_samoa_number),
                context.getString(R.string.country_samoa_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_san_marino_code),
                context.getString(R.string.country_san_marino_number),
                context.getString(R.string.country_san_marino_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_sao_tome_and_principe_code),
                context.getString(R.string.country_sao_tome_and_principe_number),
                context.getString(R.string.country_sao_tome_and_principe_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_saudi_arabia_code),
                context.getString(R.string.country_saudi_arabia_number),
                context.getString(R.string.country_saudi_arabia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_senegal_code),
                context.getString(R.string.country_senegal_number),
                context.getString(R.string.country_senegal_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_serbia_code),
                context.getString(R.string.country_serbia_number),
                context.getString(R.string.country_serbia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_seychelles_code),
                context.getString(R.string.country_seychelles_number),
                context.getString(R.string.country_seychelles_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_sierra_leone_code),
                context.getString(R.string.country_sierra_leone_number),
                context.getString(R.string.country_sierra_leone_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_singapore_code),
                context.getString(R.string.country_singapore_number),
                context.getString(R.string.country_singapore_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_sint_maarten_code),
                context.getString(R.string.country_sint_maarten_number),
                context.getString(R.string.country_sint_maarten_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_slovakia_code),
                context.getString(R.string.country_slovakia_number),
                context.getString(R.string.country_slovakia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_slovenia_code),
                context.getString(R.string.country_slovenia_number),
                context.getString(R.string.country_slovenia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_solomon_islands_code),
                context.getString(R.string.country_solomon_islands_number),
                context.getString(R.string.country_solomon_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_somalia_code),
                context.getString(R.string.country_somalia_number),
                context.getString(R.string.country_somalia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_south_africa_code),
                context.getString(R.string.country_south_africa_number),
                context.getString(R.string.country_south_africa_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_south_korea_code),
                context.getString(R.string.country_south_korea_number),
                context.getString(R.string.country_south_korea_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_spain_code),
                context.getString(R.string.country_spain_number),
                context.getString(R.string.country_spain_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_sri_lanka_code),
                context.getString(R.string.country_sri_lanka_number),
                context.getString(R.string.country_sri_lanka_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_saint_helena_code),
                context.getString(R.string.country_saint_helena_number),
                context.getString(R.string.country_saint_helena_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_saint_pierre_and_miquelon_code),
                context.getString(R.string.country_saint_pierre_and_miquelon_number),
                context.getString(R.string.country_saint_pierre_and_miquelon_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_south_sudan_code),
                context.getString(R.string.country_south_sudan_number),
                context.getString(R.string.country_south_sudan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_sudan_code),
                context.getString(R.string.country_sudan_number),
                context.getString(R.string.country_sudan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_suriname_code),
                context.getString(R.string.country_suriname_number),
                context.getString(R.string.country_suriname_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_swaziland_code),
                context.getString(R.string.country_swaziland_number),
                context.getString(R.string.country_swaziland_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_sweden_code),
                context.getString(R.string.country_sweden_number),
                context.getString(R.string.country_sweden_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_switzerland_code),
                context.getString(R.string.country_switzerland_number),
                context.getString(R.string.country_switzerland_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_syrian_arab_republic_code),
                context.getString(R.string.country_syrian_arab_republic_number),
                context.getString(R.string.country_syrian_arab_republic_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_taiwan_code),
                context.getString(R.string.country_taiwan_number),
                context.getString(R.string.country_taiwan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_tajikistan_code),
                context.getString(R.string.country_tajikistan_number),
                context.getString(R.string.country_tajikistan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_tanzania_code),
                context.getString(R.string.country_tanzania_number),
                context.getString(R.string.country_tanzania_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_thailand_code),
                context.getString(R.string.country_thailand_number),
                context.getString(R.string.country_thailand_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_togo_code),
                context.getString(R.string.country_togo_number),
                context.getString(R.string.country_togo_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_tokelau_code),
                context.getString(R.string.country_tokelau_number),
                context.getString(R.string.country_tokelau_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_tonga_code),
                context.getString(R.string.country_tonga_number),
                context.getString(R.string.country_tonga_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_trinidad_tobago_code),
                context.getString(R.string.country_trinidad_tobago_number),
                context.getString(R.string.country_trinidad_tobago_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_tunisia_code),
                context.getString(R.string.country_tunisia_number),
                context.getString(R.string.country_tunisia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_turkey_code),
                context.getString(R.string.country_turkey_number),
                context.getString(R.string.country_turkey_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_turkmenistan_code),
                context.getString(R.string.country_turkmenistan_number),
                context.getString(R.string.country_turkmenistan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_turks_and_caicos_islands_code),
                context.getString(R.string.country_turks_and_caicos_islands_number),
                context.getString(R.string.country_turks_and_caicos_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_tuvalu_code),
                context.getString(R.string.country_tuvalu_number),
                context.getString(R.string.country_tuvalu_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_united_arab_emirates_code),
                context.getString(R.string.country_united_arab_emirates_number),
                context.getString(R.string.country_united_arab_emirates_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_uganda_code),
                context.getString(R.string.country_uganda_number),
                context.getString(R.string.country_uganda_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_united_kingdom_code),
                context.getString(R.string.country_united_kingdom_number),
                context.getString(R.string.country_united_kingdom_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_ukraine_code),
                context.getString(R.string.country_ukraine_number),
                context.getString(R.string.country_ukraine_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_uruguay_code),
                context.getString(R.string.country_uruguay_number),
                context.getString(R.string.country_uruguay_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_united_states_code),
                context.getString(R.string.country_united_states_number),
                context.getString(R.string.country_united_states_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_us_virgin_islands_code),
                context.getString(R.string.country_us_virgin_islands_number),
                context.getString(R.string.country_us_virgin_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_uzbekistan_code),
                context.getString(R.string.country_uzbekistan_number),
                context.getString(R.string.country_uzbekistan_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_vanuatu_code),
                context.getString(R.string.country_vanuatu_number),
                context.getString(R.string.country_vanuatu_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_holy_see_vatican_city_state_code),
                context.getString(R.string.country_holy_see_vatican_city_state_number),
                context.getString(R.string.country_holy_see_vatican_city_state_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_venezuela_code),
                context.getString(R.string.country_venezuela_number),
                context.getString(R.string.country_venezuela_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_viet_nam_code),
                context.getString(R.string.country_viet_nam_number),
                context.getString(R.string.country_viet_nam_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_wallis_and_futuna_code),
                context.getString(R.string.country_wallis_and_futuna_number),
                context.getString(R.string.country_wallis_and_futuna_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_yemen_code),
                context.getString(R.string.country_yemen_number),
                context.getString(R.string.country_yemen_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_zambia_code),
                context.getString(R.string.country_zambia_number),
                context.getString(R.string.country_zambia_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_zimbabwe_code),
                context.getString(R.string.country_zimbabwe_number),
                context.getString(R.string.country_zimbabwe_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_aland_islands_code),
                context.getString(R.string.country_aland_islands_number),
                context.getString(R.string.country_aland_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_american_samoa_code),
                context.getString(R.string.country_american_samoa_number),
                context.getString(R.string.country_american_samoa_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_british_indian_ocean_territory_code),
                context.getString(R.string.country_british_indian_ocean_territory_number),
                context.getString(R.string.country_british_indian_ocean_territory_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_guadeloupe_code),
                context.getString(R.string.country_guadeloupe_number),
                context.getString(R.string.country_guadeloupe_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_guam_code),
                context.getString(R.string.country_guam_number),
                context.getString(R.string.country_guam_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_guernsey_code),
                context.getString(R.string.country_guernsey_number),
                context.getString(R.string.country_guernsey_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_jersey_code),
                context.getString(R.string.country_jersey_number),
                context.getString(R.string.country_jersey_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_norfolk_island_code),
                context.getString(R.string.country_norfolk_island_number),
                context.getString(R.string.country_norfolk_island_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_northern_mariana_islands_code),
                context.getString(R.string.country_northern_mariana_islands_number),
                context.getString(R.string.country_northern_mariana_islands_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_palestian_territory_code),
                context.getString(R.string.country_palestian_territory_number),
                context.getString(R.string.country_palestian_territory_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_saint_martin_code),
                context.getString(R.string.country_saint_martin_number),
                context.getString(R.string.country_saint_martin_name)
            )
        )
        countries?.add(
            Country(
                context.getString(R.string.country_south_georgia_code),
                context.getString(R.string.country_south_georgia_number),
                context.getString(R.string.country_south_africa_name)
            )
        )

        countries?.sortWith(Comparator { country1, country2 ->
            country1.name.compareTo(
                country2.name,
                ignoreCase = true
            )
        })
        return countries as ArrayList<Country>
    }

    @JvmStatic
    fun getByNumber(
        context: Context,
        preferredCountries: List<Country>?,
        fullNumber: String
    ): Country? {
        if (fullNumber.isEmpty()) return null
        val firstDigit: Int = if (fullNumber[0] == '+') {
            1
        } else {
            0
        }
        var country: Country?
        for (i in firstDigit until firstDigit + 4) {
            val code = fullNumber.substring(firstDigit, i)
            country = getByCode(context, preferredCountries, code)
            if (country != null) return country
        }
        return null
    }

    /**
     * Search a country which matches @param code.
     *
     *
     * //@param preferredCountries list of country with priority,
     *
     * @param code phone code. i.e 91 or 1
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     */
    @JvmStatic
    fun getByCode(context: Context?, code: Int): Country {
        return getByCode(context, (code.toString() + "").toInt())
    }

    /**
     * Search a country which matches @param code.
     *
     * @param preferredCountries is list of preference countries.
     * @param code               phone code. i.e "91" or "1"
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     * if same code (e.g. +1) available for more than one country ( US, canada) , this function will
     * return preferred country.
     */
    private fun getByCode(
        context: Context,
        preferredCountries: List<Country>?,
        code: String
    ): Country? {
        if (preferredCountries != null && preferredCountries.isNotEmpty()) {
            for (country in preferredCountries) {
                if (country.phoneCode == code) {
                    return country
                }
            }
        }
        for (country in getAllCountries(context)) {
            if (country.phoneCode == code) {
                return country
            }
        }
        return null
    }

    /**
     * Search a country which matches @param nameCode.
     *
     * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     */
    @JvmStatic
    fun getByNameCodeFromCustomCountries(
        context: Context,
        customCountries: List<Country>?,
        nameCode: String?
    ): Country? {
        if (customCountries == null || customCountries.isEmpty()) {
            return getByNameCodeFromAllCountries(context, nameCode)
        } else {
            for (country in customCountries) {
                if (country.iso.equals(nameCode, ignoreCase = true)) {
                    return country
                }
            }
        }
        return null
    }

    /**
     * Search a country which matches @param nameCode.
     *
     * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     */
    @JvmStatic
    fun getByNameCodeFromAllCountries(
        context: Context,
        nameCode: String?
    ): Country? {
        val countries = getAllCountries(context)
        for (country in countries) {
            if (country.iso.equals(nameCode, ignoreCase = true)) {
                return country
            }
        }
        return null
    }

    @JvmStatic
    fun getCountryIsoByTimeZone(
        context: Context,
        timeZoneId: String?
    ): List<String>? {
        val timeZoneAndCountryIsos: Map<String, List<String>> = getTimeZoneAndCountryISOs(context)
        return timeZoneAndCountryIsos[timeZoneId]
    }

    /**
     * Return list of Map for timezone and iso country.
     *
     * @param context Caller context
     * @return List of timezone and country.
     */
    private fun getTimeZoneAndCountryISOs(context: Context): Map<String, List<String>> {
        if (timeZoneAndCountryISOs.isNotEmpty()) {
            return timeZoneAndCountryISOs
        }
        timeZoneAndCountryISOs =
            HashMap()
        val inputStream =
            context.resources.openRawResource(R.raw.zone1970)
        val buf =
            BufferedReader(InputStreamReader(inputStream))
        var lineJustFetched: String?
        var wordsArray: Array<String>
        try {
            while (true) {
                lineJustFetched = buf.readLine()
                if (lineJustFetched == null) {
                    break
                } else {
                    wordsArray = lineJustFetched.split("\t".toRegex()).toTypedArray()
                    if (!lineJustFetched.substring(0, 1).contains("#")) {
                        if (wordsArray.size >= 3) {
                            val isos: MutableList<String> =
                                ArrayList()
                            Collections.addAll(
                                isos,
                                *wordsArray[0].split(",".toRegex()).toTypedArray()
                            )
                            timeZoneAndCountryISOs[wordsArray[2]] = isos
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return timeZoneAndCountryISOs
    }
}