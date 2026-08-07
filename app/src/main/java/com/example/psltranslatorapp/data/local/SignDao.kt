package com.example.psltranslatorapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SignDao {

    @Query("SELECT * FROM sign_table ORDER BY id DESC")
    fun getAllSigns(): Flow<List<SignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSign(sign: SignEntity)

    @Delete
    suspend fun deleteSign(sign: SignEntity)

    @Query("SELECT COUNT(*) FROM sign_table")
    suspend fun getCount(): Int
}