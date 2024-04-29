package com.example.mytravelers.Activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mytravelers.Modal.UserDataModal
import com.example.mytravelers.R
import com.example.mytravelers.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var customLoaderDialog: AlertDialog
    private lateinit var auth: FirebaseAuth
    private var filePath: Uri? = null // Initialize filePath as nullable Uri
    private val PICK_IMAGE_REQUEST = 22
    // instance for firebase storage and StorageReference
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference
    var downloadUrl: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize Firebase components
        firebaseDatabase = FirebaseDatabase.getInstance()
        customLoaderDialog = createCustomLoaderDialog()
        auth = FirebaseAuth.getInstance()

        // Initialize FirebaseStorage instance
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference // Initialize storageReference

        // Initialize firebase user
        val firebaseUser = auth.currentUser

        // Initialize UI elements and setup listeners
        initView()
        setupRadioButtons()
        setupPasswordVisibilityToggle()
        setupPasswordTextWatcher()
        setupProfilePictureSelection()
    }

    // Initialize UI elements and setup click listener for sign up button
    private fun initView() {
        with(binding) {
            btnCreateAccount.setOnClickListener {
                val username = edtEnterUsername.text.toString()
                val mobile = edtEnterPhone.text.toString()
                val address = edtEnterAddress.text.toString()
                val email = edtEnterEmail.text.toString()
                val password = edtEnterPassword.text.toString()
                val userType = if (rbUser.isChecked) "User" else "Admin"
                val userTypeInt = if (userType == "User") 1 else 0 // Convert userType string to integer
                val profile = filePath.toString()

                // Validate input fields
                if (TextUtils.isEmpty(username)) {
                    edtEnterUsername.error = "Enter Username"
                    return@setOnClickListener
                }

                if (TextUtils.isEmpty(mobile)) {
                    edtEnterPhone.error = "Enter Your Number"
                    return@setOnClickListener
                }

                if (mobile.length != 10) {
                    edtEnterPhone.error = "Enter 10 digit Number"
                    return@setOnClickListener
                }

                if (TextUtils.isEmpty(address)) {
                    edtEnterAddress.error = "Enter Your Address"
                    return@setOnClickListener
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    edtEnterEmail.error = "Enter a valid email address"
                    return@setOnClickListener
                }

                if (password.length < 8) {
                    edtEnterPassword.error = "Password must be at least 8 characters long"
                    return@setOnClickListener
                }

                // Show custom loader dialog
                customLoaderDialog.show()

                // Create user account with email and password
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // If account creation is successful, store user data in Firebase Realtime Database
                            val key = firebaseDatabase.reference.child("userDataTB").push().key

                            val modal = key?.let {
                                UserDataModal(
                                    it,
                                    profile,
                                    username,
                                    mobile,
                                    address,
                                    email,
                                    password,
                                    userTypeInt
                                )
                            }

                            if (key != null && modal != null) {
                                firebaseDatabase.reference.child("userDataTB").child(username).setValue(modal)
                                    .addOnCompleteListener {
                                        customLoaderDialog.dismiss()
                                        if (it.isSuccessful) {
                                            // If data insertion is successful, show success message and clear input fields
                                            Toast.makeText(
                                                this@SignupActivity,
                                                "SignUP Successful",
                                                Toast.LENGTH_SHORT
                                            ).show()


                                            //uplode image to storage
                                            uploadImage()
                                            // If account creation is successful, store user data in SharedPreferences
                                            saveUserData()

                                            finish() // Finish the current activity to prevent going back to it using back button

                                            clearFields()

                                        } else {
                                            // If data insertion fails, show error message
                                            Toast.makeText(
                                                this@SignupActivity,
                                                "Failed to SignUP",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.e("TAG", "Failed to insert record: ", it.exception)
                                        }

                                    }
                            }
                        } else {
                            // If account creation fails, show error message
                            customLoaderDialog.dismiss()
                            Toast.makeText(
                                this@SignupActivity,
                                "Account creation failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(TAG, "Account creation failed: ${task.exception?.message}")
                        }
                    }
            }
        }
    }

    // Setup radio button colors
    private fun setupRadioButtons() {
        with(binding) {
            val buttonTint = ContextCompat.getColor(this@SignupActivity, R.color.radio_button_tint)
            rbUser.buttonTintList = ColorStateList.valueOf(buttonTint)
            rbAdmin.buttonTintList = ColorStateList.valueOf(buttonTint)
        }
    }

    // Setup listener for password visibility toggle checkbox
    private fun setupPasswordVisibilityToggle() {
        with(binding) {
            cbShowPass.setOnCheckedChangeListener { _, isChecked ->
                // Toggle password visibility
                edtEnterPassword.transformationMethod = if (isChecked) {
                    null // Show password
                } else {
                    PasswordTransformationMethod.getInstance() // Hide password
                }
            }
        }
    }

    // Setup text watcher for password input field to validate password length
    private fun setupPasswordTextWatcher() {
        binding.edtEnterPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Not used
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                if (password.length < 8) {
                    binding.edtEnterPassword.error = "Password must be at least 8 characters long"
                } else {
                    binding.edtEnterPassword.error = null
                }
            }
        })
    }

    // Setup listener for profile picture selection
    private fun setupProfilePictureSelection() {
        binding.cvUploadImage.setOnClickListener {
            selectImage()
        }
    }

    // Launch image selection intent
    private fun selectImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST)
        Log.d(TAG, "Select Image clicked")
    }

    // Clear input fields
    private fun clearFields() {
        with(binding) {
            edtEnterUsername.text.clear()
            edtEnterPhone.text.clear()
            edtEnterAddress.text.clear()
            edtEnterEmail.text.clear()
            edtEnterPassword.text.clear()
        }
    }

    // Save user data to SharedPreferences
    fun saveUserData() {
        var sharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)

        var username = binding.edtEnterUsername.text.toString()
        var mobile = binding.edtEnterPhone.text.toString()
        var address = binding.edtEnterAddress.text.toString()
        var email = binding.edtEnterEmail.text.toString()
        var password = binding.edtEnterPassword.text.toString()
        val userType = if (binding.rbUser.isChecked) "User" else "Admin"
        // Retrieve the profile image URI from filePath
        val profile = filePath.toString()

        var editor = sharedPreferences.edit()

        editor.putBoolean("isLogin", true)
        editor.putString("username", username)
        editor.putString("phone", mobile)
        editor.putString("address", address)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.putString("userType", userType)
        // Save the profile image URI as a string representation
        editor.putString("profile", profile)
        editor.apply()

        var intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }

    // Upload image to Firebase Storage
    private fun uploadImage() {
        if (filePath != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val filename = binding.edtEnterUsername.text.toString()
            val ref = storageReference.child("images/$filename")

            ref.putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Image Uploaded!!", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Uploading image")
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                }
        }
    }

    // Create custom loader dialog
    private fun createCustomLoaderDialog(): AlertDialog {
        val customLoaderView = LayoutInflater.from(this).inflate(R.layout.custom_loader_item, null)
        return AlertDialog.Builder(this)
            .setView(customLoaderView)
            .setCancelable(false)
            .create()
    }

    // Handle image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            Log.d(TAG, "File Path: $filePath")

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding.cvUploadImage.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val TAG = "com.example.mytravelers.Activity.SignupActivity"

    }
}
