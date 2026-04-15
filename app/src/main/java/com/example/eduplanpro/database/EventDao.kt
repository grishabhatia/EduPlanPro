package com.eduplanpro.database

import androidx.room.*
import com.eduplanpro.models.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM events WHERE userId = :userId ORDER BY date DESC")
    fun getEventsByUser(userId: Int): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Int): Event?
}