package nl.inholland.myvitality.util

import java.util.Arrays
import android.content.pm.PackageManager
import java.util.ArrayList
import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionsUtil {

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(context: Context, requestCodeOfPermission: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(
            (context as Activity?)!!,
            permissions,
            requestCodeOfPermission
        )
    }

    fun onRequestPermissionsResult(permissions: Array<String?>, grantResults: IntArray?, iPermissionResult: IPermissionResult) {
        val grantedPermissions: MutableList<String> = ArrayList()
        if (grantResults != null && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) permissions[i]?.let {
                    grantedPermissions.add(it)
                }
            }
            iPermissionResult.grantedPermissions(grantedPermissions.toTypedArray())
        }
    }

    fun isPermissionExists(permissions: Array<String?>, permission: String): Boolean {
        return Arrays.asList(*permissions).contains(permission)
    }

    interface IPermissionResult {
        fun grantedPermissions(permission: Array<String>?)
    }
}