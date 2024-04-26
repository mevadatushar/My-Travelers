package com.example.mytravelers.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mytravelers.R
import com.example.mytravelers.databinding.ActivityGoogleFacebookLoginAdditionalBinding

class GoogleFacebookLoginAdditionalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoogleFacebookLoginAdditionalBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleFacebookLoginAdditionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)

        initView()
        retriveData()
        setupRadioButtons()


    }

    private fun retriveData() {
        // Retrieve username from intent extras
        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")

        // Set username to EditText
        binding.edtEnterUsername.setText(username)
        binding.edtEnterEmail.setText(email)
    }

    private fun initView() {
        binding.btnCreateAccount.setOnClickListener {
            saveAndPassDataToDashboard()
        }
    }

    private fun saveAndPassDataToDashboard() {
        val username = binding.edtEnterUsername.text.toString()
        val email = binding.edtEnterEmail.text.toString()
        val phone = binding.edtEnterPhone.text.toString()
        val address = binding.edtEnterAddress.text.toString()

        val userType = if (binding.rbUser.isChecked) "User" else "Admin"

        // Save user data to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("phone", phone)
        editor.putString("address", address)
        editor.putString("userType", userType)
        editor.apply()

        // Start DashboardActivity
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }


    // Setup radio button colors
    private fun setupRadioButtons() {
        with(binding) {
            val buttonTint = ContextCompat.getColor(this@GoogleFacebookLoginAdditionalActivity, R.color.radio_button_tint)
            rbUser.buttonTintList = ColorStateList.valueOf(buttonTint)
            rbAdmin.buttonTintList = ColorStateList.valueOf(buttonTint)
        }
    }
}
