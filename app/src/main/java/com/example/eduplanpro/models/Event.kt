package com.eduplanpro.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val date: Long,
    val time: String,
    val reminderType: String,
    val createdAt: Long = System.currentTimeMillis()
)