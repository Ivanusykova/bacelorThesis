package com.example.bak_python

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        if (sharedPrefs.getBoolean("isLoggedIn", false)) {
            setContentView(R.layout.activity_login_true)

            val scanBtn: Button = findViewById(R.id.ScanLoginButton)
            scanBtn.setOnClickListener {
                startActivity(Intent(this, ScanActivity::class.java))
                finish()
            }

            val logoutBtn: Button = findViewById(R.id.LogOutButton)
            logoutBtn.setOnClickListener{
                startActivity(Intent(this, LoginFalseActivity::class.java))
            }

        } else {
            startActivity(Intent(this, LoginFalseActivity::class.java))
        }
    }
}
