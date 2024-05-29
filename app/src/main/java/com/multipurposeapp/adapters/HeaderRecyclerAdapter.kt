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
import com.multipurposeapp.data.HeaderModel
import com.multipurposeapp.utils.Constants
import kotlin.random.Random

class HeaderRecyclerAdapter(private val context: Context,
                            private val headerList:List<HeaderModel>,
    private val onHeaderClick: OnHeaderClick):
RecyclerView.Adapter<HeaderRecyclerAdapter.ViewHolder>(){
    class ViewHolder (ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val headerTitle: TextView = itemView.findViewById(R.id.headerTitle)
        val header:ConstraintLayout = itemView.findViewById(R.id.header)
        val cardView:CardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.header_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val headerModel = headerList?.get(position)
        holder.cardView.setCardBackgroundColor(getRandomColorCode())
        holder.headerTitle.text = headerModel?.headerName
        holder.header.setOnClickListener {
            headerModel?.headerName?.let { it1 -> onHeaderClick.onClick(it1) }
        }
        holder.header.setOnLongClickListener {
            if (Constants.isAdmin){
                headerModel?.let { it1 -> onHeaderClick.onLongClick(it1) }
            }
            true
        }
       // Log.e("headerModel","headerModel: ${headerModel?.headerName}")
    }

    override fun getItemCount(): Int {
        return headerList!!.size
    }

    private fun getRandomColorCode(): Int {
        val random = Random
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }
}