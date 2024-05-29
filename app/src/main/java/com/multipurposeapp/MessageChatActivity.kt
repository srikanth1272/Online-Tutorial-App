package com.multipurposeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.multipurposeapp.adapters.MessageAdapter
import com.multipurposeapp.data.Chat
import com.multipurposeapp.data.User
import com.multipurposeapp.databinding.ActivityMessageChatBinding
import com.multipurposeapp.notifications.Client
import com.multipurposeapp.notifications.Data
import com.multipurposeapp.notifications.MyResponse
import com.multipurposeapp.notifications.Sender
import com.multipurposeapp.notifications.Token
import com.multipurposeapp.utils.APIService
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MessageChatActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMessageChatBinding
    var firebaseDatabase: FirebaseDatabase? = null

    // creating a variable for our Database
    // Reference for Firebase.
    var databaseReference: DatabaseReference? = null
    var fuser: FirebaseUser? = null
    var apiService: APIService? = null
    var notify = false
    var messageAdapter: MessageAdapter? = null
    var mchat: List<Chat>? = null
    val TAG = "MessageChatActivity"
    var userid = ""
    var username = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fuser = FirebaseAuth.getInstance().currentUser
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService::class.java)

        // instance of our FIrebase database.
        firebaseDatabase = FirebaseDatabase.getInstance()
        val intent = intent
        userid = intent.getStringExtra("userid")!!
        username = intent.getStringExtra("username")!!
        binding?.toolbar?.title = username

        // below line is used to get reference for our database.
        databaseReference = firebaseDatabase?.getReference("MessageInfo")
        binding.messageRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        binding.messageRecyclerView.layoutManager = linearLayoutManager

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid)

        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
               // username.setText(user!!.username)
                /*if (user!!.imageURL.equals("default")) {
                   // profile_image.setImageResource(R.drawable.profile_img)
                } else {
                    //and this
                  //  Glide.with(applicationContext).load(user!!.imageURL).into(profile_image)
                }*/
                Log.d(TAG,"fuser?.uid!!${fuser?.uid!!}")
                readMesagges(fuser?.uid!!, userid, "")
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {}
        })

        binding.sendButton.setOnClickListener {
            notify = true
            val message = binding.enterMessage.text.toString()
            if (message.isEmpty()){

            }else{
                sendMessage(message)
                binding.enterMessage.setText("")
            }
        }

    }
    /*private fun createMeeting(token: String) {
        // we will make an API call to VideoSDK Server to get a roomId
        AndroidNetworking.post("https://api.videosdk.live/v2/rooms")
            .addHeaders("Authorization", token) //we will pass the token in the Headers
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        // response will contain `roomId`
                        val meetingId = response.getString("roomId")
                        Log.e("TAG","meetingId: $meetingId")
                        // starting the MeetingActivity with received roomId and our sampleToken
                        val intent = Intent(this@MessageChatActivity, VideoChatActivity::class.java)
                        intent.putExtra("token", sampleToken)
                        intent.putExtra("meetingId", meetingId)
                        startActivity(intent)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onError(anError: ANError) {
                    anError.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@MessageChatActivity, anError.message, Toast.LENGTH_SHORT).show()
                    }

                }
            })
    }*/

    private fun sendMessage(message: String) {
        val time = System.currentTimeMillis().toString()
        var reference = FirebaseDatabase.getInstance().reference

        val hashMap = java.util.HashMap<String, Any>()
        hashMap["sender"] = fuser!!.uid
        hashMap["receiver"] = userid//receiver
        hashMap["message"] = message
        hashMap["isseen"] = false
        hashMap["time"] = time

        reference.child("Chats").push().setValue(hashMap)

        // add user to chat fragment
        val chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
            .child(fuser!!.uid)
            .child(userid)

        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userid)
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {}
        })

        val chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
            .child(userid)
            .child(fuser!!.uid)
        chatRefReceiver.child("id").setValue(fuser!!.uid)

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser!!.uid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                val user: User? = dataSnapshot.getValue(User::class.java)
                if (notify) {
                    Log.e(TAG,"notify $notify")
                    sendNotification(user?.id!!, user?.username.toString(), message)
                }
                notify = false
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {}
        })
    }
    private fun status(status: String) {
        val reff = FirebaseDatabase.getInstance().getReference("Users").child(fuser?.uid.toString())
        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        reff?.updateChildren(hashMap)
    }
    private fun sendNotification(receiver: String, username: String, message: String) {
        val tokens = FirebaseDatabase.getInstance().getReference("Tokens")
        val query = tokens.orderByKey().equalTo(receiver)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                Log.e(TAG,"snapshot $dataSnapshot")
                for (snapshot in dataSnapshot.children) {
                    Log.e(TAG,"snapshot $snapshot")
                    val token: Token? = snapshot.getValue(Token::class.java)
                    val data = Data(
                        fuser!!.uid, R.drawable.profile_img, "$username: $message", "New Message",
                        userid
                    )
                    val sender = Sender(data, token?.token)
                    Log.e(TAG,sender.toString())
                    apiService?.sendNotification(sender)
                        ?.enqueue(object : Callback<MyResponse?> {
                            override fun onResponse(
                                call: Call<MyResponse?>?,
                                response: Response<MyResponse?>
                            ) {
                                Log.e(TAG,response.code().toString())
                                Log.e(TAG,response.body()?.success.toString())
                                if (response.code() == 200) {
                                    if (response.body()?.success !== 1) {
                                        //Toast.makeText(applicationContext, "Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse?>?, t: Throwable?) {
                                Log.e(TAG,t.toString())
                            }
                        })
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {
                Log.e(TAG,"databaseError $databaseError")
            }
        })
    }

    private fun readMesagges(myid: String, userid: String, imageurl: String) {
        mchat = ArrayList()
       val reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (mchat as ArrayList<Chat>).clear()
                for (snapshot in dataSnapshot.children) {
                    Log.d(TAG,"sn ${snapshot.getValue()}")
                    val chat: Chat? = snapshot.getValue(Chat::class.java)
                    if (chat?.receiver.equals(myid) && chat?.sender.equals(userid) ||
                        chat?.receiver.equals(userid) && chat?.sender.equals(myid)
                    ) {
                        chat?.let { (mchat as ArrayList<Chat>).add(it) }
                    }
                    Log.d(TAG,"mCHat${mchat}")
                    messageAdapter = MessageAdapter(this@MessageChatActivity, mchat,"")
                    binding.messageRecyclerView.adapter = messageAdapter
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {}
        })
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

}