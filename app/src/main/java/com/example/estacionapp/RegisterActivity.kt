package com.example.estacionapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException


class RegisterActivity : AppCompatActivity() {

    private lateinit var txtName:EditText
    private lateinit var txtEmail:EditText
    private lateinit var txtPassword:EditText
    private lateinit var progressBar:ProgressBar

    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        progressBar = findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()

    }

    fun register(view: View) {
        createNewUser()
    }

    private fun createNewUser(){
        val name:String = txtName.text.toString()
        val email:String = txtEmail.text.toString()
        val password:String = txtPassword.text.toString()

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email)
            && !TextUtils.isEmpty(password)){

            progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){
                        task ->
                    if (task.isSuccessful){
                        val user:FirebaseUser?=auth.currentUser
                        verifyEmail(user)

                        action()
                    }else{
                        try {
                            throw task.getException()!!
                        } catch (e: FirebaseAuthWeakPasswordException) {
                            //passwpord longitud minima 6 caracteres
                            Toast.makeText(this,e.message, Toast.LENGTH_LONG).show()
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this,e.message, Toast.LENGTH_LONG).show()
                        } catch (e: FirebaseAuthUserCollisionException) {
                            //email ya registrado
                            Toast.makeText(this,e.message, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(this,e.message, Toast.LENGTH_LONG).show()
                        }

                    }
                }
        }
    }

    private fun action(){
        startActivity(Intent(this, AuthActivity::class.java))
    }

    private fun verifyEmail(user:FirebaseUser?){
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this){
                    task ->
                if(task.isComplete){
                    Toast.makeText(this,"Email enviado", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this,"Error al enviar email", Toast.LENGTH_LONG).show()
                }
            }
    }
}