package com.natemacleod.android.steps.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.natemacleod.android.steps.data.Day

@Dao
interface DayDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Day)

    @Update
    suspend fun update(item: Day)

    @Query("DELETE FROM days WHERE did = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * from days where did = :id")
    suspend fun snapshot(id: Int): Day

    @Query("SELECT * from days ORDER BY did ASC")
    suspend fun snapshotList(): List<Day>

    @Query("SELECT * from days WHERE did = :id")
    fun getItem(id: Int): LiveData<Day>

    @Query("SELECT * from days ORDER BY did ASC")
    fun getItems(): LiveData<List<Day>>
}
