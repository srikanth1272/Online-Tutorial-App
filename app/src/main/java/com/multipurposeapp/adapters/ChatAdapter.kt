package com.multipurposeapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.multipurposeapp.R
import com.multipurposeapp.data.Chat


class ChatAdapter(val context: Context, private val mList: List<Chat>?):
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1
    var fuser: FirebaseUser? = null

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        return if (viewType == MSG_TYPE_RIGHT) {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false)
            ViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false)
            ViewHolder(view)
        }
    }
    override fun getItemViewType(position: Int): Int {
        fuser = FirebaseAuth.getInstance().currentUser
        return if (mList!![position].sender == fuser!!.uid) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList?.get(position)

        // sets the image to the imageview from our itemHolder class
        //holder.profile_image.setImageResource(ItemsViewModel.image)

        // sets the text to the textview from our itemHolder class
        holder.show_message.text = ItemsViewModel?.message
        holder.time_tv.text = ItemsViewModel?.time

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList!!.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val profile_image: ImageView = itemView.findViewById(R.id.profile_image)
        val time_tv: TextView = itemView.findViewById(R.id.time_tv)
        val show_message:TextView = itemView.findViewById(R.id.show_message)
    }
}