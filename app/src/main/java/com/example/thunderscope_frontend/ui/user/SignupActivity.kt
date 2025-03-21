package com.example.thunderscope_frontend.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.ui.patient.PatientListActivity
import com.google.android.material.textfield.TextInputEditText

class SignupActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var buttonCreateAccount: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_activity)

        // Initialize UI components
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        nameEditText = findViewById(R.id.etName)
        phoneNumberEditText = findViewById(R.id.etPhoneNumber)
        buttonCreateAccount = findViewById(R.id.btnCreateAccount)

        // Set Input Fields Placeholder
        setPlaceholder(emailEditText as TextInputEditText, "Enter email")
        setPlaceholder(passwordEditText as TextInputEditText, "Enter password")
        setPlaceholder(nameEditText as TextInputEditText, "Enter name")
        setPlaceholder(phoneNumberEditText as TextInputEditText, "Enter phone number")

        // Set click listener to navigate to PatientListActivity
        buttonCreateAccount.setOnClickListener {
            val intent = Intent(this, PatientListActivity::class.java)
            startActivity(intent)
        }
    }

    // Input Field Placeholder Logic
    private fun setPlaceholder(editText: TextInputEditText, placeholder: String) {
        editText.setText(placeholder)

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && editText.text.toString() == placeholder) {
                editText.setText("") // Remove placeholder on focus
            } else if (!hasFocus && editText.text.toString().isEmpty()) {
                editText.setText(placeholder) // Restore placeholder if empty
            }
        }
    }
}
