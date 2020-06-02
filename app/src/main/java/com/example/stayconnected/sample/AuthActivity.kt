package com.example.stayconnected.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.stayconnected.R
import com.google.android.material.snackbar.Snackbar

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED) {
            Log.i("wtf", "permission granted")

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        else {
            Log.i("wtf", "permission not granted")
            val parentLayout =
                findViewById<View>(android.R.id.content)

            Snackbar.make(
                parentLayout,
                "This app can't display your contact records unless you...",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Grant Access", View.OnClickListener {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_CONTACTS
                        )
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_CONTACTS),
                            0
                        )
                    } else {
                        // User has checked the "Don't ask again" box but keeps hitting the button
                        val uri = Uri.fromParts(
                            "package",
                            this.packageName,
                            null
                        )
                        val intent = Intent()
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).data =
                            uri
                        this.startActivity(intent)
                    }
                }
                ).show()
        }
    }
}