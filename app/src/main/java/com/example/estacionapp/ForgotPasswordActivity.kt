package com.example.estacionapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
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

    fun send(view: View){
        val email:String = txtEmail.text.toString()

        if(!TextUtils.isEmpty(email)){
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this){
                        task ->
                    if (task.isSuccessful){
                        startActivity(Intent(this, AuthActivity::class.java))
                    }else{
                        Toast.makeText(this,"Error al enviar email", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}