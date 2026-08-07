package com.example.psltranslatorapp.ui

import android.content.Context
import android.util.Log
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandDetector(context: Context) {

    private val handLandmarker: HandLandmarker

    init {

        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.VIDEO)
            .setNumHands(1)
            .setMinHandDetectionConfidence(0.7f)
            .setMinTrackingConfidence(0.7f)
            .setMinHandPresenceConfidence(0.7f)
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)

        Log.d("HAND_INIT", "Hand Landmarker Loaded")
    }

    fun detect(image: MPImage, timestamp: Long): HandLandmarkerResult? {
        return try {
            handLandmarker.detectForVideo(image, timestamp)
        } catch (e: Exception) {
            Log.e("HAND_ERROR", e.message ?: "error")
            null
        }
    }
}