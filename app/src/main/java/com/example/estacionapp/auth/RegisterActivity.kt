package com.example.estacionapp.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.estacionapp.R
import com.google.firebase.auth.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        progressBar = findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()
    }

    fun register(view: View) = createNewUser()

    private fun createNewUser() {
        val email = txtEmail.text.toString()
        val password = txtPassword.text.toString()

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(password)) {
            progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser
                        verifyEmail(user)
                        action()
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: Exception) {
                            when (e) {
                                is FirebaseAuthWeakPasswordException,
                                is FirebaseAuthInvalidCredentialsException,
                                is FirebaseAuthUserCollisionException -> {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                                }
                                else -> throw e
                            }
                        }
                    }
                }
        } else {
            Toast.makeText(this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
        }
    }

    private fun action() = startActivity(Intent(this, AuthActivity::class.java))

    private fun verifyEmail(user:FirebaseUser?) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this){ task ->
                if (task.isComplete) {
                    Toast.makeText(this, getString(R.string.signup_success), Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(this, getString(R.string.signup_error), Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}