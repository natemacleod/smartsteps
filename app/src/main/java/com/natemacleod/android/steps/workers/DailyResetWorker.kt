package com.natemacleod.android.steps.workers

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.natemacleod.android.steps.data.Day
import com.natemacleod.android.steps.data.DayRoomDatabase
import java.lang.Integer.max
import kotlinx.coroutines.*

class DailyResetWorker (ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        // set up db and shared preferences
        val db = DayRoomDatabase.getDatabase(applicationContext)
        val dao = db.dayDao()
        val prefs = applicationContext.getSharedPreferences("sp11", MODE_PRIVATE)
        val editor = prefs.edit()

        // get a snapshot of the data
        val data = CoroutineScope(Dispatchers.Main).async(Dispatchers.IO) {
            dao.snapshotList()
        }.await()

        try {
            // get average from past week
            val cday = prefs.getInt("currentDay", -1)
            var avg = 0
            repeat(7) { index ->
                if (index < data.size) {
                    avg += data[index].steps
                } else avg += 5000
            }
            avg /= 7
            avg = max(4000 + avg / 3, avg)
            avg = (avg * ((90 + prefs.getInt("percent", 10)) / 100F)).toInt()

            // delete an entry if needed
            if (cday >= 6) {
                dao.delete(cday - 6)
            }

            // add new entry and update the current day
            dao.insert(Day(0, cday + 1, 0, avg))
            editor.putInt("currentDay", cday + 1).apply()
            return Result.success()
        } catch (e: Throwable) {
            return Result.failure()
        }
    }
}
