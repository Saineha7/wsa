package com.wsa.safetyapp

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        onLogin()
        onSignUp()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.CALL_PHONE
            ),
            0
        )
    }

    fun onLogin() {
        loginBtn.setOnClickListener() {
            if(loginEt.text.toString() == "" || passwordEt.text.toString() == "")
                Toast.makeText(this,"Field(s) should not be empty!",Toast.LENGTH_SHORT).show()
            else {
                val myIntent = Intent(this, HomePage::class.java)   // MainActivity
                myIntent.putExtra("key1", "login")
                this.startActivity(myIntent)
            }
        }
    }

    fun onSignUp() {
        signUpBtn.setOnClickListener() {
            val myIntent = Intent(this, Verificaton::class.java)   // MainActivity
            this.startActivity(myIntent)
        }
    }


}