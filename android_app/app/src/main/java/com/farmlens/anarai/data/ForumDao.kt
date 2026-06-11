package com.farmlens.anarai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {
    @Query("SELECT * FROM forum_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<ForumPostEntity>>

    @Insert
    suspend fun insert(post: ForumPostEntity)
}
