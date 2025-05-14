package com.example.xray_frontend.ui.login

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.databinding.ActivityLoginBinding
import com.example.xray_frontend.ui.casedashboard.CaseDashboardActivity
import com.example.xray_frontend.ui.signup.SignUpActivity
import com.example.xray_frontend.ui.utils.AsteriskPasswordTransformation

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(ThunderscopeRepository(this))
    }

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupViews() {
        // Initially mask password
        binding.etPassword.transformationMethod = AsteriskPasswordTransformation()
        binding.etPassword.typeface = Typeface.MONOSPACE
    }

    private fun setupListeners() {
        binding.tvShowPassword.setOnClickListener { togglePasswordVisibility() }

        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                loginViewModel.login(email, password)
            }
        }

        binding.linkSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.etPassword.transformationMethod = null
        } else {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etPassword.transformationMethod = AsteriskPasswordTransformation()
        }

        binding.etPassword.typeface = Typeface.MONOSPACE
        binding.etPassword.setSelection(binding.etPassword.text.length)
        binding.tvShowPassword.text = if (isPasswordVisible) "HIDE" else "SHOW"
    }

    private fun observeViewModel() {
        loginViewModel.apply {
            isLoggedIn.observe(this@LoginActivity) { isLoggedIn ->
                if (isLoggedIn) {
                    startActivity(Intent(this@LoginActivity, CaseDashboardActivity::class.java))
                    finishAffinity()
                }
            }

            loginResult.observe(this@LoginActivity) { result ->
                result?.token?.let { token ->
                    Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, CaseDashboardActivity::class.java))
                    finishAffinity()
                }
            }

            isLoading.observe(this@LoginActivity) { isLoading ->
                // Show/hide loading indicator if you want
            }

            errorMessage.observe(this@LoginActivity) { error ->
                if (error.isNotEmpty()) {
                    Toast.makeText(this@LoginActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        return when {
            email.isEmpty() -> {
                binding.etEmail.error = "Email is required"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Enter a valid email address"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password is required"
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Password must be at least 6 characters"
                false
            }
            else -> true
        }
    }
}

//class LoginActivity : AppCompatActivity() {
//
//    // UI components
//    private lateinit var emailEditText: EditText
//    private lateinit var passwordEditText: EditText
//    private lateinit var togglePasswordButton: TextView
//    private lateinit var loginButton: Button
//    private lateinit var linkSignup: TextView
//
//    // HTTP client for making network requests
//    private val client = OkHttpClient()
//
//    // Boolean to track password visibility
//    private var isPasswordVisible = false
//
//    companion object {
//        private const val BASE_URL = "http://10.0.2.2:8080/api"
//        private const val LOGIN_URL = "$BASE_URL/doctors/login"
//        private const val ERROR_LOGIN_FAILED = "Login failed. Please try again."
//        private const val ERROR_USER_NOT_FOUND = "User not found."
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//
//        // POLICY WORKAROUND - Refactor Later with MVVM Architecture
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
//
//        // Initialize UI components
//        emailEditText = findViewById(R.id.etEmail)
//        passwordEditText = findViewById(R.id.etPassword)
//        togglePasswordButton = findViewById(R.id.tvShowPassword)
//        loginButton = findViewById(R.id.btnLogin)
//        linkSignup = findViewById(R.id.linkSignup)
//
//        // Apply default password masking
//        passwordEditText.transformationMethod = AsteriskPasswordTransformation()
//        passwordEditText.typeface = Typeface.MONOSPACE
//
//        // Toggle password visibility when clicking the button
//        // HANDLE PASSWORD VISIBILITY IN THE XML, NOT ON THE KOTLIN LOGIC
//        togglePasswordButton.setOnClickListener { togglePasswordVisibility() }
//
//        // On click listener for Signup
//        linkSignup.setOnClickListener {
//            val intent = Intent(this, SignUpActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        // Handle login button click
//        loginButton.setOnClickListener { if (validateInputs()) attemptLogin() }
//    }
//
//    /**
//     * Validates signup inputs before sending the login request.
//     * Ensures email and password fields are not empty and properly formatted.
//     */
//    private fun validateInputs(): Boolean {
//        val email = emailEditText.text.toString().trim()
//        val password = passwordEditText.text.toString().trim()
//
//        return when {
//            email.isEmpty() -> showError(emailEditText, "Email is required")
//            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showError(
//                emailEditText,
//                "Enter a valid email address"
//            )
//
//            password.isEmpty() -> showError(passwordEditText, "Password is required")
//            password.length < 6 -> showError(
//                passwordEditText,
//                "Password must be at least 6 characters"
//            )
//
//            else -> true
//        }
//    }
//
//
//    private fun showError(field: EditText, message: String): Boolean {
//        field.error = message
//        return false
//    }
//
//
//    // Toggles password visibility when the "SHOW/HIDE" button is clicked.
//    private fun togglePasswordVisibility() {
//        isPasswordVisible = !isPasswordVisible
//
//        if (isPasswordVisible) {
//            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
//            passwordEditText.transformationMethod = null
//        } else {
//            passwordEditText.inputType =
//                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//            passwordEditText.transformationMethod = AsteriskPasswordTransformation()
//        }
//
//        // Maintain font consistency
//        passwordEditText.typeface = Typeface.MONOSPACE
//        passwordEditText.setSelection(passwordEditText.text.length)
//        togglePasswordButton.text = if (isPasswordVisible) "HIDE" else "SHOW"
//    }
//
//
//    // Sends a login request to the backend with the provided email and password.
//    private fun attemptLogin() {
//        val email = emailEditText.text.toString().trim()
//        val password = passwordEditText.text.toString().trim()
//
//        val jsonBody = JSONObject().apply {
//            put("email", email)
//            put("password", password)
//        }
//
//        val request = Request.Builder()
//            .url(LOGIN_URL)
//            .post(
//                RequestBody.create(
//                    "application/json; charset=utf-8".toMediaTypeOrNull(),
//                    jsonBody.toString()
//                )
//            )
//            .build()
//
//        handleApiCall(request, ::handleLoginResponse, ERROR_LOGIN_FAILED)
//    }
//
//
//    private fun handleLoginResponse(response: Response) {
//        if (!response.isSuccessful) {
//            handleErrorResponse(response)
//            return
//        }
//
//        response.body?.string()?.let { responseBody ->
//            val jsonResponse = JSONObject(responseBody)
//            val token = jsonResponse.optString("token", "")
//
//            if (token.isNotEmpty()) {
//                saveToken(token)
//                runOnUiThread {
//                    showSuccessToast("Login Successful!")
//                    redirectToActivity(CaseDashboardActivity::class.java)
//                }
//            } else {
//                runOnUiThread {
//                    showErrorToast(ERROR_USER_NOT_FOUND)
//                }
//            }
//        } ?: runOnUiThread { showErrorToast("Unexpected error. Please try again.") }
//    }
//
//
//    private fun handleErrorResponse(response: Response) {
//        val errorMessage = if (response.code == 404) ERROR_USER_NOT_FOUND else ERROR_LOGIN_FAILED
//        Log.e("API_ERROR", "Response Code: ${response.code}")
//
//        runOnUiThread {
//            showErrorToast(errorMessage)
//        }
//    }
//
//
//    private fun handleApiCall(
//        request: Request,
//        successHandler: (Response) -> Unit,
//        errorMessage: String
//    ) {
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("API_ERROR", e.message ?: "Unknown error")
//                runOnUiThread { showErrorToast(errorMessage) }
//            }
//
//            override fun onResponse(call: Call, response: Response) = successHandler(response)
//        })
//    }
//
//
//    private fun showErrorToast(message: String) {
//        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
//    }
//
//    private fun showSuccessToast(message: String) {
//        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
//    }
//
//    private fun saveToken(token: String) {
//        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//        with(sharedPreferences.edit()) {
//            putString("token", token)
//            apply()
//        }
//    }
//
//    private fun redirectToActivity(activityClass: Class<*>) {
//        val intent = Intent(this, activityClass)
//        startActivity(intent)
//        finish()
//    }
//}
