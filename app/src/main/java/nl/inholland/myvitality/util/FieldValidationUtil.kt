package nl.gunther.bryan.newsreader.utils

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import nl.inholland.myvitality.R
import javax.inject.Singleton

@Singleton
class FieldValidationUtil(var context: Context) {
    /**
     * The set valid state is a function to validate a EditText and show the
     *
     * @param valid Boolean true if valid else false
     * @param editText The editText to apply the validation
     * @param errorTextView The textview where we need to show the error message
     *
     * @return Returns valid
     */
    fun setFieldState(editText: EditText, valid: Boolean, errorTextView: TextView, errorMessage: String): Boolean {
        // Check the color of the based on the valid state
        val textColor =
            if (valid) ContextCompat.getColor(context, R.color.black) else ContextCompat.getColor(context, R.color.red)
        // Check the visibility based on the valid state
        val visibility = if (valid) View.INVISIBLE else View.VISIBLE

        // Set the text color of the edit text
        editText.setTextColor(textColor)

        // Check if the errorTextView is empty
        // If true show the error message else skip
        if (errorTextView.text.toString() != errorMessage && !valid) {
            errorTextView.text = errorMessage
        }

        errorTextView.visibility = visibility

        // Return the valid state
        return valid
    }
}