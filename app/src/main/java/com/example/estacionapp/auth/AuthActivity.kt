package com.example.estacionapp.auth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.estacionapp.MapsActivity
import com.example.estacionapp.R
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

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

    fun forgotPassword(view: View) = startActivity(Intent(this, ForgotPasswordActivity::class.java))

    fun register(view: View) = startActivity(Intent(this, RegisterActivity::class.java))

    fun login(view: View) = loginUser()

    private fun loginUser() {
        val user: String = txtUser.text.toString()
        val password: String = txtPassword.text.toString()

        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(password)) {
            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(user, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        action()
                    } else {
                        Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.INVISIBLE
                    }
                }
        } else {
            Toast.makeText(this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
        }
    }

    private fun action() = startActivity(Intent(this, MapsActivity::class.java))
}
/**

    fun initialData(){
    Falta hacer datos de prueba
}
**/