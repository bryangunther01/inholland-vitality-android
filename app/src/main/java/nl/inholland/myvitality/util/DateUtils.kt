package nl.inholland.myvitality.util

import android.content.Context
import android.icu.text.SimpleDateFormat
import nl.inholland.myvitality.R
import java.util.*

class DateUtils {
    companion object {
        private const val SECOND_MILLIS = 1000
        private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private const val DAY_MILLIS = 24 * HOUR_MILLIS
        private const val WEEK_MILLIS = 7 * DAY_MILLIS

        private fun currentDate(): Date {
            val calendar = Calendar.getInstance()
            return calendar.time
        }

        private fun isSameYear(date: Date): Boolean {
            val currentCalendar = Calendar.getInstance()
            val givenDateCalender = Calendar.getInstance()
            givenDateCalender.time = date

            return currentCalendar.get(Calendar.YEAR) == givenDateCalender.get(Calendar.YEAR)
        }

        fun formatDate(date: String): String{
            // Set the publishData including the right format
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            return formatter.format(parser.parse(date))
        }

        fun formatDateToTimeAgo(context: Context, dateString: String, withTime: Boolean = false): String{
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = parser.parse(dateString)

            val dateYearFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val defaultDateFormatter = if(isSameYear(date)) dateFormatter else dateYearFormatter

            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

            var time = date.time
            // TODO: Remove if API fixed
            time = time.plus(HOUR_MILLIS)

            val now = currentDate().time
            if (time > now || time <= 0) {
                return context.getString(R.string.time_future)
            }

            val diff = now - time
            return when {
                diff < 2 * MINUTE_MILLIS -> context.getString(R.string.time_minute_ago)
                diff < 60 * MINUTE_MILLIS -> context.getString(R.string.time_minutes_ago, diff / MINUTE_MILLIS)
                diff < 1 * DAY_MILLIS -> context.getString(R.string.time_hours_ago, diff / HOUR_MILLIS)
                diff < 2 * DAY_MILLIS -> if(withTime) context.getString(R.string.time_yesterday_with_time, timeFormatter.format(date)) else context.getString(R.string.time_yesterday)
                else -> if(withTime) context.getString(R.string.time_date_at, defaultDateFormatter.format(date), timeFormatter.format(date)) else defaultDateFormatter.format(date)
            }
        }
    }
}