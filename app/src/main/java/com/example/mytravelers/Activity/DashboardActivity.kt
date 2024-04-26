package com.example.mytravelers.Activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.masterproject.RatingDialog
import com.example.mytravelers.Adapter.PackageAdapter
import com.example.mytravelers.LoaderGlobal
import com.example.mytravelers.Modal.PackageData
import com.example.mytravelers.R
import com.example.mytravelers.databinding.ActivityDashboardBinding

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView

class DashboardActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var googleSignInClient: GoogleSignInClient
    private var lastBackPressTime: Long = 0
    private lateinit var packageAdapter: PackageAdapter
    private lateinit var progressDialog: Dialog
    private var packageList = ArrayList<PackageData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Google Sign-In client
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up the navigation drawer
        setupNavigationView()

        // Update UI with user data
        updateUIWithUserData()

        // Set up FloatingActionButton click listener
        addPackage()

        // Load packages from Firebase
        loadPackagesFromFirebase()

        initViews()

        //firebase message notification
        firebaseMessageNotification()
    }

    private fun firebaseMessageNotification() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.e("TokenDetails", "Token Failed To Receive", it.exception)
                return@addOnCompleteListener
            }

          var token =   it.getResult()
            Log.d("TOKENN", token)
        }
    }

    private fun initViews() {
        with(binding){

            navView.getHeaderView(0).apply {
             var PrivacyPolicy =   findViewById<LinearLayout>(R.id.txtPrivacy)
                var Rate =    findViewById<LinearLayout>(R.id.txtRate)
              //  var Share =   findViewById<LinearLayout>(R.id.txtShare)


                PrivacyPolicy.setOnClickListener {
                    var intent = Intent(this@DashboardActivity,PrivacyPolicyActivity::class.java)
                    startActivity(intent)
                }
                Rate.setOnClickListener {
                    var ratingusdialog = RatingDialog(this@DashboardActivity)
                    ratingusdialog.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(android.R.color.transparent)))
                    ratingusdialog.setCancelable(false)
                    ratingusdialog.show()
                }



            }



        }
    }

    private fun loadPackagesFromFirebase() {
        // Show progress dialog while loading packages
        progressDialog = LoaderGlobal.showProgressDialog(this@DashboardActivity, "Loading Packages...")

        // Retrieve package data from Firebase
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("userPackageTb").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val packageList = ArrayList<PackageData>()
                for (data in snapshot.children) {
                    val packageData = data.getValue(PackageData::class.java)
                    packageData?.let { packageList.add(it) }
                }
                // Log package list size
                Log.d("DashboardActivity", "Package List Size: ${packageList.size}")
                // Display packages in RecyclerView
                displayPackages(packageList)
                // Dismiss progress dialog after loading
                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Log.e("DashboardActivity", "Failed to read value.", error.toException())
                // Dismiss progress dialog in case of error
                progressDialog.dismiss()
            }
        })
    }

    private fun displayPackages(packageList: List<PackageData>) {
        // Initialize RecyclerView and Adapter
        packageAdapter = PackageAdapter(packageList)
        binding.recyclerview.adapter = packageAdapter
        val layoutManager = LinearLayoutManager(this@DashboardActivity)
        binding.recyclerview.layoutManager = layoutManager
        // Notify the adapter that the dataset has changed
        packageAdapter.notifyDataSetChanged()
    }

    private fun addPackage() {
        with(binding){
            fabAdd.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, PackageActivity::class.java))

            }
        }
    }

    private fun updateUIWithUserData() {
        // Retrieve user data from SharedPreferences
        sharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)
        val profile = sharedPreferences.getString("profile", "")
        val username = sharedPreferences.getString("username", "")
        val email = sharedPreferences.getString("email", "")
        val userType = sharedPreferences.getString("userType", "")
        val phoneNumber = sharedPreferences.getString("phone", "")
        val address = sharedPreferences.getString("address", "")

        // Set user data to corresponding views in navigation drawer
        binding.navView.getHeaderView(0).apply {
            findViewById<TextView>(R.id.txtUsername).text = username
            findViewById<TextView>(R.id.txtEmail).text = email
            findViewById<TextView>(R.id.txtUserType).text = userType
          //  findViewById<TextView>(R.id.txtMobile).text = phoneNumber


            // Show/hide FloatingActionButton based on userType
            if (userType == "Admin") {
                binding.fabAdd.visibility = View.VISIBLE
            } else {
                binding.fabAdd.visibility = View.GONE
            }

            // Load user profile image using Glide
            Glide.with(this)
                .load(profile)
                .placeholder(R.drawable.travellogo)
                .into(findViewById<CircleImageView>(R.id.cvProfile))
        }
    }

    private fun setupNavigationView() {
        // Set up click listener for navigation drawer menu icon
        binding.imgMenu.setOnClickListener {
            // Open the navigation drawer
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Set up ActionBarDrawerToggle to handle opening and closing the navigation drawer
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    // Handle back button press to close the navigation drawer if it's open
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Implement double back press to exit functionality
            if (System.currentTimeMillis() - lastBackPressTime < 2000) {
                super.onBackPressed()
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
                lastBackPressTime = System.currentTimeMillis()
            }
        }
    }

    // Handle logout button click event
    private fun showConfirmationDialog(message: String, onConfirm: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.logout_item, null)
        builder.setView(dialogView)

        val txtMessage = dialogView.findViewById<TextView>(R.id.txtMessage)
        txtMessage.text = message

        val dialog = builder.create()
        dialog.show()

        val btnNo = dialog.findViewById<Button>(R.id.btnNo)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)

        btnNo!!.setOnClickListener {
            dialog.dismiss()
        }

        btnYes!!.setOnClickListener {
            dialog.dismiss()
            onConfirm.invoke()
        }
    }

    // Logout user from Google and clear user data from SharedPreferences
    fun logout(view: View) {
        val message = "Are you sure you want to logout?"
        showConfirmationDialog(message) {
            // Code to execute on confirmation
            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.signOut()
                    Toast.makeText(applicationContext, "Logout successful", Toast.LENGTH_SHORT).show()
                    clearUserData()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }


    // Clear user data from SharedPreferences
    private fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }
}
