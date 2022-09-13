package com.example.socialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.socialapp.daos.UserDao
import com.example.socialapp.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient

import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.annotation.NonNull
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.common.api.GoogleApiClient







class SignUpActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private val RC_SIGN_IN: Int=123
    private lateinit var googleSignInClient:GoogleSignInClient
    private lateinit var mGoogleApiClient:GoogleApiClient
    private val TAG="SignInActivity tag"
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
        auth= Firebase.auth
        signInButton.setOnClickListener{
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    private fun signIn() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }



    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential=GoogleAuthProvider.getCredential(idToken,null)

        signInButton.visibility=View.GONE
        progressBar.visibility=View.VISIBLE

        GlobalScope.launch(Dispatchers.IO){
            val auth=auth.signInWithCredential(credential).await()
            val firebaseUser=auth.user
            withContext(Dispatchers.Main){
                updateUI(firebaseUser)
            }
        }

    }


    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser!=null){
            val user=User(firebaseUser.uid,firebaseUser.displayName,firebaseUser.photoUrl.toString())
            val userDao=UserDao()
            userDao.addUser(user)

            val mainActivity=Intent(this,MainActivity::class.java)
            startActivity(mainActivity)
            finish()
        }else{
            signInButton.visibility=View.VISIBLE
            progressBar.visibility=View.GONE

        }

    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }
}