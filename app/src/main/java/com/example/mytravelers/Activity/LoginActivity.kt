package com.example.mytravelers.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mytravelers.Modal.UserDataModal
import com.example.mytravelers.R
import com.example.mytravelers.databinding.ActivityLoginBinding
import com.example.mytravelers.databinding.CustomLoaderItemBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import org.json.JSONException

class LoginActivity : AppCompatActivity() {

    lateinit var callbackManager: CallbackManager
    private lateinit var binding: ActivityLoginBinding
    private lateinit var customLoaderDialogBinding: CustomLoaderItemBinding
    private lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customLoaderDialogBinding = CustomLoaderItemBinding.inflate(LayoutInflater.from(this))

        // Initialize views and setup login methods
        initView()
        init_login()
       init_LoginWithGoogle()
       init_LoginWithFacebook()
    }

    // Initialize Google Sign-In
   private fun init_LoginWithGoogle() {
        with(binding) {
            // Configure Google sign-in
            val googleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("72118481531-dleecr8u0n0chhakk44havfq7ovpkvgl.apps.googleusercontent.com")
                    .requestEmail()
                    .build()

            // Initialize GoogleSignInClient
            googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, googleSignInOptions)
            // Set click listener for Google sign-in button
            llLoginGoogle.setOnClickListener {
                val intent: Intent = googleSignInClient.signInIntent
                startActivityForResult(intent, 100)

            }
        }
    }

    // Handle Google sign-in result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            val signInAccountTask: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            if (signInAccountTask.isSuccessful) {
                val s = "Google sign in successful"
                Toast.makeText(this@LoginActivity, "Google sign in successful", Toast.LENGTH_SHORT)
                    .show()

                try {
                    val googleSignInAccount = signInAccountTask.getResult(ApiException::class.java)

                    if (googleSignInAccount != null) {
                        val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                            googleSignInAccount.idToken, null
                        )

                        Log.d(
                            TAG,
                            "onActivityResult" + googleSignInAccount.email + " " + googleSignInAccount.displayName + " " + googleSignInAccount.photoUrl
                        )

                        auth.signInWithCredential(authCredential)
                            .addOnCompleteListener(this@LoginActivity) { task ->
                                if (task.isSuccessful) {
                                    val sharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString("email", googleSignInAccount.email)
                                    editor.putString("username", googleSignInAccount.displayName)
                                    editor.putString("profile", googleSignInAccount.photoUrl.toString())
                                    editor.apply()

                                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Authentication Failed: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Initialize Facebook Login
    private fun init_LoginWithFacebook() {
        with(binding) {
            auth = FirebaseAuth.getInstance()
            callbackManager = CallbackManager.Factory.create()

            llLoginFacebook.setOnClickListener {
                LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity, listOf("email", "public_profile"))
            }

            LoginManager.getInstance().registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$result")
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "facebook:onError", error)
                }
            })
        }
    }

    // Handle Facebook access token
   private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val email = user?.email
                    val username = user?.displayName

                    val request = GraphRequest.newMeRequest(token) { jsonObject, response ->
                        if (response!!.error != null) {
                            Log.e(TAG, "GraphRequest Error: ${response.error}")
                            Toast.makeText(this@LoginActivity, "Failed to fetch profile picture.", Toast.LENGTH_SHORT).show()
                        } else {
                            try {
                                val profilePictureUrl = jsonObject!!.getJSONObject("picture").getJSONObject("data").getString("url")

                                val sharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("email", email)
                                editor.putString("username", username)
                                editor.putString("profile", profilePictureUrl)
                                editor.apply()

                                startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                                finish()
                            } catch (e: JSONException) {
                                Log.e(TAG, "JSONException: ${e.message}")
                                Toast.makeText(this@LoginActivity, "Failed to fetch profile picture.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    val parameters = Bundle()
                    parameters.putString("fields", "picture.type(large)")
                    request.parameters = parameters
                    request.executeAsync()

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this@LoginActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Initialize login methods
    private fun init_login() {
        with(binding) {
            txtSignup.setOnClickListener {
                var intent = Intent(this@LoginActivity, SignupActivity::class.java)
                startActivity(intent)
            }

            cbShowPass.setOnCheckedChangeListener { _, isChecked ->
                // Toggle password visibility
                edtEnterPassword.transformationMethod = if (isChecked) {
                    null // Show password
                } else {
                    PasswordTransformationMethod.getInstance() // Hide password
                }

                // Move cursor to the end of the text
                edtEnterPassword.setSelection(edtEnterPassword.text.length)
            }
        }
    }

    // Save user data after successful login
    private fun saveUserData(username: String) {
        auth = Firebase.auth
        auth = FirebaseAuth.getInstance()
        var sharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = FirebaseDatabase.getInstance().reference
        val email = binding.edtEnterEmail.text.toString()
        val password = binding.edtEnterPassword.text.toString()

        firebaseDatabase.reference.root.child("userDataTB").child(username).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange triggered")

                if (snapshot.exists()) {
                    val userData = snapshot.getValue(UserDataModal::class.java)
                    if (userData != null) {
                        user = userData
                        val userTypeString = if (user.UserAdmin == 1) "User" else "Admin"

                        val sharedPreferencesEditor: SharedPreferences.Editor = sharedPreferences.edit()
                        sharedPreferencesEditor.putString("email", user.email)
                        sharedPreferencesEditor.putString("username", user.Username)
                        sharedPreferencesEditor.putString("profile", user.profile)
                        sharedPreferencesEditor.putString("phone", user.mobile)
                        sharedPreferencesEditor.putString("address", user.address)
                        sharedPreferencesEditor.putString("userType", userTypeString)
                        sharedPreferencesEditor.apply()

                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        Log.e("TAG", "User data is null")
                    }
                } else {
                    Log.e("TAG", "No data available at specified location")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "onCancelled: "+error.message)
            }
        })
    }

    // Initialize views and set up text change listener for password field
    private fun initView() {
        auth = Firebase.auth

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEnterEmail.text.toString()
            val password = binding.edtEnterPassword.text.toString()
            val username = binding.edtEnterUsername.text.toString()

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtEnterEmail.setError("Enter a valid email address")
                binding.edtEnterEmail.requestFocus()
                return@setOnClickListener
            } else if (password.length < 8) {
                binding.edtEnterPassword.setError("Password must be at least 8 characters long")
                binding.edtEnterPassword.requestFocus()
                return@setOnClickListener
            }   else if( username.isEmpty()){
                binding.edtEnterUsername.setError("Username is required")
                binding.edtEnterUsername.requestFocus()
                return@setOnClickListener
            }

            // Show custom loader dialog
            showCustomLoaderDialog()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Account created successfully
                        Log.d(TAG, "Account SignIn successfully")
                        Toast.makeText(
                            this@LoginActivity,
                            "SignIn Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        saveUserData(username)

                    } else {
                        // Account creation failed
                        Log.e(TAG, "Account SignIn failed: ${task.exception?.message}")
                        Toast.makeText(
                            this@LoginActivity,
                            "Account SignIn failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Dismiss the custom loader dialog after the action is completed
                    dismissCustomLoaderDialog()
                }
        }

        // Listen to text changes in password field
        binding.edtEnterPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.length < 8) {
                    binding.edtEnterPassword.setError("Password must be at least 8 characters long")
                } else {
                    binding.edtEnterPassword.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used
            }
        })
    }

    private var customLoaderDialog: AlertDialog? = null

    private fun showCustomLoaderDialog() {
        val customLoaderView = LayoutInflater.from(this).inflate(R.layout.custom_loader_item, null)
        customLoaderDialog = AlertDialog.Builder(this)
            .setView(customLoaderView)
            .setCancelable(false)
            .create()
        customLoaderDialog?.show()
    }

    private fun dismissCustomLoaderDialog() {
        customLoaderDialog?.dismiss()
    }

    companion object {
        private const val TAG = "LoginActivity"
        lateinit var user: UserDataModal
    }

}
