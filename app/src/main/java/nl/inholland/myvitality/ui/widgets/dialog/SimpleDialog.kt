package nl.inholland.myvitality.ui.widgets.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import nl.inholland.myvitality.R


class SimpleDialog : DialogFragment() {

    private lateinit var loadedListener: DialogLoaded

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_dialog, container)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        if ((activity is DialogInterface.OnDismissListener)) {
            dialog.setOnDismissListener((activity as DialogInterface.OnDismissListener))
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        loadedListener.onDialogLoaded()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity = activity
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
    }

    class Builder {

        /**
         * The title of the dialog to show if not null
         */
        private var title: String? = null

        /**
         * The body of the dialog to show if not null
         */
        private var body: String? = null

        /**
         * Boolean if the dialog should be scrollable
         */
        private var scrollable: Boolean = false

        /**
         * Boolean if the dialog body must be centered or not
         */
        private var centeredBody: Boolean = false

        /**
         * The title of the first button to show if not null
         */
        private var firstButtonTitle: String? = null

        /**
         * The click listener of the first button to apply if not null
         */
        private var firstButtonClickListener: DialogButtonClickListener? = null

        /**
         * The title of the second button to show if not null
         */
        private var secondButtonTitle: String? = null

        /**
         * The click listener of the second button to apply if not null
         */
        private var secondButtonClickListener: DialogButtonClickListener? = null

        /**
         * Boolean if the dialog is cancelable outside
         */
        private var cancelableOutSide: Boolean = true

        /**
         * Function to build a SimpleDialog based on the given arguments
         */
        fun build(): SimpleDialog {
            val simpleDialog = SimpleDialog()
            val listener = object : DialogLoaded {
                override fun onDialogLoaded() {
                    simpleDialog.dialogTitle.text = title

                    simpleDialog.dialogScrollableLayout.visibility = if (scrollable) View.VISIBLE else View.GONE
                    simpleDialog.dialogLayout.visibility = if (scrollable) View.GONE else View.VISIBLE
                    if (scrollable) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                            simpleDialog.dialogScrollableBody.text = Html.fromHtml(body, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            simpleDialog.dialogScrollableBody.text = Html.fromHtml(body)
                        }

                        simpleDialog.dialogScrollableBody.gravity = if(centeredBody) Gravity.CENTER else Gravity.START
                    } else {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                            simpleDialog.dialogBody.text = Html.fromHtml(body, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            simpleDialog.dialogBody.text = Html.fromHtml(body)
                        }

                        simpleDialog.dialogBody.gravity = if(centeredBody) Gravity.CENTER else Gravity.START
                    }

                    if (firstButtonTitle != null && firstButtonClickListener != null) {
                        simpleDialog.dialogFirstButton.visibility = View.VISIBLE
                        simpleDialog.dialogFirstButton.text = firstButtonTitle
                        simpleDialog.dialogFirstButton.setOnClickListener {
                            firstButtonClickListener?.onDialogButtonClicked(simpleDialog)
                        }
                    }

                    if (secondButtonTitle != null && secondButtonClickListener != null) {
                        simpleDialog.dialogSecondButton.visibility = View.VISIBLE
                        simpleDialog.dialogSecondButton.text = secondButtonTitle
                        simpleDialog.dialogSecondButton.setOnClickListener {
                            secondButtonClickListener?.onDialogButtonClicked(simpleDialog)
                        }
                    }

                    simpleDialog.dialog.setCanceledOnTouchOutside(cancelableOutSide)
                    simpleDialog.dialog.setCancelable(cancelableOutSide)
                }
            }
            simpleDialog.loadedListener = listener

            return simpleDialog
        }


        /**
         * Method to append a title to the Builder
         *
         * @param title the title to display in the SimpleDialog
         *
         * @return this Builder to continue building
         */
        fun withTitle(title: String): Builder {
            this.title = title
            return this
        }

        /**
         * Method to append a body to the Builder
         *
         * @param body the title to display in the SimpleDialog
         *
         * @return this Builder to continue building
         */
        fun withBody(body: String, centered: Boolean, scrollable: Boolean? = false): Builder {
            this.body = body
            this.scrollable = scrollable!!
            this.centeredBody = centered
            return this
        }

        /**
         * Method to add the first button
         *
         * @param title the title of the button
         * @param onClickListener the click listener
         *
         * @return this Builder to continue building
         */
        fun withFirstButton(title: String, onClickListener: DialogButtonClickListener): Builder {
            this.firstButtonTitle = title
            this.firstButtonClickListener = onClickListener

            return this
        }

        /**
         * Method to add the second button
         *
         * @param title the title of the button
         * @param onClickListener the click listener
         *
         * @return this Builder to continue building
         */
        fun withSecondButton(title: String, onClickListener: DialogButtonClickListener): Builder {
            this.secondButtonTitle = title
            this.secondButtonClickListener = onClickListener

            return this
        }

        /**
         * Method to set cancelable outside
         *
         * @param cancelable boolean if cancelable outside
         *
         * @return this Builder to continue building
         */
        fun setCancelableOutside(cancelable: Boolean): Builder{
            this.cancelableOutSide = cancelable

            return this
        }
    }

    interface DialogLoaded {
        fun onDialogLoaded()
    }

    interface DialogButtonClickListener {
        fun onDialogButtonClicked(dialog: DialogFragment)
    }
}