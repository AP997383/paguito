package com.nexus.medi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HeartRateTable")
data class CampaingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = 0,
    val tipeMedition:Int=0,
    val heartRate: Int=0,
    val dateTime: String="",
    val sync: Boolean=false
)
