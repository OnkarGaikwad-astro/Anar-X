package com.farmlens.anarai.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SprayScheduleDao {
    @Query("SELECT * FROM spray_schedule ORDER BY dateMillis ASC")
    fun getAllSchedules(): Flow<List<SprayScheduleEntity>>

    @Insert
    suspend fun insert(schedule: SprayScheduleEntity)

    @Update
    suspend fun update(schedule: SprayScheduleEntity)

    @Delete
    suspend fun delete(schedule: SprayScheduleEntity)
}
