package com.svaggy.utils

import android.annotation.SuppressLint
import android.os.Build
import android.text.format.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


//----------------- Format pattern -------------------
//"yyyy.MM.dd G 'at' HH:mm:ss z" ---- 2001.07.04 AD at 12:08:56 PDT
//"hh 'o''clock' a, zzzz" ----------- 12 o'clock PM, Pacific Daylight Time
//"EEE, d MMM yyyy HH:mm:ss Z"------- Wed, 4 Jul 2001 12:08:56 -0700
//"yyyy-MM-dd'T'HH:mm:ss.SSSZ"------- 2001-07-04T12:08:56.235-0700
//"yyMMddHHmmssZ"-------------------- 010704120856-0700
//"K:mm a, z" ----------------------- 0:08 PM, PDT
//"h:mm a" -------------------------- 12:08 PM
//"EEE, MMM d, ''yy" ---------------- Wed, Jul 4, '01


const val SIMPLE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"


@SuppressLint("SimpleDateFormat")
fun dayName(inputDate: String?): String? {
    var date: Date? = null
    try {
        date = inputDate?.let { SimpleDateFormat("MMM dd, yyyy").parse(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return date?.let { SimpleDateFormat("EEEE", Locale.ENGLISH).format(it) }
}


fun getDay(date: Date, day: (day: String) -> Unit = {}) {
    val days = DateFormat.format("dd", date)
    day(days.toString())
}

fun getMonthName(date: Date, month: (month: String) -> Unit = {}) {
    month(DateFormat.format("MMM", date).toString())
}

fun getYear(date: Date, year: (year: String) -> Unit = {}) {
    year(DateFormat.format("yyyy", date).toString())
}

@SuppressLint("SimpleDateFormat")
fun stringToDate(stringDate: String, mDate: (Date) -> Unit) {
    val format = SimpleDateFormat(SIMPLE_DATE_FORMAT)
    try {
        format.parse(stringDate)?.let { mDate(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
}

@SuppressLint("SimpleDateFormat")
fun stringToDateWithTimeZone(stringDate: String, timezone: String, mDate: (Date) -> Unit) {
    val format = SimpleDateFormat(SIMPLE_DATE_FORMAT)
    format.timeZone = TimeZone.getTimeZone(timezone)
    try {
        format.parse(stringDate)?.let { mDate(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
}


@SuppressLint("SimpleDateFormat")
fun getDate(startTime: String?): String? {
    val readFormat = SimpleDateFormat("MMM dd, yyyy")
    val format = SimpleDateFormat(SIMPLE_DATE_FORMAT)
    val dat: Date?
    try {
        dat = startTime?.let { format.parse(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
        return ""
    }
    return dat?.let { readFormat.format(it) }
}


@SuppressLint("SimpleDateFormat")
fun getDateAccordingToTimeZone(startTime: String?, timeZone: String): String? {
    val inputFormat = SimpleDateFormat(SIMPLE_DATE_FORMAT)
    val outputFormat = SimpleDateFormat("MMM dd, yyyy")
    inputFormat.timeZone = TimeZone.getTimeZone(timeZone)
    var date: Date? = null
    try {
        date = inputFormat.parse(startTime ?: "")
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return outputFormat.format(date ?: "")
}


@SuppressLint("SimpleDateFormat")
fun getTimeAccordingTimeZone(d: String?, timeZone: String?): String? {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val outputFormat = SimpleDateFormat("hh:mm a")
    outputFormat.timeZone = TimeZone.getTimeZone(timeZone)
    inputFormat.timeZone = TimeZone.getTimeZone(timeZone)
    var date: Date? = null
    try {
        date = inputFormat.parse(d ?: "")
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return outputFormat.format(date ?: "")
}

@SuppressLint("SimpleDateFormat")
fun getDateWithChangedFormat(startTime: String?, inputFormat: String?): String? {
    val readFormat = SimpleDateFormat(inputFormat)
    val format = SimpleDateFormat(SIMPLE_DATE_FORMAT)
    var dat: Date? = null
    try {
        dat = startTime?.let { format.parse(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return dat?.let { readFormat.format(it) }
}

fun Date.toString(format: String): String {
    val dateFormatter = SimpleDateFormat(format, Locale.US)
    return dateFormatter.format(this)
}


@SuppressLint("SimpleDateFormat")
fun getCurrentUTCtime(): String? {
    val df: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    return df.format(Calendar.getInstance().time)
}

@SuppressLint("SimpleDateFormat")
fun getToday(format: String?): String? {
    val date = Date()
    return SimpleDateFormat(format).format(date)
}


@SuppressLint("SimpleDateFormat")
fun getDateAgo(date: String?): String {
    val time: String
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    val timeZone = TimeZone.getDefault().displayName
    format.timeZone = TimeZone.getTimeZone(timeZone)
    var past: Date? = null
    try {
        past = date?.let { format.parse(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    val now = Date()
    val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past!!.time)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
    val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
    val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time)

    time = if (seconds < 60) {
//      "just now"
        getTime(date)
    } else (when {
        minutes < 60 -> {
//          minutes.toString() + "m ago"
            getTime(date)
        }
        hours < 24 -> {
            getTime(date)
        }
        days < 2 -> {
            "Yesterday"
        }
        else -> {
            getDate(date)
        }
    })!!
    return time
}


@SuppressLint("SimpleDateFormat")
fun getDateTimeAgo(date: String?): String {
    val time: String
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    val timeZone = TimeZone.getDefault().displayName
    format.timeZone = TimeZone.getTimeZone(timeZone)
    var past: Date? = null
    try {
        past = date?.let { format.parse(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    val now = Date()
    val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past!!.time)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
    val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
    val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time)

    time = if (seconds < 60) {
//      "just now"
        getTime(date)
    } else (when {
        minutes < 60 -> {
//         minutes.toString() + "m ago"
            getTime(date)
        }
        hours < 24 -> {
            getTime(date)
        }
        days < 2 -> {
            "Yesterday"
        }
        else -> {
            getTime(date)
        }
    })
    return time
}


@SuppressLint("SimpleDateFormat")
fun getTime(d: String?): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    val timeZone = TimeZone.getDefault().displayName
    inputFormat.timeZone = TimeZone.getTimeZone(timeZone)
    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val date: Date?
    var formattedDate = ""
    try {
        date = d?.let { inputFormat.parse(it) }
        formattedDate = date?.let { outputFormat.format(it) }.toString()
        println(formattedDate)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return formattedDate
}


@SuppressLint("SimpleDateFormat")
fun getCurrentTimeStamp(): String? {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.format(Date())
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@SuppressLint("SimpleDateFormat")
fun convert24to12(time: String?): String {
    var convertedTime = ""
    try {
        val displayFormat = SimpleDateFormat("hh:mm a")
        val parseFormat = SimpleDateFormat("HH:mm:ss")
        val date = time?.let { parseFormat.parse(it) }
        convertedTime = date?.let { displayFormat.format(it) }.toString()
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return convertedTime
    //Output will be 10:23 PM
}


@SuppressLint("SimpleDateFormat")
fun getDateTime(simpleDateFormat: String): String? {
    val dateFormat = SimpleDateFormat(simpleDateFormat)
    val date = Date()
    return dateFormat.format(date)
}

/**
 * Add field date to current date
 */
fun Date.add(field: Int, amount: Int): Date {
    Calendar.getInstance().apply {
        time = this@add
        add(field, amount)
        return time
    }
}

@SuppressLint("SimpleDateFormat")
fun getCurrentDateAndTime(): String {
    try {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT)
            current.format(formatter)
        } else {
            val date = Date()
            val formatter = SimpleDateFormat(SIMPLE_DATE_FORMAT)
            formatter.format(date)
        }
    } catch (e: Exception) {
        println("Exception ${e.message}")
    }
    return ""
}


