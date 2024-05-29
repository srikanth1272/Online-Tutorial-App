package com.multipurposeapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.multipurposeapp.Utils.toastme
import com.multipurposeapp.databinding.ActivityLoginBinding
import com.multipurposeapp.utils.Constants

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        //alreadyLogin()

        binding.btnLogin.setOnClickListener {
            val semail = binding.email.text.toString()
            val sPass = binding.password.text.toString()
            if (semail.isEmpty()||sPass.isEmpty()){
                toastme("fields cannot be empty!")
            } else if (sPass.length < 6) {
                toastme("password must be at least 6 characters")
            } else{
                if (Utils.isPasswordValid(sPass)) {
                    // Password is valid
                    println("Password is valid.")
                    loginToFirebase(semail,sPass)
                } else {
                    // Password is not valid
                    toastme("Password is not valid. it should contain one Capital letter and one Number in tha password!")
                }
            }
        }
        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }
        binding.btnAdminn.setOnClickListener {
            val semail = binding.email.text.toString()
            val sPass = binding.password.text.toString()
            if (semail.isEmpty()||sPass.isEmpty()){
                toastme("fields cannot be empty!")
            } else if (sPass.length < 6) {
                toastme("password must be at least 6 characters")
            } else{
                if (semail == "admin@app.com" && sPass == "12345678") {
                    loginToFirebaseAdmin(semail, sPass)
                }else{
                    toastme("Youre not admin!")
                }
            }
        }
        binding.btnInstructor.setOnClickListener {
            val semail = binding.email.text.toString()
            val sPass = binding.password.text.toString()
            if (semail.isEmpty()||sPass.isEmpty()){
                toastme("fields cannot be empty!")
            } else if (sPass.length < 6) {
                toastme("password must be at least 6 characters")
            } else{
                if (Utils.isPasswordValid(sPass)) {
                    // Password is valid
                    println("Password is valid.")
                    loginToInstructurer(semail, sPass)
                } else {
                    // Password is not valid
                    toastme("Password is not valid. it should contain one Capital letter and one Number in tha password!")
                }
            }
        }
        if (!areNotificationsEnabled()) {
            // Notifications are not enabled, prompt the user to enable them
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestNotificationPermission()
            }
        } else {
            // Notifications are enabled
            // Your app logic when notifications are enabled
        }

    }
    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = NotificationManagerCompat.from(this)
        return notificationManager.areNotificationsEnabled()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }

    private fun loginToInstructurer(semail: String, sPass: String) {
        auth.signInWithEmailAndPassword(semail,sPass).addOnCompleteListener {
            if (it.isSuccessful){
                Constants.isAdmin = false
                Constants.isInstructur = true
                gotoHome()
            }else{
                toastme(it.exception?.message.toString())
            }
        }
    }

    private fun loginToFirebaseAdmin(semail: String, sPass: String) {
        auth.signInWithEmailAndPassword(semail,sPass).addOnCompleteListener {
            if (it.isSuccessful){
                Constants.isAdmin = true
                Constants.isInstructur = false
                gotoHome()
            }else{
                toastme(it.exception?.message.toString())
            }
        }
    }

    private fun alreadyLogin() {
        if (auth.currentUser!=null){
            toastme("Already Login!")
            gotoHome()
        }
    }

    private fun loginToFirebase(semail: String, sPass: String) {
        auth.signInWithEmailAndPassword(semail,sPass).addOnCompleteListener {
            if (it.isSuccessful){
                Constants.isAdmin = false
                Constants.isInstructur = false
                gotoHome()
            }else{
                toastme(it.exception?.message.toString())
            }
        }
    }

    private fun gotoHome() {
        startActivity(Intent(this@LoginActivity,HomeActivity::class.java))
        finish()
    }
}