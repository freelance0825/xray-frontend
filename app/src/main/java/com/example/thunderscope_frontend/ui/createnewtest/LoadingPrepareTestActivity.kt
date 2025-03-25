package com.example.thunderscope_frontend.ui.createnewtest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.thunderscope_frontend.R

class LoadingPrepareTestActivity : AppCompatActivity() {

    // UI Components
    private lateinit var backButton: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_prepare_test_activity)

        // Initialize Back Button
        backButton = findViewById(R.id.back_button)

        // Back button click listener
        backButton.setOnClickListener {
            val intent = Intent(this, CreateNewTestActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}