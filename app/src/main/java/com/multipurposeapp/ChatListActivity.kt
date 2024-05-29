package com.multipurposeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.multipurposeapp.adapters.OnItemClick
import com.multipurposeapp.adapters.UserAdapter
import com.multipurposeapp.data.Chatlist
import com.multipurposeapp.data.User
import com.multipurposeapp.databinding.ActivityChatListBinding
import com.multipurposeapp.notifications.Token

class ChatListActivity : AppCompatActivity() ,OnItemClick{
    private lateinit var binding:ActivityChatListBinding
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<User>? = null
    private var filteredUsers:List<User>? = null
    var fuser: FirebaseUser? = null
    var reference: DatabaseReference? = null

    private var usersList: List<Chatlist>? = null
    var onItemClick: OnItemClick? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onItemClick = this

        binding.recyclerViewChatList.setHasFixedSize(true)
        binding.recyclerViewChatList.layoutManager = LinearLayoutManager(applicationContext)
        val dividerItemDecoration =
            DividerItemDecoration(binding.recyclerViewChatList.context, DividerItemDecoration.VERTICAL)
        binding.recyclerViewChatList.addItemDecoration(dividerItemDecoration)

        fuser = FirebaseAuth.getInstance().currentUser

        usersList = ArrayList()
        binding.readChat.setOnClickListener { readUsers("Online") }
        binding.readUser.setOnClickListener { readUsers("Users") }
        readUsers("Users")

        FirebaseInstanceId.getInstance().token?.let { updateToken(it) }

        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@ChatListActivity,RegisterActivity::class.java))
            finish()
        }
    }
    override fun onResume() {
        super.onResume()
        status("online")
        //currentUser(userid)
    }
    override fun onPause() {
        super.onPause()
        //reference.removeEventListener(seenListener)
        status("offline")
        //currentUser("none")
    }

    private fun readChat() {
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser!!.uid)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (usersList as ArrayList<Chatlist>).clear()
                /*for (snapshot in dataSnapshot.children) {
                    val chatlist = snapshot.getValue(Chatlist::class.java)
                    (usersList as ArrayList<Chatlist>).add(chatlist!!)
                }*/
                /*if (usersList.size == 0) {
                    frameLayout.setVisibility(View.VISIBLE)
                } else {
                    frameLayout.setVisibility(View.GONE)
                }*/
                chatList()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun readUsers(s: String) {
        mUsers = ArrayList()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Users")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (binding.chat.text.toString() == "") {
                    (mUsers as ArrayList<User>).clear()
                    for (snapshot in dataSnapshot.children) {
                        if (s=="Users") {
                            val user = snapshot.getValue(User::class.java)
                            user?.let { (mUsers as ArrayList<User>).add(it) }
                        }else{
                            val user = snapshot.getValue(User::class.java)
                            user?.let { (mUsers as ArrayList<User>).add(it) }
                            filteredUsers = mUsers?.filter { user -> user.status == "online" }
                            Log.e("TAG","filteredUsers: $filteredUsers")
                            //user?.let { (mUsers?.filter { OnlineUsers -> user.status == "Online" } as ArrayList<User>).add(OnlineUsers) }
                        }
                    }
                    if (s=="Users") {
                        userAdapter = UserAdapter(
                            applicationContext,
                            onItemClick,
                            mUsers,
                            false
                        )
                        binding.recyclerViewChatList.adapter = userAdapter
                    }else{
                        userAdapter = UserAdapter(
                            applicationContext,
                            onItemClick,
                            filteredUsers,
                            false
                        )
                        binding.recyclerViewChatList.adapter = userAdapter
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
    private fun status(status: String) {
        val reff = FirebaseDatabase.getInstance().getReference("Users").child(fuser?.uid.toString())
        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        reff?.updateChildren(hashMap)
    }

    private fun updateToken(token: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token1 = Token(token)
        reference.child(fuser!!.uid).setValue(token1)
    }

    private fun chatList() {
        mUsers = ArrayList()
        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("TAG","dataSnapshot ${dataSnapshot}")
                (mUsers as ArrayList<User>).clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    for (chatlist in usersList!!) {
                        Log.d("TAG","chatlist ${chatlist.id}")
                        user?.let { (mUsers as ArrayList<User>).add(it) }
                        /*if (user != null && user.id != null && chatlist != null && chatlist.getId() != null &&
                            user.id.equals(chatlist.getId())) {
                            (mUsers as ArrayList<User>).add(user)
                        }*/
                    }
                }
                userAdapter = UserAdapter(
                    applicationContext,
                    onItemClick,
                    mUsers,
                    true
                )
                binding.recyclerViewChatList.adapter = userAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onItemCLick(uid: String?, view: View?, username: String) {
        Log.d("TAG","UID $uid")
        val intent = Intent(this, MessageChatActivity::class.java)
        intent.putExtra("userid", uid)
        intent.putExtra("username",username)
        startActivity(intent)
    }
}