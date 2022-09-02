package com.natemacleod.android.steps.data

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "days")
data class Day(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    val id: Int = 0,
    @ColumnInfo(name = "did") // day ID
    val did: Int,
    @ColumnInfo(name = "steps")
    var steps: Int,
    @ColumnInfo(name = "goal")
    var goal: Int
    )
