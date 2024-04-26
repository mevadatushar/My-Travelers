package com.example.mytravelers.Activity

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mytravelers.R
import com.example.mytravelers.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {
    lateinit var binding: ActivityPrivacyPolicyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
                initView()
    }

    private fun initView() {
        with(binding){

            webView.loadUrl("https://www.termsfeed.com/live/b1a4832b-3591-4e6f-909d-7e3500bbc7d9")
            // this will enable the javascript.
            webView.settings.javaScriptEnabled = true

            // WebViewClient allows you to handle
            // onPageFinished and override Url loading.
            webView.webViewClient = WebViewClient()

        }
    }
}