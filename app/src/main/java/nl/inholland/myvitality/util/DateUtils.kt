package nl.inholland.myvitality.util

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import nl.inholland.myvitality.R
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import java.util.*

object DateUtils {
    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    private const val WEEK_MILLIS = 7 * DAY_MILLIS

    private fun now(): Date {
        return Calendar.getInstance().time
    }

    fun isInPast(dateString: String): Boolean {
        val date = stringToDate(dateString)
        return date.before(now())
    }

    private fun isSameYear(date: Date): Boolean {
        val currentCalendar = Calendar.getInstance()
        val givenDateCalender = Calendar.getInstance()
        givenDateCalender.time = date

        return currentCalendar.get(Calendar.YEAR) == givenDateCalender.get(Calendar.YEAR)
    }

    private fun stringToDate(dateString: String): Date{
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())

        return format.parse(dateString)
    }

    fun formatDate(date: String): String {
        // Set the publishData including the right format
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return formatter.format(parser.parse(date))
    }

    fun formatDate(date: String, format: String): String {
        // Set the publishData including the right format
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(parser.parse(date))
    }


    fun formatDateToTimeAgo(dateString: String): String {
        val date = stringToDate(dateString)
        val time = date.time.plus(HOUR_MILLIS)

        return DateUtils.getRelativeTimeSpanString(time, now().time.plus(SECOND_MILLIS * 5), 59000L, DateUtils.FORMAT_ABBREV_MONTH).toString()
    }
}