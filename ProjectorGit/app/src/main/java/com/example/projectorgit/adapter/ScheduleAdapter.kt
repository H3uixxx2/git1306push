package com.example.projectorgit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectorgit.R
import com.example.projectorgit.item.ScheduleItem

class ScheduleAdapter(private val scheduleList: List<ScheduleItem>) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.tvTime)
        val titleTextView: TextView = view.findViewById(R.id.tvProgram)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scheduleItem = scheduleList[position]
        holder.timeTextView.text = scheduleItem.time
        holder.titleTextView.text = scheduleItem.title
    }

    override fun getItemCount() = scheduleList.size
}
