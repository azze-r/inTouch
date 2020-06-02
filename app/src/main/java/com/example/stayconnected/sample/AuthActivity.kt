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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.stayconnected.R
import com.example.stayconnected.sample.util.checkSelfPermissionCompat
import com.example.stayconnected.sample.util.requestPermissionsCompat
import com.example.stayconnected.sample.util.shouldShowRequestPermissionRationaleCompat
import com.example.stayconnected.sample.util.showSnackbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_auth.*

const val PERMISSION_REQUEST_CAMERA = 0

class AuthActivity : AppCompatActivity(),ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var layout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        layout = findViewById(R.id.main_layout)
        showCameraPreview()
        button.visibility = View.GONE
        button.setOnClickListener {
            showCameraPreview()
            button.visibility = View.GONE
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                startCamera()
            } else {
                // Permission request was denied.
                button.visibility = View.VISIBLE

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

    private fun showCameraPreview() {
        // Check if the Camera permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            layout.showSnackbar("permission_available", Snackbar.LENGTH_SHORT)
            startCamera()
        } else {
            // Permission is missing and must be requested.
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.READ_CONTACTS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            layout.showSnackbar("access_required",
                Snackbar.LENGTH_INDEFINITE, "ok") {
                requestPermissionsCompat(arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSION_REQUEST_CAMERA)
            }

        } else {
            layout.showSnackbar("permission_not_available", Snackbar.LENGTH_SHORT)
            // Request the permission. The result will be received in onRequestPermissionResult().
            requestPermissionsCompat(arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_CAMERA)
        }
    }

    private fun startCamera() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}