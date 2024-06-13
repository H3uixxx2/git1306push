package com.example.projectorgit.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.projectorgit.R
import com.example.projectorgit.adapter.ScheduleAdapter
import com.example.projectorgit.item.ScheduleItem

class ScheduleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private val scheduleList = listOf(
        ScheduleItem("13:30", "Siêu nhân"),
        ScheduleItem("14:30", "Hoạt hình"),
        ScheduleItem("17:30", "GameTV"),
        ScheduleItem("19:00", "Tin tức"),
        ScheduleItem("20:00", "Phim hành động"),
        ScheduleItem("20:00", "Phim hành động"),
        ScheduleItem("20:00", "Phim hành động"),
        ScheduleItem("20:00", "Phim hành động"),
        ScheduleItem("20:00", "Phim hành động"),
        ScheduleItem("20:00", "Phim hành động"),
        ScheduleItem("20:00", "Phim hành động"),
        ScheduleItem("20:00", "Phim hành động"),
        ScheduleItem("20:00", "Phim hành động"),
        ScheduleItem("21:30", "Thời sự")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // Thiết lập RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        scheduleAdapter = ScheduleAdapter(scheduleList)
        recyclerView.adapter = scheduleAdapter
    }
}
