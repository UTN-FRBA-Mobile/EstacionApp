package com.example.estacionapp.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.estacionapp.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var txtEmail: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        txtEmail = findViewById(R.id.txtEmail)

        auth = FirebaseAuth.getInstance()
    }

    fun send(view: View) {
        val email = txtEmail.text.toString()

        if (!TextUtils.isEmpty(email)) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this){ task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.forget_password_success), Toast.LENGTH_LONG)
                            .show()
                        startActivity(Intent(this, AuthActivity::class.java))
                    } else {
                        Toast.makeText(this, getString(R.string.forget_password_error), Toast.LENGTH_LONG)
                            .show()
                    }
                }
        } else {
            Toast.makeText(this, getString(R.string.empty_email), Toast.LENGTH_LONG)
                .show()
        }
    }
}