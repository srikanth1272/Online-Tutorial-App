package com.multipurposeapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.multipurposeapp.Utils.isPasswordValid
import com.multipurposeapp.Utils.toastme
import com.multipurposeapp.databinding.ActivityRegisterBinding
import com.multipurposeapp.utils.Utils
import java.util.Locale


class RegisterActivity : AppCompatActivity() {
    private lateinit var txtEmailRegister:EditText
    private lateinit var txtPasswordRegister:EditText
    private lateinit var txtUsernameRegister:EditText

    private lateinit var binding:ActivityRegisterBinding

    private lateinit var auth: FirebaseAuth
    private var mFirebaseDatabase: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    private val TAG = RegisterActivity::class.java.toString()
    var dialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_register)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        myRef = mFirebaseDatabase?.reference

        txtEmailRegister = binding.email
        txtPasswordRegister = binding.password
        txtUsernameRegister = binding.username

        binding.btnRegister.setOnClickListener {
            var stxtEmailRegister = txtEmailRegister.text.toString()
            var stxtPasswordRegister = txtPasswordRegister.text.toString()
            val susername = binding.username.text.toString()
            Log.d(TAG,"stxtEmailRegister: $stxtEmailRegister")
            Log.d(TAG,"stxtPasswordRegister: $stxtPasswordRegister")
            if (stxtEmailRegister.isEmpty()||stxtPasswordRegister.isEmpty()||susername.isEmpty()){
                toastme("fields cannot be empty!")
            } else if (stxtPasswordRegister.length < 6) {
                toastme("password must be at least 6 characters")
            }else if (stxtPasswordRegister.length > 16) {
                toastme("password must be below 16 characters")
            }  else{
                if (isPasswordValid(stxtPasswordRegister)) {
                    // Password is valid
                    println("Password is valid.")
                    registerToFirebase(stxtEmailRegister,stxtPasswordRegister,susername)
                } else {
                    // Password is not valid
                    toastme("Password is not valid. It should contain one Capital letter and one Number in tha password!")
                }
            }
        }
        binding.btnLogin.setOnClickListener {
            gotoLogin()
        }
    }

    private fun registerToFirebase(email: String, password: String,username:String) {
        Log.d(TAG,"registerToFirebase")
        dialog = Utils.showLoader(this@RegisterActivity);
        auth?.createUserWithEmailAndPassword(email,password)?.addOnCompleteListener {
            if (it.isSuccessful){
                Log.d(TAG,"success")
                registrationSuccess(username)
                /*val user = auth?.currentUser
                val userID = user?.uid
                val emails = user?.email
                val newUserProData = UserProData(
                    "", "",
                    username, "", "Please update your address"
                )
                if (userID != null) {
                    myRef?.child("users")?.child(userID)?.setValue(newUserProData)
                    myRef?.child("users")?.child(userID)?.child("email")?.setValue(emails)
                }*/
                // ========================================================================== on test


            }else{
                Log.d(TAG,"fail")
                if (dialog != null) {
                    dialog!!.dismiss()
                }
                toastme(it.exception?.message.toString())
            }
        }
    }

    private fun registrationSuccess(username: String) {
        val firebaseUser = auth.currentUser!!
        Log.d(TAG,firebaseUser.toString())
        val userid = firebaseUser!!.uid
        Log.d(TAG,userid.toString())
        myRef = FirebaseDatabase.getInstance().getReference("Users").child(userid)

        val hashMap = HashMap<String, String>()
        hashMap["id"] = userid
        hashMap["username"] = username
        hashMap["imageURL"] = "default"
        hashMap["status"] = "offline"
        hashMap["bio"] = ""
        hashMap["search"] = username.lowercase(Locale.getDefault())
        hashMap["paymentStatus"] = "false"
        if (dialog != null) {
            dialog!!.dismiss()
        }
        myRef?.setValue(hashMap)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toastme("Registration successful")
                    gotoLogin()
                }else{
                    Log.d(TAG,"error")
                }
            }
    }

    private fun gotoLogin() {
        val intent = Intent(
            this@RegisterActivity,
            LoginActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}