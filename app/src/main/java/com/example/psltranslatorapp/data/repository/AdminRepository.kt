package com.example.psltranslatorapp.data.repository

import com.example.psltranslatorapp.data.datastore.AdminSettings
import com.example.psltranslatorapp.data.local.SignDao
import com.example.psltranslatorapp.data.local.SignEntity
import com.example.psltranslatorapp.ml.MLInferenceEngine
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.ArrayDeque

class AdminRepository(
    private val signDao: SignDao,
    private val mlEngine: MLInferenceEngine,
    private val settings: AdminSettings
) {

    fun getAllSigns(): Flow<List<SignEntity>> = signDao.getAllSigns()

    suspend fun addSign(label: String) {
        if (label.isNotBlank()) {
            signDao.insertSign(SignEntity(label = label))
        }
    }

    suspend fun deleteSign(sign: SignEntity) {
        signDao.deleteSign(sign)
    }

    val confidenceThreshold: Flow<Float> = settings.confidenceThreshold

    suspend fun updateThreshold(value: Float) {
        settings.updateThreshold(value)
    }


    fun runInference(buffer: ArrayDeque<FloatArray>): Int {
        val flatArray = FloatArray(60 * 63)
        var index = 0

        for (frame in buffer) {
            for (value in frame) {
                if (index < flatArray.size) {
                    flatArray[index++] = value
                }
            }
        }
        return mlEngine.predict(flatArray)
    }

  
    fun updateModel(file: File) {
        if (file.exists()) {

            mlEngine.updateModelPath(file.absolutePath)
        }
    }
}