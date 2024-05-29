package com.multipurposeapp

import android.app.Activity
import android.widget.Toast
import java.util.regex.Pattern

object Utils {
    fun Activity.toastme(message:String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_LONG).show()
    }
    fun isPasswordValid(password: String): Boolean {
        // Define patterns for at least one uppercase letter and one digit
        val uppercasePattern = Pattern.compile("[A-Z]")
        val digitPattern = Pattern.compile("[0-9]")

        // Check if the password contains at least one uppercase letter
        val hasUppercase = uppercasePattern.matcher(password).find()

        // Check if the password contains at least one digit
        val hasDigit = digitPattern.matcher(password).find()

        // Return true if both conditions are satisfied
        return hasUppercase && hasDigit
    }

}