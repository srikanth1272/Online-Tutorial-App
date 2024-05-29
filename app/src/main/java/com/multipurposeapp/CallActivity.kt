package com.multipurposeapp

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.multipurposeapp.databinding.ActivityCallBinding
import kotlin.random.Random


class CallActivity : AppCompatActivity() {

    private lateinit var binding:ActivityCallBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val userName = intent.getStringExtra("userid")!!
        Log.d("CallActivity",userName)
        addCallFragment(userName)
    }

    private fun addCallFragment(username:String) {
        // Create a random number generator
        val random = Random

        // Generate a random integer between 0 (inclusive) and 10 (exclusive)
        val randomNumber = random.nextInt(10)
        val appID: Long = 1947381968
        val appSign: String = "98c7d3cd878dbd226587eb0024c3c3122ecc1621de3c2b733efa226b8fd52eaf"
        val callID: String = "test_call_id"
        val userID: String = Build.MANUFACTURER+"_"+randomNumber
        val userName: String = "user_$username"
    }
}