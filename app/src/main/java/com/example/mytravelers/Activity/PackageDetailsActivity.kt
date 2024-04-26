package com.example.mytravelers.Activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.mytravelers.Adapter.ViewPagerAdapter
import com.example.mytravelers.LoaderGlobal
import com.example.mytravelers.R
import com.example.mytravelers.databinding.ActivityPackageDetailsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PackageDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityPackageDetailsBinding
    lateinit var Adapter : ViewPagerAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressDialog: Dialog
    private lateinit var viewPager: ViewPager





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackageDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
// Inside onCreate() or wherever you initialize the loader dialog
        progressDialog = LoaderGlobal.showProgressDialog(this@PackageDetailsActivity, "Loading...")

        initViews()
        getPackageDetails()
        // Retrieve user data from SharedPreferences
        sharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)

    }


    override fun onResume() {
        super.onResume()
        // Dismiss the loading dialog when the activity resumes
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
        // Check if the adapter is initialized before starting auto-slide
        if (::Adapter.isInitialized) {
            Adapter.startAutoSlide(viewPager) // Start auto-sliding
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Adapter.stopAutoSlide() // Stop auto-sliding
    }

    private fun initViews() {
        // Initialize viewPager
        viewPager = binding.vpPackageImages


        var place = intent.extras?.getString("place")
        var phone = intent.extras?.getString("mobile")
        val allImages = intent.getStringArrayExtra("allImages")
        var day = intent.extras?.getInt("day")
        var Price = intent.extras?.getString("Price")
        var description = intent.extras?.getString("description")

        with(binding){
            txtMobile.setOnClickListener {
                phoneCall()
            }
        }

        binding.fbBook.setOnClickListener {
            var i = Intent(this,BookNowActivity::class.java)
            i.putExtra("place",place)
            i.putExtra("Price",Price)

            startActivity(i)
        }




        binding.llMap.setOnClickListener {

            // Show loader dialog
            progressDialog.show()

            // Retrieve latitude and longitude from Firebase Realtime Database
            val databaseReference = FirebaseDatabase.getInstance().getReference("userPackageTb")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var foundLocation = false
                    for (packageSnapshot in dataSnapshot.children) {
                        val place = packageSnapshot.child("place").getValue(String::class.java)
                        val latitude = packageSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = packageSnapshot.child("longitude").getValue(Double::class.java)

                        if (place == intent.extras?.getString("place") && latitude != null && longitude != null) {
                            Log.d("PackageDetailsActivity", "Place: $place, Latitude: $latitude, Longitude: $longitude")

                            // Start MapsActivity with latitude and longitude
                            val intent = Intent(this@PackageDetailsActivity, MapsActivity::class.java).apply {
                                putExtra("latitude", latitude)
                                putExtra("longitude", longitude)
                                putExtra("hideSearchBar", true)
                                // Add other extras if needed
                            }
                            startActivity(intent)
                            foundLocation = true
                            break // Exit the loop after finding the latitude and longitude for the selected place
                        }
                    }

                    if (!foundLocation) {
                        // Handle case where latitude or longitude is not found for the selected place
                        Log.e("PackageDetailsActivity", "Latitude or longitude not found for the selected place")
                        progressDialog.dismiss()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("PackageDetailsActivity", "Database error: ${databaseError.message}")
                    progressDialog.dismiss()
                    // Handle database error
                }
            })
        }





    }

    private fun phoneCall() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dilouge_box, null)
        builder.setView(dialogView)

        val txtMessage = dialogView.findViewById<TextView>(R.id.txtMessage)
        txtMessage.text = "Do you want to proceed with the call?" // Set the message dynamically

        val dialog = builder.create()
        dialog.show()

        // Find views from the dialog layout
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)

        // Set click listeners for the buttons
        btnNo!!.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog if "No" is clicked
        }

        btnYes!!.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog first
            // Initiate the phone call
            val phoneNumber = binding.txtMobile.text.toString().substring(3) // Remove the leading "+91 "
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        }
    }




    private fun getPackageDetails() {

        with(binding){

            var place = intent.extras?.getString("place")
            var phone = intent.extras?.getString("mobile")
            val allImages = intent.getStringArrayExtra("allImages")
            var day = intent.extras?.getInt("day")
            var Price = intent.extras?.getString("Price")

            var description = intent.extras?.getString("description")



            binding.txtPlace.text = place
            binding.txtDescription.text = description
            binding.txtMobile.text = "+91 $phone"
            binding.txtPrice.text = Price
            binding.txtDays.text = "$day Day's"

            Log.e("TAG", "imageUrls: "+allImages)
            Adapter = ViewPagerAdapter(this@PackageDetailsActivity, allImages,viewPager)
            vpPackageImages.adapter = Adapter


            // Start auto-slide after ViewPager setup
            Adapter.startAutoSlide(vpPackageImages)

        }




    }
}