package com.example.estacionapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore




class AuthActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()

    private lateinit var txtUser: EditText
    private lateinit var txtPassword: EditText
    private lateinit var progressBar: ProgressBar


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        txtUser = findViewById(R.id.txtUser)
        txtPassword = findViewById(R.id.txtPassword)
        progressBar = findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()

        /**snip **/
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.package.ACTION_LOGOUT")
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("onReceive", "Logout in progress")
                //At this point you should start the login activity and finish this one
                finish()
            }
        }, intentFilter)
        //** snip **//

        //initialData() //Solo correr sino se tienen datos en Firestore.
    }

    fun forgotPassword(view: View) {
        startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }

    fun register(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    fun login(view: View) {
        loginUser()
    }

    private fun loginUser() {
        val user: String = txtUser.text.toString()
        val password: String = txtPassword.text.toString()
//        val user = "alvaro.arando@gmail.com"
//        val password = "123456"

        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(password)) {
            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(user, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        action()
                    } else {
                        Toast.makeText(this, "Error en la Autenticación", Toast.LENGTH_LONG).show()
                    }
                }

        }
    }

    private fun action() {
        startActivity(Intent(this, MapsActivity::class.java))
    }
}
/**

    fun initialData(){
    Falta hacer datos de prueba
}
**/