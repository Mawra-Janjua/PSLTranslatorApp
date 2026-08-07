package com.example.psltranslatorapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psltranslatorapp.data.local.SignEntity
import com.example.psltranslatorapp.data.repository.AdminRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.ArrayDeque

class AdminViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    val allSigns: StateFlow<List<SignEntity>> =
        repository.getAllSigns()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    val threshold: StateFlow<Float> =
        repository.confidenceThreshold
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.85f
            )


    fun addSign(label: String) {
        viewModelScope.launch {
            if (label.isNotBlank()) {
                repository.addSign(label) // Fixed method name
            }
        }
    }

    fun deleteSign(sign: SignEntity) {
        viewModelScope.launch {
            repository.deleteSign(sign) // Fixed method name
        }
    }

    private val _testResult = mutableStateOf("Waiting for hand...")
    val testResult: State<String> = _testResult

    private val buffer = ArrayDeque<FloatArray>()

    fun processFrame(features: FloatArray) {
        buffer.addLast(features)

        if (buffer.size > 60) buffer.removeFirst()

        if (buffer.size == 60) {
            val predictedIndex = repository.runInference(buffer)

            if (predictedIndex != -1) {
                _testResult.value = "Detected Sign Index: $predictedIndex"
            } else {
                _testResult.value = "Analyzing..."
            }
        }
    }

    fun updateModel(file: File) {
        viewModelScope.launch {
            repository.updateModel(file) // Fixed method name
        }
    }
}