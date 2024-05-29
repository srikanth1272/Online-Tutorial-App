package com.multipurposeapp.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.multipurposeapp.R
import com.multipurposeapp.data.InstructorModel
import com.multipurposeapp.utils.Constants


class InstructorRecyclerAdapter(private val context: Context,
                             private val insList:List<InstructorModel>,
                             val onContentClick: IClick) :
    RecyclerView.Adapter<InstructorRecyclerAdapter.ViewModel>(){

    class ViewModel (ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val insName: TextView = itemView.findViewById(R.id.insName)
        val insProfession: TextView = itemView.findViewById(R.id.insProfession)
        val instructorImage: ImageView = itemView.findViewById(R.id.instructorImage)
        val contentItem: ConstraintLayout = itemView.findViewById(R.id.contentItem)

        val phone: TextView = itemView.findViewById(R.id.phone)
        val email: TextView = itemView.findViewById(R.id.email)
        val dob: TextView = itemView.findViewById(R.id.dob)
        val education: TextView = itemView.findViewById(R.id.education)
        val achievements: TextView = itemView.findViewById(R.id.achievements)
        val studyTime: TextView = itemView.findViewById(R.id.studyTime)
        val courseFee: TextView = itemView.findViewById(R.id.courseFee)
        val youtubeLink:ImageView = itemView.findViewById(R.id.youtubeLink)
        val chat:ImageView = itemView.findViewById(R.id.chat)
        val videoCall:ImageView = itemView.findViewById(R.id.videoCall)


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InstructorRecyclerAdapter.ViewModel {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.instructor_item, parent, false)
        return ViewModel(view)
    }

    override fun onBindViewHolder(holder: InstructorRecyclerAdapter.ViewModel, position: Int) {
        val contentModel = insList?.get(position)
        holder.insName.text = "Name: "+contentModel?.instructorName
        holder.insProfession.text = "Profession: "+contentModel?.insProfession
        holder.contentItem.setOnClickListener {
            contentModel?.let { it1 -> onContentClick.onLongClick(it1) }
        }

        holder.youtubeLink.setOnClickListener {
            contentModel?.demoLinks?.let { it1 -> onContentClick.youtube(it1) }
        }

        holder.chat.setOnClickListener {
            contentModel?.let { it1 -> onContentClick.chat(it1) }
        }

        holder.videoCall.setOnClickListener {
            contentModel?.let { it1 -> onContentClick.video(it1) }
        }


        var imgUrl = Constants.BaseUrl+contentModel?.instructorImage+ Constants.PostUrl
        Glide.with(context).load(imgUrl).into(holder.instructorImage)

        holder.phone.text = "phone: "+contentModel?.phone
        holder.email.text = "email: "+contentModel?.email
        holder.dob.text = "dob: "+contentModel?.dob
        holder.education.text = "education: "+contentModel?.education
        holder.achievements.text = "achievements: "+contentModel?.achievements
        holder.studyTime.text = "studyTime: "+contentModel?.studyTime
        holder.courseFee.text = "Fee: "+contentModel?.courseFee

    }

    override fun getItemCount(): Int {
        return insList?.size!!
    }
}