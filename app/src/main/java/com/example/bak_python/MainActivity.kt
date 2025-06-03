package com.example.bak_python

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

// ---- sources ----
// coworking with AI assistent
// https://docs.opencv.org/4.x/d6/d00/tutorial_py_root.html
// https://m2.material.io/components/dialogs/android#using-dialogs
// https://medium.com/%40CharlesAE/custom-alert-dialogs-android-374d1a6b9f5b
// https://developer.android.com/develop/ui/views/components/spinner
// https://developer.android.com/develop/ui/views/layout/recyclerview
// https://stackoverflow.com/questions/19218775/android-copy-assets-to-internal-storage
// https://stackoverflow.com/questions/58931675/image-processing-using-chaquopy-on-android-studio
// https://proandroiddev.com/chaquopy-using-python-in-android-apps-dd5177c9ab6b
// https://medium.com/inside-ppl-b7/make-a-login-session-on-android-using-sharedpreferences-48b6eb22f17c
// https://stackoverflow.com/questions/67779327/android-kotlin-how-to-use-hash-password-for-login
// https://dev.to/apoorvmishra21/capture-with-camerax-57am
// https://developer.android.com/media/camera/camerax/take-photo
// https://sirv.com/help/articles/rotate-photos-to-be-upright/
// https://docs.opencv.org/4.x/da/df5/tutorial_py_sift_intro.html
// https://www.geeksforgeeks.org/measure-similarity-between-images-using-python-opencv/
// ---- sources ----

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = this.getSharedPreferences("UserPrefs", MODE_PRIVATE)
        prefs.edit().putBoolean("isLoggedIn", false).apply()

        findViewById<Button>(R.id.scan_button).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        findViewById<Button>(R.id.login_button).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<Button>(R.id.download_button).setOnClickListener {
            startActivity(Intent(this, DownloadActivity::class.java))
        }
    }
}
