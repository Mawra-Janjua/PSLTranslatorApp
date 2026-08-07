package com.example.psltranslatorapp.ml


import android.content.Context
import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MLInferenceEngine(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val modelFileName = "model.tflite"

    private val NUM_FRAMES = 60
    private val LANDMARKS_COUNT = 21
    private val COORDINATES = 3 // x, y, z
    private val INPUT_FEATURES = LANDMARKS_COUNT * COORDINATES // 63

    init {
        try {
            interpreter = Interpreter(loadModelFile(context.assets, modelFileName))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(assetManager: AssetManager, fileName: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(fileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predict(sequence: FloatArray): Int {
        if (interpreter == null || sequence.size != NUM_FRAMES * INPUT_FEATURES) return -1

        val inputBuffer = ByteBuffer.allocateDirect(1 * NUM_FRAMES * INPUT_FEATURES * 4)
        inputBuffer.order(ByteOrder.nativeOrder())
        sequence.forEach { inputBuffer.putFloat(it) }

        val output = Array(1) { FloatArray(10) }

        interpreter?.run(inputBuffer, output)

        return output[0].indices.maxByOrNull { output[0][it] } ?: -1
    }

    fun normalizeLandmarks(landmarks: List<FloatArray>): FloatArray {
        val normalized = FloatArray(LANDMARKS_COUNT * COORDINATES)
        if (landmarks.isEmpty()) return normalized

        val wrist = landmarks[0] // Landmark 0 is usually the wrist

        for (i in landmarks.indices) {
            normalized[i * 3] = landmarks[i][0] - wrist[0]     // x
            normalized[i * 3 + 1] = landmarks[i][1] - wrist[1] // y
            normalized[i * 3 + 2] = landmarks[i][2] - wrist[2] // z
        }
        return normalized
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    fun updateModelPath(string: String) {}
}
