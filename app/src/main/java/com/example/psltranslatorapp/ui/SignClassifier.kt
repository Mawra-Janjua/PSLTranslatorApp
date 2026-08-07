package com.example.psltranslatorapp.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SignClassifier(context: Context) {
    private var interpreter: Interpreter? = null
    private var handLandmarker: HandLandmarker? = null
    private val TAG = "SignClassifier"

    private val inputBuffer = Array(1) { Array(60) { FloatArray(63) } }
    private val outputBuffer = Array(1) { FloatArray(15) }

    private val labels = listOf(
        "assalamualaikum",
        "tea",
        "fail"  ,
        "garden",
        "grapes",
        "home",
        "ihaveacomplaint",
        "water",
        "mine",
        "mother",
        "multan",
        "orange",
        "sleep",
        "cousin",
        "lahore"
    )

    init {
        try {
            val options = Interpreter.Options().apply {
                addDelegate(FlexDelegate())
                setNumThreads(4)
                setUseXNNPACK(true)
            }

            val modelBuffer = loadModelFile(context, "model.tflite")
            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "Fast Inference Model Ready")

            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()

            val mpOptions = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setNumHands(1)
                .setRunningMode(com.google.mediapipe.tasks.vision.core.RunningMode.IMAGE)
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, mpOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Init Error: ${e.message}")
        }
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fd = context.assets.openFd(modelName)
        return FileInputStream(fd.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength
        )
    }

    fun getLandmarksFromBitmap(bitmap: Bitmap): FloatArray? {
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = handLandmarker?.detect(mpImage) ?: return null
            if (result.landmarks().isEmpty()) return null

            val landmarks = result.landmarks()[0]
            val features = FloatArray(63)
            for (i in 0 until 21) {
                features[i * 3] = landmarks[i].x()
                features[i * 3 + 1] = landmarks[i].y()
                features[i * 3 + 2] = landmarks[i].z()
            }
            features
        } catch (e: Exception) { null }
    }

    fun predict(sequence: List<FloatArray>): Pair<String, Float> {
        if (sequence.size < 60) return Pair("Buffering...", 0f)

        return try {
            for (i in 0 until 60) {
                System.arraycopy(sequence[i], 0, inputBuffer[0][i], 0, 63)
            }

            interpreter?.run(inputBuffer, outputBuffer)

            val probabilities = outputBuffer[0]
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
            val confidence = if (maxIndex != -1) probabilities[maxIndex] else 0f

            Log.d(TAG, "MODEL RESULT -> Index: $maxIndex | Conf: $confidence")

            val label = if (maxIndex != -1 && confidence > 0.45f) {
                labels[maxIndex]
            } else {
                "Unknown"
            }

            Pair(label, confidence)
        } catch (e: Exception) {
            Pair("Error", 0f)
        }
    }
}