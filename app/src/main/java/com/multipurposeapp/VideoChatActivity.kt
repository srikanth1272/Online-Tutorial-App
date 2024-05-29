package com.multipurposeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.multipurposeapp.adapters.IVideoClicked
import com.multipurposeapp.adapters.ParticipantAdapter
import com.multipurposeapp.data.MeetingIDModel
import com.multipurposeapp.utils.Constants
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.listeners.MeetingEventListener

class VideoChatActivity : AppCompatActivity() {
    private var meeting: Meeting? = null
    private var micEnabled = true
    private var webcamEnabled = true
    private var rvParticipants: RecyclerView? = null
    var fuser: FirebaseUser? = null
    var participantName:String = ""
    private lateinit var auth: FirebaseAuth
    private var mFirebaseDatabase: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat)
        auth = Firebase.auth
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        myRef = mFirebaseDatabase?.reference

        fuser = FirebaseAuth.getInstance().currentUser

        val token = intent.getStringExtra("token")
        val meetingId = intent.getStringExtra("meetingId")
        participantName = fuser?.email.toString()
       /* if (Constants.isFrom=="JOIN"){
            participantName = fuser?.displayName.toString()
        }else {
            participantName = Constants.instructorModel?.instructorName.toString()
        }*///"Rajiv"//fuser?.displayName
        Log.e("TAG",participantName)

        // 1. Configuration VideoSDK with Token
        VideoSDK.config(token)
        // 2. Initialize VideoSDK Meeting
        meeting = VideoSDK.initMeeting(
            this@VideoChatActivity, meetingId, participantName,
            micEnabled, webcamEnabled, null, null, true, null
        )

        // 3. Add event listener for listening upcoming events
        meeting!!.addEventListener(meetingEventListener)

        //4. Join VideoSDK Meeting
        meeting!!.join()
        (findViewById<View>(R.id.tvMeetingId) as TextView).text = meetingId

        // actions
        setActionListeners()
        rvParticipants = findViewById(R.id.rvParticipants)
        rvParticipants!!.layoutManager = GridLayoutManager(this, 2)
        rvParticipants!!.adapter = ParticipantAdapter(meeting!!,this)
    }

    // creating the MeetingEventListener
    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingJoined() {
            Log.d("#meeting", "onMeetingJoined()")
        }

        override fun onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()")
            meeting = null
            if (!isDestroyed) finish()
        }

        override fun onParticipantJoined(participant: Participant) {
            Toast.makeText(
                this@VideoChatActivity,
                participant.displayName + " joined",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onParticipantLeft(participant: Participant) {
            Toast.makeText(
                this@VideoChatActivity,
                participant.displayName + " left",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setActionListeners() {
        // toggle mic
        findViewById<View>(R.id.btnMic).setOnClickListener {
            if (micEnabled) {
                // this will mute the local participant's mic
                meeting!!.muteMic()
                Toast.makeText(this@VideoChatActivity, "Mic Disabled", Toast.LENGTH_SHORT).show()
            } else {
                // this will unmute the local participant's mic
                meeting!!.unmuteMic()
                Toast.makeText(this@VideoChatActivity, "Mic Enabled", Toast.LENGTH_SHORT).show()
            }
            micEnabled = !micEnabled
        }

        // toggle webcam
        findViewById<View>(R.id.btnWebcam).setOnClickListener {
            if (webcamEnabled) {
                // this will disable the local participant webcam
                meeting!!.disableWebcam()
                Toast.makeText(this@VideoChatActivity, "Webcam Disabled", Toast.LENGTH_SHORT).show()
            } else {
                // this will enable the local participant webcam
                meeting!!.enableWebcam()
                Toast.makeText(this@VideoChatActivity, "Webcam Enabled", Toast.LENGTH_SHORT).show()
            }
            webcamEnabled = !webcamEnabled
        }

        // leave meeting
        findViewById<View>(R.id.btnLeave).setOnClickListener {
            // this will make the local participant leave the meeting
            meeting!!.leave()
            myRef = FirebaseDatabase.getInstance().getReference("MEETINGS")
            val slotKey = myRef?.push()?.key!!
            val meetingID = MeetingIDModel("")

            myRef?.setValue(meetingID)?.addOnSuccessListener {
                //toastme("Header Added!")
            }?.addOnFailureListener {

            }
        }
    }

    override fun onDestroy() {
        rvParticipants!!.adapter = null
        super.onDestroy()
    }
}