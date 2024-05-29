package com.multipurposeapp.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.multipurposeapp.R
import com.multipurposeapp.data.ContentModel
import kotlin.random.Random

class ContentRecyclerAdapter(private val context:Context,
                             private val contentList:List<ContentModel>,
    val onContentClick: OnContentClick) :
    RecyclerView.Adapter<ContentRecyclerAdapter.ViewModel>(){

    class ViewModel (ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val contentTitle:TextView = itemView.findViewById(R.id.contentTitle)
        val contentItem: ConstraintLayout = itemView.findViewById(R.id.contentItem)
        val cardView:CardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContentRecyclerAdapter.ViewModel {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.content_item, parent, false)
        return ViewModel(view)
    }

    override fun onBindViewHolder(holder: ContentRecyclerAdapter.ViewModel, position: Int) {
        val contentModel = contentList?.get(position)
        holder.cardView.setCardBackgroundColor(getRandomColorCode())
        holder.contentTitle.text = "${contentModel?.contentName}"
        holder.cardView.setOnClickListener {
            contentModel?.contentName?.let { it1 -> onContentClick.onClick(it1) }
        }
        holder.cardView.setOnLongClickListener {
            contentModel?.let { it1 -> onContentClick.onLongClick(it1) }
            true
        }
    }

    override fun getItemCount(): Int {
        return contentList?.size!!
    }

    private fun getRandomColorCode(): Int {
        val random = Random
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }
}