package com.example.socialapp


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialapp.daos.PostDao
import com.example.socialapp.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import androidx.annotation.NonNull
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity(), IPostAdapter {
    private lateinit var postDao: PostDao
    private lateinit var adapter: PostAdapter
    private lateinit var mgoogleSignInClient:GoogleSignInClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mgoogleSignInClient= GoogleSignIn.getClient(this,gso)
        fab.setOnClickListener {
            val intent=Intent(this,CreatePostActivity::class.java)
            startActivity(intent)
        }
        setUpRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
       val inflater=menuInflater
        inflater.inflate(R.menu.menu_items,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.log_out-> {
            Firebase.auth.signOut()
            val intent=Intent(this,SignUpActivity::class.java)
            startActivity(intent)
            true
        }




        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }




    private fun setUpRecyclerView() {
            postDao=PostDao()
            val postsCollection=postDao.postCollections
            val query=postsCollection.orderBy("createdAt",Query.Direction.DESCENDING)
            val recyclerViewOptions=FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()
            adapter= PostAdapter(recyclerViewOptions,this)
        recyclerView.adapter=adapter
        recyclerView.layoutManager=LinearLayoutManager(this)

    }


    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
            postDao.updateLikes(postId)
    }
}