package com.example.bak_python

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hashPassword

class RegisterActivity : AppCompatActivity() {

    private lateinit var regUsername: EditText
    private lateinit var regPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        regUsername = findViewById(R.id.regUsername)
        regPassword = findViewById(R.id.regPassword)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        registerButton.setOnClickListener {
            val username = regUsername.text.toString()
            val password = regPassword.text.toString()
            val hashedPassword = hashPassword(password)
            sharedPrefs.edit().putString(username, hashedPassword).apply()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            } else {
                if (sharedPrefs.contains(username)) {
                    Toast.makeText(this, "User already exists!", Toast.LENGTH_SHORT).show()
                } else {
                    val hashedPassword = hashPassword(password)
                    sharedPrefs.edit().putString(username, hashedPassword).apply()
                    Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
        loginButton.setOnClickListener{ startActivity(Intent(this, LoginActivity::class.java))}
    }
}
