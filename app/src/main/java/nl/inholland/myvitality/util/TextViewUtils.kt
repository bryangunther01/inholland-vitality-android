package nl.inholland.myvitality.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import nl.inholland.myvitality.R
import java.util.*


object TextViewUtils {
    /**
     * @param context the context of the activity it's triggered from
     * @return the greeting message based on the time
     */
    fun getGreetingMessage(context: Context): String {
        val c = Calendar.getInstance()

        return when (c.get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> context.getString(R.string.message_good_morning)
            in 12..17 -> context.getString(R.string.message_good_afternoon)
            in 18..23 -> context.getString(R.string.message_good_evening)
            in 0..4 -> context.getString(R.string.message_good_night)
            else -> context.getString(R.string.message_welcome)
        }
    }

    /**
     *
     * @param string this will setup to your textView
     * @param colorId  text will fill with this color.
     * @return string with color, it will append to textView.
     */
    fun getColoredString(string: String?, colorId: Int): Spannable {
        val spannable: Spannable = SpannableString(string)
        spannable.setSpan(
            ForegroundColorSpan(colorId),
            0,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }
}