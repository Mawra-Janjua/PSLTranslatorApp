package com.example.psltranslatorapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sign_table")
data class SignEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val label: String,

    val createdAt: Long = System.currentTimeMillis()
)