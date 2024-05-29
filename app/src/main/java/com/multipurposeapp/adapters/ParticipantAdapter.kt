package com.multipurposeapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.multipurposeapp.R
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.Stream
import live.videosdk.rtc.android.VideoView
import live.videosdk.rtc.android.listeners.MeetingEventListener
import live.videosdk.rtc.android.listeners.ParticipantEventListener
import org.webrtc.VideoTrack

class ParticipantAdapter(meeting: Meeting,var context: Context) :
    RecyclerView.Adapter<ParticipantAdapter.PeerViewHolder>() {
    private val participants: MutableList<Participant> = ArrayList()

    init {
        // adding the local participant(You) to the list
        participants.add(meeting.localParticipant)

        // adding Meeting Event listener to get the participant join/leave event in the meeting.
        meeting.addEventListener(object : MeetingEventListener() {
            override fun onParticipantJoined(participant: Participant) {
                // add participant to the list
                participants.add(participant)
                notifyItemInserted(participants.size - 1)
            }

            override fun onParticipantLeft(participant: Participant) {
                var pos = -1
                for (i in participants.indices) {
                    if (participants[i].id == participant.id) {
                        pos = i
                        break
                    }
                }
                // remove participant from the list
                participants.remove(participant)
                if (pos >= 0) {
                    notifyItemRemoved(pos)
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        return PeerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_remote_peer, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        val participant = participants[position]
        holder.tvName.text = participant.displayName
        var videoTrack:VideoTrack? = null
        // adding the initial video stream for the participant into the 'VideoView'
        for ((_, stream) in participant.streams) {
            if (stream.kind.equals("video", ignoreCase = true)) {
                holder.participantView.visibility = View.VISIBLE
                videoTrack = stream.track as VideoTrack
                holder.participantView.addTrack(videoTrack)
                break
            }
        }
        holder.itemView.setOnClickListener {
            Log.e("ParticipantAdapter","item clicked")
            for ((_, stream) in participant.streams) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    videoTrack = stream.track as VideoTrack
                    bigDialog(participant, videoTrack!!)
                    break
                }
            }

        }


        // add Listener to the participant which will update start or stop the video stream of that participant
        participant.addEventListener(object : ParticipantEventListener() {
            override fun onStreamEnabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    holder.participantView.visibility = View.VISIBLE
                    val videoTrack = stream.track as VideoTrack
                    holder.participantView.addTrack(videoTrack)
                }
            }

            override fun onStreamDisabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    holder.participantView.removeTrack()
                    holder.participantView.visibility = View.GONE
                }
            }
        })
    }

    private fun bigDialog(participant: Participant, videoTrack: VideoTrack) {
        Log.e("TAG","${participant.displayName}")
        val builder = AlertDialog.Builder(context).create()
        val inflater = LayoutInflater.from(context)
        val dialogLayout = inflater.inflate(R.layout.video_dialog_big, null)

        val participantView:VideoView = dialogLayout.findViewById(R.id.participantView)
        participantView.visibility = View.VISIBLE
        participantView.addTrack(videoTrack)
        val tvName:TextView = dialogLayout.findViewById(R.id.tvName)
        tvName.text = participant.displayName
        builder.setView(dialogLayout)
        builder.show()
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    class PeerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 'VideoView' to show Video Stream
        var participantView: VideoView
        var tvName: TextView

        init {
            tvName = view.findViewById(R.id.tvName)
            participantView = view.findViewById(R.id.participantView)
        }
    }

    override fun onViewRecycled(holder: PeerViewHolder) {
        holder.participantView.releaseSurfaceViewRenderer();
        super.onViewRecycled(holder)
    }
}