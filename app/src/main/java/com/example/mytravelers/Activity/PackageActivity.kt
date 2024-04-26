package com.example.mytravelers.Activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.mytravelers.LoaderGlobal
import com.example.mytravelers.Modal.PackageData
import com.example.mytravelers.databinding.ActivityPackgeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PackageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPackgeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private val PICK_IMAGE_MULTIPLE = 1
    private val mArrayUri = ArrayList<Uri>()
    private var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackgeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        // Set up image selection functionality
        imageSelection()

        initview()
    }

    private fun initview() {
        with(binding){
            llLocation.setOnClickListener {
                val intent = Intent(this@PackageActivity, MapsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun imageSelection() {
        // Set up ViewSwitcher for image display
        binding.imgSwitcher.setFactory(ViewSwitcher.ViewFactory {
            ImageView(applicationContext)
        })

        // Set up click listeners for navigation buttons
        binding.right.setOnClickListener {
            if (position < mArrayUri.size - 1) {
                position++
                binding.imgSwitcher.showNext()
            } else {
                Toast.makeText(
                    this@PackageActivity,
                    "Last Image Already Shown",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.left.setOnClickListener {
            if (position > 0) {
                position--
                binding.imgSwitcher.showPrevious()
            } else {
                Toast.makeText(
                    this@PackageActivity,
                    "First Image Already Shown",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Set up click listener for image selection button
        binding.txtUploadImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_MULTIPLE
            )
        }

        // Set up click listener for submit button
        binding.btnSubmit.setOnClickListener {
            // Check if required fields are filled
            val place = binding.edtEnterPlace.text.toString()
            val priceText = binding.edtEnterPrice.text.toString()
            val daysText = binding.edtEnterDays.text.toString()
            val mobile = binding.edtEnterMobile.text.toString()
            if (place.isEmpty() || priceText.isEmpty() || daysText.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(
                    this@PackageActivity,
                    "Please fill in all required fields (Place, Price, Days , Mobile)",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (mArrayUri.isEmpty()) {
                Toast.makeText(
                    this@PackageActivity,
                    "Please select at least one image",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // If fields are filled, save data to Firebase
            saveDataToFirebase()
        }
    }


    private fun saveDataToFirebase() {

        // Receive latitude and longitude from com.example.mytravelers.Activity.com.example.mytravelers.Activity.MapsActivity
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        // Initialize variables with form data
        val images = mutableListOf<String>()
        val place = binding.edtEnterPlace.text.toString()
        val price = binding.edtEnterPrice.text.toString().toInt()
        val days = binding.edtEnterDays.text.toString().toInt()
        val mobile = binding.edtEnterMobile.text.toString()
        val description = binding.edtEnterDescription.text.toString()
        val userId = auth.currentUser?.uid

        userId?.let {
            // Generate unique package ID
            val packageId = databaseReference.child("userPackageTb").push().key ?: ""
            val packageRef = databaseReference.child("userPackageTb").child(place)
            val progressDialog = LoaderGlobal.showProgressDialog(this@PackageActivity, "Uploading...")

            // Upload images to Firebase Storage and get their URLs
            mArrayUri.forEachIndexed { index, uri ->
                val imageRef = storageReference.child("images").child(place).child("image_$index")
                imageRef.putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            images.add(imageUrl.toString())

                            // Check if all images have been uploaded
                            if (images.size == mArrayUri.size) {
                                // Save package data to Firebase Realtime Database
                                val packageData = PackageData(
                                    packageId,
                                    images,
                                    place,
                                    price,
                                    days,
                                    mobile,
                                    description,
                                    latitude,
                                    longitude

                                )

                                Log.d("TAG", "saveDataToFirebase: "+packageId+" "+images+" "+place+" "+price+" "+days+" "+mobile+" "+description+" "+latitude+" "+longitude)

                                packageRef.setValue(packageData)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this@PackageActivity,
                                            "Package Added successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // Clear form and navigate back to dashboard
                                        clearForm()
                                        progressDialog.dismiss()
                                        startActivity(Intent(this@PackageActivity, DashboardActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this@PackageActivity,
                                            "Failed to save package  ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        progressDialog.dismiss()
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@PackageActivity,
                            "Failed to upload image: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialog.dismiss()
                    }
            }
        }


    }

    private fun clearForm() {
        // Clear form fields and image list
        binding.edtEnterPlace.text.clear()
        binding.edtEnterDays.text.clear()
        binding.edtEnterPrice.text.clear()
        binding.edtEnterMobile.text.clear()
        binding.edtEnterDescription.text.clear()
        mArrayUri.clear()
        binding.imgSwitcher.setImageURI(null)
        position = 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Handle result of image selection
        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == Activity.RESULT_OK && data != null) {
            if (data.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    mArrayUri.add(imageUri)
                }
            } else {
                val imageUri = data.data
                if (imageUri != null) {
                    mArrayUri.add(imageUri)
                }
            }

            // Update visibility of navigation buttons based on number of images
            if (mArrayUri.size > 1) {
                binding.left.isVisible = true
                binding.right.isVisible = true
            } else {
                binding.left.isVisible = false
                binding.right.isVisible = false
            }

            // Reset position to first image
            position = 0
            // Display the first image in the ViewSwitcher
            binding.imgSwitcher.setImageURI(mArrayUri[position])
        } else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show()
        }
    }

}
