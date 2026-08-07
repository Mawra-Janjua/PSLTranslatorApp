package com.example.psltranslatorapp.ui

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import java.util.Locale
import java.util.concurrent.Executors

data class DetailedStat(
    val signName: String,
    val isCorrect: Boolean,
    val count: Int
)


@Entity(tableName = "practice_history")
data class PracticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val signName: String,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface PracticeDao {
    @Insert
    suspend fun insertPractice(practice: PracticeEntity)

    @Query("SELECT COUNT(*) FROM practice_history WHERE isCorrect = 1")
    suspend fun getTotalCorrect(): Int

    @Query("SELECT COUNT(*) FROM practice_history WHERE isCorrect = 0")
    suspend fun getTotalWrong(): Int

    @Query("SELECT signName, isCorrect, COUNT(*) as count FROM practice_history GROUP BY signName, isCorrect")
    suspend fun getDetailedStats(): List<DetailedStat>

    @Query("DELETE FROM practice_history")
    suspend fun deleteAll()
}

@Database(entities = [PracticeEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun practiceDao(): PracticeDao
}


enum class CaptureState { DEFAULT, SUCCESS, ERROR, LOADING }

class CaptureActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var classifier: SignClassifier

    private val sequenceBuffer = ArrayDeque<FloatArray>(60)
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classifier = SignClassifier(this)
        tts = TextToSpeech(this, this)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "psl_db")
            .fallbackToDestructiveMigration()
            .build()

        val targetWord = intent.getStringExtra("selected_word")

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) finish()
        }
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        setContent {
            CaptureScreen(
                classifier = classifier,
                sequenceBuffer = sequenceBuffer,
                targetWord = targetWord,
                onBack = { finish() },
                onSpeak = { speak(it) },
                onSaveProgress = { sign, correct -> saveSignToDatabase(sign, correct) }
            )
        }
    }

    private fun saveSignToDatabase(sign: String, correct: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.practiceDao().insertPractice(PracticeEntity(signName = sign, isCorrect = correct))
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts.language = Locale.US
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }

    fun ImageProxy.toCustomBitmap(): Bitmap {
        val plane = planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        val cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        val matrix = Matrix()
        matrix.postRotate(imageInfo.rotationDegrees.toFloat())
        matrix.postScale(-1f, 1f, width / 2f, height / 2f)
        return Bitmap.createBitmap(cropped, 0, 0, cropped.width, cropped.height, matrix, true)
    }
}


@Composable
fun CaptureScreen(
    classifier: SignClassifier,
    sequenceBuffer: ArrayDeque<FloatArray>,
    targetWord: String?,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onSaveProgress: (String, Boolean) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentState by remember { mutableStateOf(CaptureState.DEFAULT) }
    var detectedText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("Gesture not recognized") }
    var bufferCount by remember { mutableStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "shiny_animation")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -400f, targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer_offset"
    )

    val startColor = Color(0xFFA85C89)
    val endColor = Color(0xFFAD6B8F)
    val captureModeGradient = Brush.horizontalGradient(listOf(startColor, endColor))

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF6F6F6))) {
        Box(
            modifier = Modifier.fillMaxWidth().height(110.dp)
                .shadow(8.dp, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(captureModeGradient, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer { translationX = offsetX }
                .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.15f), Color.Transparent))))

            Row(modifier = Modifier.fillMaxSize().padding(top = 25.dp, start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Column {
                    Text(text = if (targetWord != null) "Practice: $targetWord" else "Capture Mode", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("پاکستان سائن لینگویج مترجم", color = Color.White.copy(0.8f), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().aspectRatio(1f)
            .shadow(10.dp, RoundedCornerShape(24.dp))
            .border(4.dp, Color.White, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)).background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
                        analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                            val activity = ctx as? CaptureActivity
                            val bitmap = activity?.run { imageProxy.toCustomBitmap() }
                            if (bitmap != null) {
                                val landmarks = classifier.getLandmarksFromBitmap(bitmap)
                                if (landmarks != null) {
                                    synchronized(sequenceBuffer) {
                                        sequenceBuffer.addLast(landmarks)
                                        if (sequenceBuffer.size > 60) sequenceBuffer.removeFirst()
                                        bufferCount = sequenceBuffer.size
                                    }
                                }
                            }
                            imageProxy.close()
                        }
                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Surface(modifier = Modifier.fillMaxWidth().shadow(20.dp, RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp)),
            color = Color.White, shape = RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).animateContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                when (currentState) {
                    CaptureState.DEFAULT -> {
                        val progress = bufferCount / 60f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = if (progress >= 1f) Color(0xFF4CAF50) else startColor
                        )
                        Text(text = if (progress >= 1f) "Ready" else "Keep Hand: ${(progress * 100).toInt()}%", fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

                        ActionButton("Capture Gesture", Icons.Default.CameraAlt, captureModeGradient, offsetX) {
                            if (bufferCount >= 60) {
                                currentState = CaptureState.LOADING
                                val (word, conf) = classifier.predict(sequenceBuffer.toList())

                                if (conf > 0.45f && word != "Unknown") {
                                    detectedText = word
                                    if (targetWord != null) {
                                        val isCorrect = word.equals(targetWord, true)
                                        currentState = if (isCorrect) CaptureState.SUCCESS else CaptureState.ERROR
                                        if (!isCorrect) errorMessage = "Wrong Sign! You did: $word"
                                        onSpeak(if (isCorrect) "Correct! This is $word" else "Try again, that was $word")
                                        onSaveProgress(word, isCorrect)
                                    } else {
                                        currentState = CaptureState.SUCCESS
                                        onSpeak(word)
                                        onSaveProgress(word, true)
                                    }
                                } else {
                                    errorMessage = "Gesture not recognized"
                                    currentState = CaptureState.ERROR
                                }
                            }
                        }
                    }
                    CaptureState.ERROR -> {
                        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFEBEE), RoundedCornerShape(16.dp)).padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red)
                                Spacer(Modifier.width(12.dp))
                                Text(errorMessage, color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        ActionButton("Try Again", Icons.Default.Refresh, Brush.linearGradient(listOf(Color.LightGray, Color.Gray)), 0f) {
                            sequenceBuffer.clear()
                            bufferCount = 0
                            currentState = CaptureState.DEFAULT
                        }
                    }
                    CaptureState.SUCCESS -> {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Detected Gesture:", fontSize = 14.sp, color = Color.Gray)
                                Text(detectedText, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                                Button(onClick = { onSpeak(detectedText) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = CircleShape) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Replay Sound")
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        ActionButton("Capture New", Icons.Default.Add, captureModeGradient, offsetX) {
                            sequenceBuffer.clear()
                            bufferCount = 0
                            currentState = CaptureState.DEFAULT
                        }
                    }
                    CaptureState.LOADING -> {
                        CircularProgressIndicator(color = startColor)
                        Text("Processing Sign...", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, gradient: Brush, offsetX: Float, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(55.dp).fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(30.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(gradient), contentAlignment = Alignment.Center) {
            if (offsetX != 0f) {
                Box(modifier = Modifier.fillMaxSize().graphicsLayer { translationX = offsetX }
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.2f), Color.Transparent))))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}