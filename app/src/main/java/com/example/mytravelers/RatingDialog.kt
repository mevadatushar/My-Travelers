package com.example.masterproject

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.mytravelers.R

class RatingDialog(context: Context) : Dialog(context) {
    private var userrate = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_rating)

        initview()
    }

    private fun initview() {
        val btnRateNow = findViewById<AppCompatButton>(R.id.btnRateNow)
        val btnLater = findViewById<AppCompatButton>(R.id.btnLater)
        val rtgbrRating = findViewById<RatingBar>(R.id.rtgbrRating)
        val imgRatingImage = findViewById<ImageView>(R.id.imgRatingImage)

        btnRateNow?.setOnClickListener {
            val RatingValue = rtgbrRating?.rating
            Toast.makeText(context, "Thanks for rating $RatingValue", Toast.LENGTH_SHORT).show()
            dismiss()


        }

        btnLater?.setOnClickListener {
            dismiss()
        }

        rtgbrRating?.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->

            when {
                rating <= 1 -> imgRatingImage?.setImageResource(R.drawable.angry1)
                rating <= 2 -> imgRatingImage?.setImageResource(R.drawable.angry)
                rating <= 3 -> imgRatingImage?.setImageResource(R.drawable.neutral)
                rating <= 4 -> imgRatingImage?.setImageResource(R.drawable.happy)
                rating <= 5 -> imgRatingImage?.setImageResource(R.drawable.emoji)
            }

            animationImage(imgRatingImage)

            userrate = rating
        }
    }

    private fun animationImage(imgRatingImage: ImageView?) {
        imgRatingImage?.let { imageView ->
            // Your animation logic here
            val scaleAnimation = ScaleAnimation(
                0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            scaleAnimation.fillAfter = true
            scaleAnimation.duration = 200
            imageView.startAnimation(scaleAnimation)
        }
    }

}
