package com.example.mytravelers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mytravelers.Activity.DashboardActivity
import com.example.mytravelers.Activity.LoginActivity
import com.example.mytravelers.Activity.SignupActivity
import com.example.mytravelers.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()

        var versionNamee =   findViewById<TextView>(R.id.txtVersionNumber)
        var versionCodee =   findViewById<TextView>(R.id.txtVersionCode)
        val packageInfo = packageManager.getPackageInfo(packageName, 0)


        val versionName = packageInfo.versionName
        val versionCode = packageInfo.versionCode

        // Display the version name and version code in TextViews
        versionNamee.text = "Version Name: $versionName"
        versionCodee.text = "Version Code: $versionCode"




    }


    private fun initView() {
        auth = Firebase.auth
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        // Check if the user is already logged in using shared preferences
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        // Check if the user is already logged in
        val currentUser = auth.currentUser

        // Show the splash screen for a short duration
        val splashDurationMillis = 2000L // 2 seconds
        Handler().postDelayed({
            if (isLoggedIn || currentUser != null) {
                // User is already logged in, navigate to com.example.mytravelers.Activity.DashboardActivity
                startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
            } else {
                // User is not logged in, navigate to LoginActivity
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
            finish() // Finish MainActivity so it's removed from the back stack
        }, splashDurationMillis)
    }



}