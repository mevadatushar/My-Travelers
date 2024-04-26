package com.example.mytravelers.Activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mytravelers.R
import com.example.mytravelers.databinding.ActivityPaymentBinding
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

class PaymentActivity : AppCompatActivity(), PaymentResultListener {
    lateinit var binding: ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val profileImageUrl = getProfileImageUrl() ?: ""

        Checkout.preload(applicationContext)
        val co = Checkout()
        // apart from setting it in AndroidManifest.xml, keyId can also be set
        // programmatically during runtime
        co.setKeyID("rzp_test_qPmsYCdhpHwjIw")

        startPayment()
    }
    private fun startPayment() {


        // Retrieve data from Intent extras
        val name = intent.getStringExtra("name") ?: ""
        val phone = intent.getStringExtra("phone") ?: ""
        val price = intent.getStringExtra("Price") ?: ""

        // Extract numeric part from price string
        val numericPrice = price.replace("[^\\d.]".toRegex(), "")


        if (price.isNullOrEmpty()) {
            // Handle case where price is null or empty
            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
            return
        }

        Log.d("TAG", "priicce "+price)

        val profileImageUrl = getProfileImageUrl() ?: ""

        val designerFees: String = "10000.50"

        val amountInPaise: Int
        try {
            amountInPaise = (designerFees.toFloat() * 100).toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            // Handle the case where designerFees is not a valid float
            return
        }
//        val amountInPaise = try {
//            (numericPrice.toFloat() * 100).toInt()
//        } catch (e: NumberFormatException) {
//            e.printStackTrace()
//            Log.d("TAG", "start $e")
//            Toast.makeText(this, "Invalid price format: $e", Toast.LENGTH_SHORT).show()
//            finish() // Close the activity
//            return
//        }

//        val amountInPaise: Int
//        try {
//            amountInPaise = (price.toFloat() * 100).toInt()
//        } catch (e: NumberFormatException) {
//            e.printStackTrace()
//            // Handle the case where designerFees is not a valid float
//            return
//        }


        /*
        *  You need to pass the current activity to let Razorpay create CheckoutActivity
        * */


        val activity:Activity = this
        val co = Checkout()
        co.setKeyID("rzp_test_qPmsYCdhpHwjIw")
        try {
            val options = JSONObject()
            options.put("name",name)
            options.put("description","Demoing Charges")
            //You can omit the image option to fetch the image from the dashboard
            options.put("image",profileImageUrl)
            options.put("theme.color", "#EB44B72F");
            options.put("currency","INR");

            options.put("amount",amountInPaise)//pass amount in currency subunits

            val prefill = JSONObject()
            prefill.put("email","tushar.mevada45@gmail.com")
            prefill.put("contact","9727026233")
            options.put("prefill",prefill)


            co.open(activity,options)
        }catch (e: Exception){
            Toast.makeText(activity,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


        private fun getProfileImageUrl(){
            val sharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)
            sharedPreferences.getString("profile", "")

        }



    override fun onPaymentSuccess(p0: String?) {
        Toast.makeText(this, "Payment success", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Toast.makeText(this, "Error : ${p1}", Toast.LENGTH_SHORT).show()
        finish()
        Log.e("TAG", "onPaymentError: "+p0+p1)    }
}
