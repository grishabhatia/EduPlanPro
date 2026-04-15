package com.eduplanpro.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val mobile: String,
    val age: Int,
    val profileImagePath: String = "",  // This stores the image path
    val registrationDate: Long = System.currentTimeMillis()
)