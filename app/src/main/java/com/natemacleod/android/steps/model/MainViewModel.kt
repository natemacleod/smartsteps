package com.natemacleod.android.steps.model

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.natemacleod.android.steps.workers.DailyResetWorker
import com.natemacleod.android.steps.data.Day
import com.natemacleod.android.steps.data.DayDao
import com.natemacleod.android.steps.data.DayRepository
import com.natemacleod.android.steps.data.DayRoomDatabase
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

class MainViewModel(application: Application) : ViewModel() {

    val days: LiveData<List<Day>>
    private val repository: DayRepository
    val prefs = application.getSharedPreferences("sp11", MODE_PRIVATE)
    val editor = prefs.edit()
    var buffer = MutableLiveData<Int>(0)
    var dao: DayDao

    init {
        // set up database
        val db = DayRoomDatabase.getDatabase(application)
        dao = db.dayDao()
        repository = DayRepository(dao)
        days = repository.data

        // set up shared preferences (first time startup only)
        if (!prefs.contains("percent")) {
            println("In setup")
            editor.putInt("percent", 10)
            editor.putInt("currentDay", 0)
            insert(Day(0, 0, 0, 5000))
            editor.commit()

            // set up work request to run at 00:00 every day
            val wr = PeriodicWorkRequest.Builder(
                DailyResetWorker::class.java,
                1, TimeUnit.DAYS
            ).setInitialDelay(calculateTimeUntilMidnight(), TimeUnit.MILLISECONDS).build()

            WorkManager.getInstance().enqueueUniquePeriodicWork(
                "RESET", ExistingPeriodicWorkPolicy.REPLACE, wr
            )
        }
    }

    // Calculates the time until midnight in milliseconds using the current system time.
    private fun calculateTimeUntilMidnight(): Long {
        val z: ZoneId = ZoneId.of("America/Toronto")
        val now: ZonedDateTime = ZonedDateTime.now(z)

        val tomorrow: LocalDate = now.toLocalDate().plusDays(1)
        val tomorrowStart: ZonedDateTime = tomorrow.atStartOfDay(z)

        val duration = Duration.between(now, tomorrowStart)
        return duration.toMillis()
    }

    // DB functions
    private fun insert(day: Day) {
        viewModelScope.launch { repository.insert(day) }
    }

    private suspend fun update(did: Int, addedSteps: Int = 0, newGoal: Int = -1) {
        val d = CoroutineScope(Dispatchers.Main).async(Dispatchers.IO) {
            dao.snapshot(did)
        }.await()
        d.apply {
            if (addedSteps != 0) steps += addedSteps
            if (newGoal != -1) goal = newGoal
        }
        viewModelScope.launch { repository.update(d) }
    }

    // Adds a step to the count.
    // Note: This will trigger after every step, but it will only update the database
    //   once every 10 steps to avoid absurd numbers of calls to update().
    fun addStep() {
        buffer.value = buffer.value?.plus(1)
        if (buffer.value == 10) {
            viewModelScope.launch { update(prefs.getInt("currentDay", 0), addedSteps = 10) }
            buffer.value = 0
        }
    }
}
