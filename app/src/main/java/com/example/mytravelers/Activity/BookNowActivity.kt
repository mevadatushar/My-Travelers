package com.example.mytravelers.Activity

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.mytravelers.Modal.PackageData
import com.example.mytravelers.R
import com.example.mytravelers.databinding.ActivityBookNowBinding
import java.util.Calendar

class BookNowActivity : AppCompatActivity() {
    lateinit var binding: ActivityBookNowBinding
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookNowBinding.inflate(layoutInflater)
        setContentView(binding.root)

                initView()

    }

    private fun initView() {
        sharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)

        // Retrieve data from SharedPreferences
        val username = sharedPreferences.getString("username", "")
        val email = sharedPreferences.getString("email", "")
        val address = sharedPreferences.getString("address", "")
        val phone = sharedPreferences.getString("phone", "")
//        Log.e("TAG", "mobile: "+mobile)
        val userType = sharedPreferences.getString("userType", "")
//        val imageDownloadUrl = sharedPreferences.getString("imageDownloadUrl", "")

        var place = intent.getStringExtra("place")
        var Price = intent.getStringExtra("Price")

//        var addres = intent.getStringExtra("address")

        var profileList = PackageData()

        binding.edtFrom.setText(address)
        binding.edtTo.setText(place)
        binding.edtPhone.setText(phone)
        // Display retrieved data in appropriate views
        binding.edtUsername.setText(username)
        binding.edtEmail.setText(email)


        binding.btnSubmit.setOnClickListener {

            var date = binding.txtDate.text.toString()
            var person = binding.edtPerson.text.toString()

            if(binding.edtFrom.text!!.isEmpty())
            {
                binding.edtFrom.error = "Please enter your location"
                addontextchangelistner()
            }
            else if(binding.edtTo.text!!.isEmpty())
            {
                binding.edtTo.error = "Please enter your going location"
                addontextchangelistner()
            }
            else if(binding.edtUsername.text!!.isEmpty())
            {
                binding.edtUsername.error = "Please enter your Username"
                addontextchangelistner()
            }
            else if(binding.edtEmail.text!!.isEmpty())
            {
                binding.edtEmail.error = "Please enter your email"
                addontextchangelistner()
            }
            else if(binding.edtPhone.text!!.isEmpty())
            {
                binding.edtPhone.error = "Please enter your mobile number"
                addontextchangelistner()
            }
            else if(binding.txtDate.text!!.isEmpty())
            {
                binding.txtDate.error = "Please enter your date of going"
                addontextchangelistner()
            }
            else if(binding.edtPerson.text!!.isEmpty())
            {
                binding.edtPerson.error = "please enter total person"
                addontextchangelistner()
            }
            else
            {

                var i = Intent(this,PaymentActivity::class.java)
//              i.putExtra("address",address)
                 i.putExtra("place",place)
                i.putExtra("username",username)
                i.putExtra("email",email)
                i.putExtra("phone",phone)
                i.putExtra("Price",Price)
//              i.putExtra("date",date)
//              i.putExtra("person",person)
                startActivity(i)
            }
        }

        binding.imgDate.setOnClickListener {
            val c = Calendar.getInstance()

            // Initialize year, month, and day from the calendar instance
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                R.style.CustomDatePickerDialog, // Set custom style here
                { view, year, monthOfYear, dayOfMonth ->
                    // on below line we are setting
                    // date to our edit text.
                    val dat = (dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
                    binding.txtDate.setText(dat)
                },
                // Pass year, month, and day to the DatePickerDialog
                year,
                month,
                day
            )

            // Set minimum date to today's date
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

            // Set custom buttons text color
            datePickerDialog.setOnShowListener { dialog ->
                val positiveButton = (dialog as DatePickerDialog).getButton(DatePickerDialog.BUTTON_POSITIVE)
                val negativeButton = dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)

                positiveButton.setTextColor(ContextCompat.getColor(this, R.color.textColor))
                negativeButton.setTextColor(ContextCompat.getColor(this, R.color.textColor))
            }

            // Show the dialog
            datePickerDialog.show()
        }



    }

    fun addontextchangelistner() {
        binding.edtFrom.addTextChangedListener {
            binding.edtFrom.error = null
        }

        binding.edtTo.addTextChangedListener {
            binding.edtTo.error = null
        }

        binding.edtUsername.addTextChangedListener {
            binding.edtUsername.error = null
        }

        binding.edtEmail.addTextChangedListener {
            binding.edtEmail.error = null
        }

        binding.edtPhone.addTextChangedListener {
            binding.edtPhone.error = null
        }

        binding.txtDate.addTextChangedListener {
            binding.txtDate.error = null
        }

        binding.edtPerson.addTextChangedListener {
            binding.edtPerson.error = null
        }
    }
}