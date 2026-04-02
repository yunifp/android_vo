package com.bit.bilikdigitalkarawang.helpers

import com.bit.bilikdigitalkarawang.common.Constant

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object BluetoothPermissionHelper {

    private const val REQUEST_CODE_BLUETOOTH = 2001

    fun checkAndRequestBluetoothPermissions(activity: Activity): Boolean {
        val permissionsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        return if (permissionsNeeded.isNotEmpty()) {
            Log.w(Constant.LOG_TAG, "Meminta izin Bluetooth: $permissionsNeeded")
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toTypedArray(), REQUEST_CODE_BLUETOOTH)
            false
        } else {
            true
        }
    }
}
