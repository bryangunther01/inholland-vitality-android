package nl.inholland.myvitality.util

import android.content.Context
import android.util.Log

import android.text.Spannable

import android.text.style.ForegroundColorSpan

import android.text.SpannableString
import nl.inholland.myvitality.R
import java.util.*


class TextViewUtils {
    companion object {

        fun getGreetingMessage(context: Context) :String{
            val c = Calendar.getInstance()

            return when (c.get(Calendar.HOUR_OF_DAY)) {
                in 0..11 -> context.getString(R.string.message_good_morning)
                in 12..15 -> context.getString(R.string.message_good_afternoon)
                in 16..20 -> context.getString(R.string.message_good_evening)
                in 21..23 -> context.getString(R.string.message_good_night)
                else -> context.getString(R.string.message_welcome)
            }
        }

        /***
         *
         * @param mString this will setup to your textView
         * @param colorId  text will fill with this color.
         * @return string with color, it will append to textView.
         */
        fun getColoredString(mString: String?, colorId: Int): Spannable {
            val spannable: Spannable = SpannableString(mString)
            spannable.setSpan(
                ForegroundColorSpan(colorId),
                0,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }
    }
}