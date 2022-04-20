package nl.inholland.myvitality.util

import android.os.Build
import android.text.Html
import android.text.Spanned

object StringUtils {

    fun String.toHtmlSpan(): Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}