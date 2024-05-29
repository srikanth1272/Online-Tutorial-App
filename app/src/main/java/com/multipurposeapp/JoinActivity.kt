package com.multipurposeapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.multipurposeapp.Utils.toastme
import com.multipurposeapp.data.MeetingIDModel
import com.multipurposeapp.utils.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class JoinActivity : AppCompatActivity() {
    val sampleToken2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGlrZXkiOiI2OWRjOGMyYS00MjJjLTRlY2MtYTQyZC0wYWQ3MzBmZjNhNWIiLCJwZXJtaXNzaW9ucyI6WyJhbGxvd19qb2luIl0sImlhdCI6MTcwNzQ3NzU5NiwiZXhwIjoxNzEwMDY5NTk2fQ.yS5-HRtlm-n1s5UUKg6JDckBgP63t1DhfSrUmNne4QY"
val sampleToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGlrZXkiOiIzNjVjZjk1OS0zZTAwLTQyM2EtYjdkOS05ZjM2ZjBiZjRiYWMiLCJwZXJtaXNzaW9ucyI6WyJhbGxvd19qb2luIl0sImlhdCI6MTcxMDA4NjIxNCwiZXhwIjoxNzE3ODYyMjE0fQ.JPw_bz9N1cxrZDAQs5VUkdCJeMnKC7lhL3hmRZt3bHo"

    private lateinit var auth: FirebaseAuth
    private var mFirebaseDatabase: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    var etMeetingId:EditText?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        auth = Firebase.auth
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        myRef = mFirebaseDatabase?.reference

        val btnCreate = findViewById<Button>(R.id.btnCreateMeeting)
        val btnJoin = findViewById<Button>(R.id.btnJoinMeeting)
        etMeetingId = findViewById<EditText>(R.id.etMeetingId)
        checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID)
        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)
       // checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)
        if (Constants.isInstructur){
            btnCreate.visibility = View.VISIBLE
            btnJoin.visibility = View.GONE
            etMeetingId?.visibility = View.GONE
        }else{
            btnCreate.visibility = View.GONE
        }
        btnCreate.setOnClickListener {
            createMeeting(
                sampleToken
            )
        }
        Handler(Looper.getMainLooper()).postDelayed({
            Log.e("TAG","delay ")
            readMeetingID()
        }, 300)

        btnJoin.setOnClickListener {
            val sMeetingID = etMeetingId?.text.toString()
            if (sMeetingID.isEmpty()){
                toastme("No Meeting Available!")
            }else {
                val intent = Intent(this@JoinActivity, VideoChatActivity::class.java)
                intent.putExtra("token", sampleToken)
                intent.putExtra("meetingId", sMeetingID)
                startActivity(intent)
                Constants.isFrom = "JOIN"
            }
        }
    }

    private fun createMeeting(token: String) {
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
                        uploadMeetingID(meetingId)
                        // starting the MeetingActivity with received roomId and our sampleToken
                        val intent = Intent(this@JoinActivity, VideoChatActivity::class.java)
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
                        Toast.makeText(this@JoinActivity, anError.message, Toast.LENGTH_SHORT).show()
                    }

                }
            })
    }

    private fun uploadMeetingID(meetingId: String) {
        myRef = FirebaseDatabase.getInstance().getReference("MEETINGS")
        val slotKey = myRef?.push()?.key!!
        val meetingID = MeetingIDModel(meetingId)

        myRef?.setValue(meetingID)?.addOnSuccessListener {
            //toastme("Header Added!")
            readMeetingID()
        }?.addOnFailureListener {

        }
    }

    private fun readMeetingID() {
        val reference = FirebaseDatabase.getInstance().reference.child("MEETINGS")
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    //Log.e("TAG","childSnapshot $childSnapshot")
                    try {
                        val meetingId = childSnapshot.value
                        Log.e("TAG", "meetingId ${meetingId.toString()}")
                        if (meetingId != null) {
                            if (meetingId == ""){
                                toastme("No meeting available!")
                                etMeetingId?.setText("")
                            }else{
                                etMeetingId?.setText(meetingId.toString())
                            }
                        }
                    } catch (e: DatabaseException) {
                        e.printStackTrace()
                        Log.e("TAG", "e ${e}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode)
            return false
        }
        return true
    }

    companion object {
        private const val PERMISSION_REQ_ID = 22
        private val REQUESTED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }
}