package nl.inholland.myvitality.ui.widgets.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import nl.inholland.myvitality.R
import nl.inholland.myvitality.ui.authentication.login.LoginActivity

object Dialogs  {

    private var currentDialog: Dialog? = null

    fun hideCurrentDialog(){
        if (currentDialog != null && currentDialog?.isShowing == true) {
            currentDialog?.dismiss();
        }

        currentDialog = null
    }

    fun showGeneralLoadingDialog(activity: Activity){
        val dialog = setupSimpleLoadingDialog(activity)
        val body = dialog.findViewById<TextView>(R.id.dialog_message)

        body.text = activity.getString(R.string.dialog_loading_general)
        dialog.show()
    }

    fun showAccountDeletionDialog(activity: Activity, onClickListener: View.OnClickListener) {
        val dialog = setupSimpleDialog(activity, true)

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val body = dialog.findViewById<TextView>(R.id.dialog_body)
        val button = dialog.findViewById<TextView>(R.id.dialog_button)
        val buttonCancel = dialog.findViewById<TextView>(R.id.dialog_button_2)

        title.text = activity.getString(R.string.profile_delete_dialog_title)
        body.text = activity.getString(R.string.profile_delete_dialog_body)
        button.text = activity.getString(R.string.profile_delete_dialog_confirm)

        button.setBackgroundResource(R.drawable.button_delete)
        button.setOnClickListener(onClickListener)

        dialog.show()
    }

    fun showCalendarEventDialog(activity: Activity, onClickListener: View.OnClickListener) {
        val dialog = setupSimpleDialog(activity, withCancelButton = true)

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val body = dialog.findViewById<TextView>(R.id.dialog_body)
        val buttonConfirm = dialog.findViewById<TextView>(R.id.dialog_button)

        title.text = activity.getString(R.string.dialog_activity_calendar_title)
        body.text = activity.getString(R.string.dialog_activity_calendar_body)
        buttonConfirm.text = activity.getString(R.string.dialog_activity_calendar_button_confirm)

        buttonConfirm.setOnClickListener(onClickListener)

        dialog.show()
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
            activity.finish()

            dialog.dismiss()
        }

        dialog.show()
    }

    fun showCancelChallengeDialog(activity: Activity, onClickListener: View.OnClickListener) {
        val dialog = setupSimpleDialog(activity, withCancelButton = true)

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val body = dialog.findViewById<TextView>(R.id.dialog_body)
        val buttonContinue = dialog.findViewById<TextView>(R.id.dialog_button)

        title.text = activity.getString(R.string.dialog_activity_title)
        body.text = activity.getString(R.string.dialog_activity_body)
        buttonContinue.text = activity.getString(R.string.dialog_activity_button_confirm_text)

        buttonContinue.setOnClickListener(onClickListener)

        dialog.show()
    }

    fun showUnfollowDialog(activity: Activity, name: String?, onClickListener: View.OnClickListener) {
        val dialog = setupSimpleDialog(activity, withCancelButton = true)

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val body = dialog.findViewById<TextView>(R.id.dialog_body)
        val buttonContinue = dialog.findViewById<TextView>(R.id.dialog_button)

        if(name == null){
            title.text = activity.getString(R.string.profile_dialog_title_2)
        } else {
            title.text = activity.getString(R.string.profile_dialog_title, name)
        }

        body.text = activity.getString(R.string.profile_dialog_body)
        buttonContinue.text = activity.getString(R.string.profile_dialog_button_confirm_text)

        buttonContinue.setOnClickListener(onClickListener)

        dialog.show()
    }

    fun showDeletePostDialog(activity: Activity, onClickListener: View.OnClickListener) {
        val dialog = setupSimpleDialog(activity, withCancelButton = true)

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val body = dialog.findViewById<TextView>(R.id.dialog_body)
        val buttonContinue = dialog.findViewById<TextView>(R.id.dialog_button)

        title.text = activity.getString(R.string.post_dialog_title)
        body.text = activity.getString(R.string.post_dialog_body)
        buttonContinue.text = activity.getString(R.string.post_dialog_button_confirm_text)

        buttonContinue.setOnClickListener(onClickListener)

        dialog.show()
    }

    fun createNotificationSettingsDialog(activity: Activity, isEnabled: Boolean): Dialog {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.notification_settings_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val switch = dialog.findViewById<SwitchCompat>(R.id.notifications_general_switch)

        switch.isChecked = isEnabled
        currentDialog = dialog

        return dialog
    }

    private fun setupSimpleDialog(activity: Activity, withCancelButton: Boolean? = false): Dialog{
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.simple_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if(withCancelButton == true){
            val buttonCancel = dialog.findViewById<TextView>(R.id.dialog_button_2)

            buttonCancel.visibility = View.VISIBLE
            buttonCancel.setBackgroundResource(R.drawable.button_secondary)
            buttonCancel.stateListAnimator = null
            buttonCancel.setTextColor(activity.getColor(R.color.primary))
            buttonCancel.text = activity.getString(R.string.cancel_text)

            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }
        }

        currentDialog = dialog
        return dialog
    }

    private fun setupSimpleLoadingDialog(activity: Activity): Dialog{
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loading_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        currentDialog = dialog
        return dialog
    }
}