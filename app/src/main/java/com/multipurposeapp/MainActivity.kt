package com.multipurposeapp

import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_ADMIN
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.CAMERA
import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.MODIFY_AUDIO_SETTINGS
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.RECORD_AUDIO
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.multipurposeapp.Utils.toastme


class MainActivity : AppCompatActivity() {
    private val delay = 3000L
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        Dexter.withContext(this)
            .withPermissions(
                GET_ACCOUNTS,
                READ_CONTACTS,
                READ_MEDIA_IMAGES,
                RECORD_AUDIO,CAMERA,BLUETOOTH,READ_PHONE_STATE,MODIFY_AUDIO_SETTINGS,
                BLUETOOTH,BLUETOOTH_ADMIN,BLUETOOTH_CONNECT
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        // do you work now
                        toastme("All the permissions are granted..")
                        goToReg()
                    }else{
                        goToReg()
                        //toastme("Please grant all the permissions...")
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {

                }
            }).check()

    }
    fun goToReg(){
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }, delay) // 3000 is the delayed time in milliseconds.
    }
}