package com.natemacleod.android.steps.data

import androidx.lifecycle.LiveData
import com.natemacleod.android.steps.data.Day
import com.natemacleod.android.steps.data.DayDao
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers

class DayRepository(private val dayDao: DayDao) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    val data: LiveData<List<Day>> = dayDao.getItems()

    suspend fun insert(day: Day) {
        val done = coroutineScope.async(Dispatchers.IO) {
            dayDao.insert(day)
        }
        done.await()
    }

    suspend fun update(day: Day) {
        val done = coroutineScope.async(Dispatchers.IO) {
            dayDao.update(day)
        }
        done.await()
    }

    suspend fun delete(id: Int) {
        val done = coroutineScope.async(Dispatchers.IO) {
            dayDao.delete(id)
        }
        done.await()
    }
}
