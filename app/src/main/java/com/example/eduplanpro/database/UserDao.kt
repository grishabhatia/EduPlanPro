package com.eduplanpro.database

import androidx.room.*
import com.eduplanpro.models.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND name = :name")
    suspend fun getUserByEmailAndName(email: String, name: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?
}