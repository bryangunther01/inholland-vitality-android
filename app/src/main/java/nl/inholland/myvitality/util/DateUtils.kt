package nl.inholland.myvitality.util

import android.icu.text.SimpleDateFormat
import android.text.format.DateUtils
import java.util.*

object DateUtils {
    const val SECOND_MILLIS = 1000
    const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    const val DAY_MILLIS = 24 * HOUR_MILLIS
    const val WEEK_MILLIS = 7 * DAY_MILLIS

    private fun now(): Date {
        return Calendar.getInstance().time
    }

    fun isInPast(dateString: String): Boolean {
        val date = stringToDate(dateString)
        return date.before(now())
    }

    fun isWithinAWeek(dateString: String): Boolean {
        val date = stringToDate(dateString)
        date.time = date.time.minus(WEEK_MILLIS)

        return date.before(now())
    }

    private fun isSameDate(date: Date): Boolean {
        val c1 = Calendar.getInstance()
        val c2 = Calendar.getInstance()
        c2.time = date

        return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH));
    }

    private fun stringToDate(dateString: String): Date{
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        return format.parse(dateString)
    }

    fun formatDate(date: String): String {
        // Set the publishData including the right format
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return formatter.format(parser.parse(date))
    }

    fun formatDate(date: String, format: String): String {
        // Set the publishData including the right format
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(parser.parse(date))
    }


    fun formatDateToTimeAgo(dateString: String): String {
        val date = stringToDate(dateString)
        return DateUtils.getRelativeTimeSpanString(date.time, now().time.plus(SECOND_MILLIS * 5), 59000L, DateUtils.FORMAT_ABBREV_MONTH).toString()
    }
}