package nl.inholland.myvitality.ui.widgets.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.TextView
import nl.inholland.myvitality.R
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.authentication.register.RegisterActivity

object Dialogs  {

    private var currentDialog: Dialog? = null

    fun hideCurrentDialog(){
        currentDialog?.hide()
        currentDialog = null
    }

    fun showGeneralLoadingDialog(activity: Activity){
        val dialog = setupSimpleLoadingDialog(activity)
        val body = dialog.findViewById<TextView>(R.id.dialog_message)

        body.text = activity.getString(R.string.dialog_loading_general)
        dialog.show()

        currentDialog = dialog
    }

    fun showEmailVerificationDialog(activity: Activity) {
        val dialog = setupSimpleDialog(activity)

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val body = dialog.findViewById<TextView>(R.id.dialog_body)
        val button = dialog.findViewById<TextView>(R.id.dialog_button)

        title.text = activity.getString(R.string.dialog_email_title)
        body.text = activity.getString(R.string.dialog_email_body)
        button.text = activity.getString(R.string.dialog_email_button_text)

        button.setOnClickListener {
            val myIntent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(myIntent)

            dialog.dismiss()
        }

        dialog.show()
    }

    fun showCancelChallengeDialog(activity: Activity, onClickListener: View.OnClickListener) {
        val dialog = setupSimpleDialog(activity)

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val body = dialog.findViewById<TextView>(R.id.dialog_body)
        val buttonContinue = dialog.findViewById<TextView>(R.id.dialog_button)
        val buttonBack = dialog.findViewById<TextView>(R.id.dialog_button_2)

        buttonBack.visibility = View.VISIBLE

        title.text = activity.getString(R.string.dialog_challenge_title)
        body.text = activity.getString(R.string.dialog_challenge_body)
        buttonContinue.text = activity.getString(R.string.dialog_challenge_button_confirm_text)
        buttonBack.text = activity.getString(R.string.dialog_challenge_button_cancel_text)

        buttonContinue.setOnClickListener(onClickListener)
        buttonBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showUnfollowDialog(activity: Activity, name: String?, onClickListener: View.OnClickListener) {
        val dialog = setupSimpleDialog(activity)

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val body = dialog.findViewById<TextView>(R.id.dialog_body)
        val buttonContinue = dialog.findViewById<TextView>(R.id.dialog_button)
        val buttonBack = dialog.findViewById<TextView>(R.id.dialog_button_2)

        buttonBack.visibility = View.VISIBLE

        if(name == null){
            title.text = activity.getString(R.string.profile_dialog_title_2)
        } else {
            title.text = activity.getString(R.string.profile_dialog_title, name)
        }

        body.text = activity.getString(R.string.profile_dialog_body)
        buttonContinue.text = activity.getString(R.string.profile_dialog_button_confirm_text)
        buttonBack.text = activity.getString(R.string.profile_dialog_button_cancel_text)

        buttonContinue.setOnClickListener(onClickListener)
        buttonBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        currentDialog = dialog
    }

    private fun setupSimpleDialog(activity: Activity): Dialog{
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.simple_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog
    }

    private fun setupSimpleLoadingDialog(activity: Activity): Dialog{
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loading_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog
    }
}