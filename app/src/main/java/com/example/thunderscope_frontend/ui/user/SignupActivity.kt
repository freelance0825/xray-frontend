package com.example.thunderscope_frontend.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.thunderscope_frontend.MainActivity
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.ui.patient.PatientListActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class SignupActivity : AppCompatActivity() {

    // UI components
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var spinnerSpecialist: Spinner
    private lateinit var nameEditText: TextInputEditText
    private lateinit var phoneNumberEditText: TextInputEditText
    private lateinit var birthDateEditText: EditText
    private lateinit var creatAccountButton: Button
    private lateinit var linkLogin: TextView

    private var isFirstSelection = true

    // HTTP client for making network requests
    private val client = OkHttpClient()

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/api"
        private const val REGISTRATION_URL = "$BASE_URL/doctors/add"
        private const val ERROR_REGISTER_FAILED = "Registration Failed. Please try again."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_activity)

        // POLICY WORKAROUND - Refactor Later with MVVM Architecture
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Initialize UI components
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        passwordInputLayout = findViewById(R.id.passwordInputLayout) // Get parent TextInputLayout
        nameEditText = findViewById(R.id.etName)
        phoneNumberEditText = findViewById(R.id.etPhoneNumber)
        spinnerSpecialist = findViewById(R.id.spinnerSpecialist)
        birthDateEditText = findViewById(R.id.etBirthDate)
        creatAccountButton = findViewById(R.id.btnCreateAccount)
        linkLogin = findViewById(R.id.linkLogin)

        // Set Input Field Placeholders
        // PLACEHOLDER IS CONFIGURED FROM THE ATTR OF XML VIEW
//        setPlaceholder(emailEditText, "Enter email")
//        setPlaceholder(birthDateEditText as TextInputEditText, "MM-DD-YYYY")
//        setPasswordPlaceholder()
//        setPlaceholder(nameEditText, "Enter name")
//        setPlaceholder(phoneNumberEditText, "Enter phone number")


        /*  <----- START OF SPECIALIST LOGIC ------> */

        // Load Specialist Options from strings.xml
        val specialistOptions = resources.getStringArray(R.array.specialist_options)

        // Set up Spinner Adapter
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_dropdown, specialistOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialist.adapter = adapter

        // Set default selection (optional, choose index 0 or any preferred)
        spinnerSpecialist.setSelection(0)

        // Handle Click on Spinner to Show Dropdown
        spinnerSpecialist.setOnTouchListener { _, _ ->
            spinnerSpecialist.performClick() // Open the dropdown when clicked
            false
        }

        // Ensure the first selection works correctly
        spinnerSpecialist.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }
                // Selection logic works normally, nothing extra needed
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        /* <------ END OF SPECIALIST LOGIC -----------> */

        // Handle login button click
        creatAccountButton.setOnClickListener { if (validateInputs()) performRegistration() }

        // On click listener for login
        linkLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    // Validate Input Data
    private fun validateInputs(): Boolean {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val birthDate = birthDateEditText.text.toString().trim()
        val specialist = spinnerSpecialist.selectedItem.toString()

        if (name.isEmpty()) {
            nameEditText.error = "Name is required"
            return false
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email"
            return false
        }
        if (password.isEmpty() || password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            return false
        }
        if (phoneNumber.isEmpty() || !phoneNumber.matches(Regex("^[0-9]{10,15}$"))) {
            phoneNumberEditText.error = "Enter a valid phone number"
            return false
        }
        if (birthDate.isEmpty() || !birthDate.matches(Regex("^\\d{2}-\\d{2}-\\d{4}$"))) {
            birthDateEditText.error = "Enter birthdate in MM-DD-YYYY format"
            return false
        }
        if (specialist == "Select Specialist") { // Assuming first item is a placeholder
            Toast.makeText(this, "Please select a specialist", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // Perform the Registration
    private fun performRegistration() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val birthDate = birthDateEditText.text.toString().trim()
        val specialist = spinnerSpecialist.selectedItem.toString()

        // Create JSON request body
        val jsonBody = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("password", password)
            put("phone_number", phoneNumber)
            put("specialist", specialist)
            put("birth_date", birthDate)
        }

        val request = Request.Builder()
            .url(REGISTRATION_URL)
            .post(
                RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    jsonBody.toString()
                )
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@SignupActivity,
                        "Network Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    val responseBody = response.body?.string() // Store response body before parsing

                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonObject = JSONObject(responseBody)
                            val token = jsonObject.optString("token", "")

                            // Save token to SharedPreferences if it's not empty
                            if (token.isNotEmpty()) {
                                val sharedPreferences =
                                    getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putString("token", token).apply()
                            }

                            Toast.makeText(this@SignupActivity, "Registration successful!", Toast.LENGTH_SHORT).show()

                            // Delay for 1.5 seconds before navigating
                            birthDateEditText.postDelayed({
                                val intent = Intent(this@SignupActivity, PatientListActivity::class.java)
                                startActivity(intent)
                                finish()
                            }, 1500)

                        } catch (e: JSONException) {
                            // CURRENT WORKAROUND FOR ERROR, PLEASE CHANGE IT LATER

                            performRegistration()

//                            Toast.makeText(this@SignupActivity, "Invalid response format: ${e.message}", Toast.LENGTH_SHORT
//                            ).show()
                        }
                    } else {
                        // CURRENT WORKAROUND FOR ERROR, PLEASE CHANGE IT LATER

                        performRegistration()

//                        val errorBody = responseBody ?: ERROR_REGISTER_FAILED
//                        Toast.makeText(this@SignupActivity, "Error: $errorBody", Toast.LENGTH_SHORT)
//                            .show()
                    }
                }
            }
        })
    }


    // Placeholder logic
    private fun setPlaceholder(editText: TextInputEditText, placeholder: String) {
        // Initially set the placeholder
        editText.setText(placeholder)
        editText.setTextColor(
            resources.getColor(
                R.color.black,
                null
            )
        )

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (editText.text.toString() == placeholder) {
                    editText.setText("") // Remove placeholder when focused
                    editText.setTextColor(
                        resources.getColor(
                            R.color.black,
                            null
                        )
                    ) // Set normal text color
                }
            } else {
                if (editText.text.isNullOrEmpty()) {
                    editText.setText(placeholder) // Restore placeholder if empty
                    editText.setTextColor(
                        resources.getColor(
                            R.color.black,
                            null
                        )
                    ) // Set placeholder color
                }
            }
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != placeholder) {
                    editText.setTextColor(
                        resources.getColor(
                            R.color.black,
                            null
                        )
                    ) // Set normal text color
                }
            }
        })
    }

    // Password Placeholder Logic
    private fun setPasswordPlaceholder() {
        val placeholder = "Enter your password"
        passwordEditText.setText(placeholder)
        passwordEditText.inputType = EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD // Ensure text is visible initially

        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && passwordEditText.text.toString() == placeholder) {
                passwordEditText.setText("")
                passwordEditText.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
            } else if (!hasFocus && passwordEditText.text.isNullOrEmpty()) {
                passwordEditText.setText(placeholder)
                passwordEditText.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
        }

        // Handle the eye icon toggle for password visibility
        passwordInputLayout.setEndIconOnClickListener {
            val inputType = passwordEditText.inputType
            if (inputType == (EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)) {
                // If currently hidden, show the password
                passwordEditText.inputType = EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // If currently visible, hide the password
                passwordEditText.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
            }
            // Move cursor to the end after toggling
            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        }
    }

}
