package com.multipurposeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.multipurposeapp.Utils.toastme
import com.multipurposeapp.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_forgot_password)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        resetPassword()

    }

    private fun resetPassword() {
        binding.btnReset.setOnClickListener {
            val sEmail = binding.sendEmail.text.toString()
            if (sEmail.isEmpty()){
                toastme("All fields are required!")
            }else{
                auth.sendPasswordResetEmail(sEmail).addOnCompleteListener{
                    if (it.isSuccessful){
                        toastme("Please check you Email")
                        gotoLogin()
                    }else{
                        toastme(it.exception?.message.toString())
                    }
                }
            }
        }
    }

    private fun gotoLogin() {
        startActivity(
            Intent(
                this@ForgotPasswordActivity,
                LoginActivity::class.java)
        )
    }
}

