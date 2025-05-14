package com.example.xray_frontend.ui.signup

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.xray_frontend.ui.login.LoginActivity
import com.example.xray_frontend.R
import com.example.xray_frontend.data.models.AuthDoctorRequest
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.databinding.ActivitySignUpBinding
import com.example.xray_frontend.ui.casedashboard.CaseDashboardActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private val signUpViewModel: SignUpViewModel by viewModels {
        SignUpViewModel.Factory(ThunderscopeRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeViewModel()

        setViews()
        setListeners()

    }

    private fun observeViewModel() {
        signUpViewModel.apply {
            registrationResult.observe(this@SignUpActivity) { result ->
                if (result != null) {
                    Toast.makeText(this@SignUpActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignUpActivity, CaseDashboardActivity::class.java))
                    finishAffinity()
                }
            }

            isLoading.observe(this@SignUpActivity) {
                // observe is it loading or not like show/hide progressbar
            }

            errorMessage.observe(this@SignUpActivity) {
                if (!it.isNullOrEmpty()) {
                    Toast.makeText(this@SignUpActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setViews() {
        // Set Specialist Dropdown (Kotlin Version)
        val specialistOptions = resources.getStringArray(R.array.specialist_options)
        val specialistAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, specialistOptions)

        binding.spinnerSpecialist.adapter = specialistAdapter
    }

    private fun setListeners() {
        binding.btnCreateAccount.setOnClickListener {
            if (validateInputs()) {
                performRegistration()
            }
        }

        binding.linkLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val birthDate = binding.etBirthDate.text.toString().trim()
        val specialist = binding.spinnerSpecialist.selectedItem.toString()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return false
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email"
            return false
        }
        if (password.isEmpty() || password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return false
        }
        if (phoneNumber.isEmpty() || !phoneNumber.matches(Regex("^[0-9]{10,15}$"))) {
            binding.etPhoneNumber.error = "Enter a valid phone number"
            return false
        }
        if (birthDate.isEmpty() || !birthDate.matches(Regex("^\\d{2}-\\d{2}-\\d{4}$"))) {
            binding.etBirthDate.error = "Enter birthdate in MM-DD-YYYY format"
            return false
        }
        if (specialist == "Select Specialist") {
            Toast.makeText(this, "Please select a specialist", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun performRegistration() {
        // Create a Doctor object from the input fields
        val authDoctorRequest = AuthDoctorRequest(
            name = binding.etName.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            password = binding.etPassword.text.toString().trim(),
            phoneNumber = binding.etPhoneNumber.text.toString().trim(),
            specialist = binding.spinnerSpecialist.selectedItem.toString(),
            birthDate = binding.etBirthDate.text.toString().trim()
        )

        // Pass the Doctor object to the ViewModel for registration
        signUpViewModel.registerDoctor(authDoctorRequest)
    }
}
