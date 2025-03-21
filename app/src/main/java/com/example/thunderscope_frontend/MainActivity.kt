package com.example.thunderscope_frontend

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.thunderscope_frontend.ui.user.SignupActivity
import com.example.thunderscope_frontend.ui.utils.AsteriskPasswordTransformation

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var togglePasswordButton: TextView
    private lateinit var loginButton: Button

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity) // Ensure XML file name is correct

        // Initialize UI components
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        togglePasswordButton = findViewById(R.id.tvShowPassword)
        loginButton = findViewById(R.id.btnLogin)

        // Apply default asterisk transformation initially
        passwordEditText.transformationMethod = AsteriskPasswordTransformation()
        passwordEditText.typeface = Typeface.MONOSPACE  // Maintain consistent font

        // Set click listener for password visibility toggle
        togglePasswordButton.setOnClickListener { togglePasswordVisibility() }


        //Place Holder
        loginButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent) // Redirects to SignupActivity
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            // Show actual password (plain text)
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordEditText.transformationMethod = null // Remove transformation to show text
        } else {
            // Hide password using asterisks (****)
            passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordEditText.transformationMethod = AsteriskPasswordTransformation()
        }

        // Keep font consistency
        passwordEditText.typeface = Typeface.MONOSPACE

        // Keep cursor at the end
        passwordEditText.setSelection(passwordEditText.text.length)

        // Toggle button text
        togglePasswordButton.text = if (isPasswordVisible) "HIDE" else "SHOW"
    }

}
