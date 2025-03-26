package com.example.thunderscope_frontend

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.thunderscope_frontend.ui.patient.PatientListActivity
import com.example.thunderscope_frontend.ui.user.SignupActivity
import com.example.thunderscope_frontend.ui.utils.AsteriskPasswordTransformation
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // UI components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var togglePasswordButton: TextView
    private lateinit var loginButton: Button

    // HTTP client for making network requests
    private val client = OkHttpClient()

    // Boolean to track password visibility
    private var isPasswordVisible = false

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/api"
        private const val LOGIN_URL = "$BASE_URL/doctors/login"
        private const val ERROR_LOGIN_FAILED = "Login failed. Please try again."
        private const val ERROR_USER_NOT_FOUND = "User not found. Redirecting to registration."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Initialize UI components
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        togglePasswordButton = findViewById(R.id.tvShowPassword)
        loginButton = findViewById(R.id.btnLogin)

        // Apply default password masking
        passwordEditText.transformationMethod = AsteriskPasswordTransformation()
        passwordEditText.typeface = Typeface.MONOSPACE

        // Toggle password visibility when clicking the button
        togglePasswordButton.setOnClickListener { togglePasswordVisibility() }

        // Handle login button click
        loginButton.setOnClickListener { if (validateInputs()) attemptLogin() }
    }

    /**
     * Validates user inputs before sending the login request.
     * Ensures email and password fields are not empty and properly formatted.
     */
    private fun validateInputs(): Boolean {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        return when {
            email.isEmpty() -> showError(emailEditText, "Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showError(
                emailEditText,
                "Enter a valid email address"
            )

            password.isEmpty() -> showError(passwordEditText, "Password is required")
            password.length < 6 -> showError(
                passwordEditText,
                "Password must be at least 6 characters"
            )

            else -> true
        }
    }


    private fun showError(field: EditText, message: String): Boolean {
        field.error = message
        return false
    }


    // Toggles password visibility when the "SHOW/HIDE" button is clicked.
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordEditText.transformationMethod = null
        } else {
            passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordEditText.transformationMethod = AsteriskPasswordTransformation()
        }

        // Maintain font consistency
        passwordEditText.typeface = Typeface.MONOSPACE
        passwordEditText.setSelection(passwordEditText.text.length)
        togglePasswordButton.text = if (isPasswordVisible) "HIDE" else "SHOW"
    }


    // Sends a login request to the backend with the provided email and password.
    private fun attemptLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = Request.Builder()
            .url(LOGIN_URL)
            .post(
                RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    jsonBody.toString()
                )
            )
            .build()

        handleApiCall(request, ::handleLoginResponse, ERROR_LOGIN_FAILED)
    }


    private fun handleLoginResponse(response: Response) {
        if (!response.isSuccessful) {
            handleErrorResponse(response)
            return
        }

        response.body?.string()?.let { responseBody ->
            val jsonResponse = JSONObject(responseBody)
            val token = jsonResponse.optString("token", "")

            if (token.isNotEmpty()) {
                saveToken(token)
                runOnUiThread {
                    showSuccessToast("Login Successful!")
                    redirectToActivity(PatientListActivity::class.java)
                }
            } else {
                runOnUiThread {
                    showErrorToast(ERROR_USER_NOT_FOUND)
                    redirectToActivity(SignupActivity::class.java)
                }
            }
        } ?: runOnUiThread { showErrorToast("Unexpected error. Please try again.") }
    }


    private fun handleErrorResponse(response: Response) {
        val errorMessage = if (response.code == 404) ERROR_USER_NOT_FOUND else ERROR_LOGIN_FAILED
        Log.e("API_ERROR", "Response Code: ${response.code}")

        runOnUiThread {
            showErrorToast(errorMessage)
            if (response.code == 404) {
                redirectToActivity(SignupActivity::class.java)
            }
        }
    }


    private fun handleApiCall(
        request: Request,
        successHandler: (Response) -> Unit,
        errorMessage: String
    ) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", e.message ?: "Unknown error")
                runOnUiThread { showErrorToast(errorMessage) }
            }

            override fun onResponse(call: Call, response: Response) = successHandler(response)
        })
    }


    private fun showErrorToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    private fun showSuccessToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("token", token)
            apply()
        }
    }

    private fun redirectToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }
}
