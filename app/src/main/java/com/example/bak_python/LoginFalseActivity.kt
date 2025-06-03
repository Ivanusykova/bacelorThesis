package com.example.bak_python

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hashPassword

class LoginFalseActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        setContentView(R.layout.activty_login)

        val username: EditText = findViewById(R.id.username)
        val password: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.loginButton)
        val registerButton: Button = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            val storedPassword = sharedPrefs.getString(user, null)
            val hashedInput = hashPassword(pass)

            if (storedPassword == hashedInput) {
                sharedPrefs.edit().putBoolean("isLoggedIn", true).apply()
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ScanActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show()
            }
        }
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}