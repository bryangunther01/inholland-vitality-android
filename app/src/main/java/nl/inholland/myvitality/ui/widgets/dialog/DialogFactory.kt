package nl.dtt.ebike.ui.widgets.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import nl.dtt.ebike.App
import nl.dtt.ebike.R
import nl.dtt.ebike.ui.menu.MenuActivity_
import nl.dtt.ebike.ui.menu.location.LocationFragment_
import nl.dtt.ebike.ui.sessions.SessionFragment_
import nl.dtt.ebike.utils.TransactionUtils
import nl.inholland.myvitality.R


object DialogFactory {

    /**
     * Function to create and show the terms of use dialog on the given activity
     *
     * @param activity the activity where we need to show the dialog
     * @param fragmentManager the fragmentManager to register the dialog
     */
    fun createTermsOfUseDialog(@NonNull activity: Activity, fragmentManager: FragmentManager) {
        SimpleDialog.Builder()
                .withTitle(activity.getString(R.string.terms_of_use_title))
                .withBody(App.instance.sharedPreference?.getTermsOfUse().toString(), false, true)
                .withFirstButton(
                        activity.getString(R.string.terms_of_use_button),
                        object : SimpleDialog.DialogButtonClickListener {
                            override fun onDialogButtonClicked(dialog: DialogFragment) {
                                dialog.dismiss()
                            }
                        }
                ).build().show(getFragmentTransaction(fragmentManager, "termsOfUseDialog"), "termsOfUseDialog")
    }

    /**
     * Function to create and show the confirm registration dialog on the given activity
     *
     * @param activity the activity where we need to show the dialog
     * @param fragmentManager the fragmentManager to register the dialog
     */
    fun createConfirmRegistrationDialog(@NonNull activity: Activity, fragmentManager: FragmentManager) {
        SimpleDialog.Builder()
                .withTitle(activity.getString(R.string.register_success_confirm_title))
                .withBody(activity.getString(R.string.register_success_confirm_body), true)
                .build().show(getFragmentTransaction(fragmentManager, "confirmRegistrationDialog"), "confirmRegistrationDialog")
    }

    /**
     * Function to create and show the extension failed registration dialog on the given activity
     *
     * @param activity the activity where we need to show the dialog
     * @param fragmentManager the fragmentManager to register the dialog
     */
    fun createExtensionFailedRegistrationDialog(@NonNull activity: Activity, fragmentManager: FragmentManager) {
        SimpleDialog.Builder()
                .withTitle(activity.getString(R.string.register_success_failed_extension_title))
                .withBody(activity.getString(R.string.register_success_failed_extension_body), true)
                .build().show(getFragmentTransaction(fragmentManager, "extensionFailedRegistrationDialog"), "extensionFailedRegistrationDialog")
    }


    /**
     * Function to create and show the bike usage dialog on the given activity
     *
     * @param activity the activity where we need to show the dialog
     * @param fragmentManager the fragmentManager to register the dialog
     */
    fun createBikeUsageDialog(@NonNull activity: Activity, fragmentManager: FragmentManager) {
        SimpleDialog.Builder()
                .withTitle(activity.getString(R.string.usage_title))
                .withBody(activity.getString(R.string.usage_message), false)
                .withFirstButton(activity.getString(R.string.usage_button),
                        object : SimpleDialog.DialogButtonClickListener {
                            override fun onDialogButtonClicked(dialog: DialogFragment) {
                                dialog.dismiss()
                            }
                        })
                .build().show(getFragmentTransaction(fragmentManager, "bikeUsageDialog"), "bikeUsageDialog")
    }

    /**
     * Function to create and show the location permissions dialog on the given activity
     *
     * @param activity the activity where we need to show the dialog
     * @param fragmentManager the fragmentManager to register the dialog
     */
    fun  createLocationPermissionDialog(@NonNull activity: Activity, fragmentManager: FragmentManager, showedOnLocationOverview: Boolean? = false) : SimpleDialog {
       return SimpleDialog.Builder()
                .withTitle(activity.getString(R.string.location_access_title))
                .withBody(activity.getString(R.string.location_access_message), false)
                .withFirstButton(activity.getString(R.string.location_access_allow),
                        object : SimpleDialog.DialogButtonClickListener {
                            override fun onDialogButtonClicked(dialog: DialogFragment) {
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    // Check if the user has granted the location permission
                                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                                    } else {
                                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                        activity.startActivity(intent)
                                    }
                                    dialog.dismiss()
                                }
                            }
                        })
                .withSecondButton(activity.getString(R.string.location_access_disallow),
                        object : SimpleDialog.DialogButtonClickListener {
                            override fun onDialogButtonClicked(dialog: DialogFragment) {
                                if (showedOnLocationOverview == true) {
                                    TransactionUtils.goToFragment(fragmentManager, R.id.container, LocationFragment_.builder().build(), false)
                                }
                                dialog.dismiss()
                            }
                        })
                .setCancelableOutside(false)
                .build()
    }

    /**
     * Function to create and show the active session dialog on the given activity
     *
     * @param activity the activity where we need to show the dialog
     * @param fragmentManager the fragmentManager to register the dialog
     */
    fun createActiveSessionDialog(@NonNull activity: Activity, fragmentManager: FragmentManager, openedFromMenu: Boolean) {
        SimpleDialog.Builder()
                .withTitle(activity.getString(R.string.session_active_popup_title))
                .withBody(activity.getString(R.string.session_active_popup_message), false)
                .withFirstButton(
                        activity.getString(R.string.session_active_popup_button_view),
                        object : SimpleDialog.DialogButtonClickListener {
                            override fun onDialogButtonClicked(dialog: DialogFragment) {
                                dialog.dismiss()

                                if (openedFromMenu) {
                                    TransactionUtils.goToFragment(fragmentManager, R.id.container, SessionFragment_.builder().build(), false)
                                    (activity as MenuActivity_).guillotineAnimation?.close()
                                } else {
                                    activity.setResult(Activity.RESULT_OK, Intent())
                                    activity.finish()
                                }
                            }
                        })
                .withSecondButton(
                        activity.getString(R.string.session_active_popup_button_back),
                        object : SimpleDialog.DialogButtonClickListener {
                            override fun onDialogButtonClicked(dialog: DialogFragment) {
                                dialog.dismiss()
                            }
                        })
                .build().show(getFragmentTransaction(fragmentManager, "activeSessionDialog"), "activeSessionDialog")
    }

    /**
     * Function to create and show the location distance dialog on the given activity
     *
     * @param activity the activity where we need to show the dialog
     * @param fragmentManager the fragmentManager to register the dialog
     */
    fun createLocationDistanceDialog(@NonNull activity: Activity, fragmentManager: FragmentManager) {
        SimpleDialog.Builder()
                .withTitle(activity.getString(R.string.session_distance_location_title))
                .withBody(activity.getString(R.string.session_distance_location_message), false)
                .withFirstButton(activity.getString(R.string.session_distance_location_button),
                        object : SimpleDialog.DialogButtonClickListener {
                            override fun onDialogButtonClicked(dialog: DialogFragment) {
                                dialog.dismiss()
                            }
                        })
                .build().show(getFragmentTransaction(fragmentManager, "locationDistanceDialog"), "locationDistanceDialog")
    }

    /**
     * Function to create and show the bik not found dialog on the given activity
     *
     * @param activity the activity where we need to show the dialog
     * @param fragmentManager the fragmentManager to register the dialog
     */
    fun createBikeNotFoundDialog(@NonNull activity: Activity, fragmentManager: FragmentManager) {
        SimpleDialog.Builder()
                .withTitle(activity.getString(R.string.session_bike_not_found_popup_title))
                .withBody(activity.getString(R.string.session_bike_not_found_popup_message), false)
                .withFirstButton(activity.getString(R.string.session_bike_not_found_popup_button),
                        object : SimpleDialog.DialogButtonClickListener {
                            override fun onDialogButtonClicked(dialog: DialogFragment) {
                                dialog.dismiss()
                            }
                        })
                .build().show(getFragmentTransaction(fragmentManager, "bikeNotFoundDialog"), "bikeNotFoundDialog")
    }

    /**
     * Function to create and show the server error dialog on the given activity
     *
     * @param activity the activity where we need to show the dialog
     * @param fragmentManager the fragmentManager to register the dialog
     */
    fun createServerErrorDialog(@NonNull activity: Activity, fragmentManager: FragmentManager) {
        SimpleDialog.Builder()
                .withTitle(activity.getString(R.string.dialog_server_title))
                .withBody(activity.getString(R.string.dialog_server_message), false)
                .withFirstButton(activity.getString(R.string.dialog_server_button),
                        object : SimpleDialog.DialogButtonClickListener {
                            override fun onDialogButtonClicked(dialog: DialogFragment) {
                                dialog.dismiss()
                            }
                        })
                .build().show(getFragmentTransaction(fragmentManager, "serverErrorDialog"), "serverErrorDialog")
    }


    @SuppressLint("CommitTransaction")
    private fun getFragmentTransaction(pFragmentManager: android.support.v4.app.FragmentManager, tag: String): FragmentTransaction {
        @SuppressLint("CommitTransaction") val ft = pFragmentManager.beginTransaction()
        val prev = pFragmentManager.findFragmentByTag(tag)
        if (prev != null) {
            ft.remove(prev)
        }

        return ft.addToBackStack(tag)
    }
}