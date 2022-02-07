package nl.inholland.myvitality.ui.authentication.recover

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnTextChanged
import nl.gunther.bryan.newsreader.utils.FieldValidationUtil
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import javax.inject.Inject

class AccountRecoverActivity : BaseActivity() {
    @BindView(R.id.recover_error) lateinit var errorField: TextView
    @BindView(R.id.recover_email) lateinit var email: EditText
    @BindView(R.id.recover_button) lateinit var recoverButton: Button

    @Inject
    lateinit var factory: AccountRecoveryViewModelFactory
    lateinit var viewModel: AccountRecoveryViewModel

    override fun layoutResourceId(): Int {
        return R.layout.activity_recover
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(AccountRecoveryViewModel::class.java)
    }

    @OnClick(R.id.recover_button)
    fun onClickRecoverAccount() {
        Dialogs.showAccountRecoveryDialog(this, View.OnClickListener {
            viewModel.sendRecoveryMail(email.text.toString())
            finish()
        })
    }

    @OnTextChanged(R.id.recover_email, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onInputFieldsChanged(){
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email.text).matches()
        recoverButton.isEnabled = isValid
    }

    @OnTextChanged(R.id.recover_email, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onEmailInputFieldChanged(){
        if(email.text.length > 3){
            FieldValidationUtil(this).setFieldState(email, Patterns.EMAIL_ADDRESS.matcher(email.text).matches(), errorField, getString(
                R.string.login_error_invalid_email))
        }
    }

    @OnClick(R.id.recover_cancel)
    fun onClickCancelRecover() {
        finish()
    }
}