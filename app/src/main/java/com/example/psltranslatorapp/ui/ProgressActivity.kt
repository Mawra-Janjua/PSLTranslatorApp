@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.psltranslatorapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProgressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProgressScreen(onBack = { finish() })
        }
    }
}

@Composable
fun ProgressScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val primaryBlue = Color(0xFF508DA4)
    val lightBlue = Color(0xFF649FB5)
    val greenColor = Color(0xFF4CAF50)
    val redColor = Color(0xFFF44336)
    val progressGradient = Brush.horizontalGradient(listOf(primaryBlue, lightBlue))

    var refreshTrigger by remember { mutableStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "shiny_animation")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    // Database access
    val db = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "psl_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val detailedList by produceState<List<DetailedStat>>(emptyList(), db, refreshTrigger) {
        value = try {
            db.practiceDao().getDetailedStats()
        } catch (e: Exception) {
            emptyList()
        }
    }

    val totalCorrect = remember(detailedList) { detailedList.filter { it.isCorrect }.sumOf { it.count } }
    val totalWrong = remember(detailedList) { detailedList.filter { !it.isCorrect }.sumOf { it.count } }
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(detailedList) {
        if (detailedList.isNotEmpty()) {
            val correctEntries = mutableListOf<FloatEntry>()
            val wrongEntries = mutableListOf<FloatEntry>()
            val distinctSigns = detailedList.map { it.signName }.distinct()

            distinctSigns.forEachIndexed { index, name ->
                val x = index.toFloat()
                val cVal = detailedList.find { it.signName == name && it.isCorrect }?.count?.toFloat() ?: 0f
                val wVal = detailedList.find { it.signName == name && !it.isCorrect }?.count?.toFloat() ?: 0f
                correctEntries.add(FloatEntry(x, cVal))
                wrongEntries.add(FloatEntry(x, wVal))
            }
            chartEntryModelProducer.setEntries(correctEntries, wrongEntries)
        } else {
            chartEntryModelProducer.setEntries(emptyList(), emptyList())
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF6F6F6))) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .shadow(8.dp, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(progressGradient, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationX = offsetX }
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color.White.copy(0.15f), Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 25.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progress & Accuracy",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("پاکستان سائن لینگویج مترجم", color = Color.White.copy(0.8f), fontSize = 12.sp)
                }

                IconButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            db.practiceDao().deleteAll()
                            refreshTrigger++
                        } catch (e: Exception) { }
                    }
                }) {
                    Icon(Icons.Default.Delete, "Clear All", tint = Color.White)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatSummaryCard("Correct", totalCorrect.toString(), greenColor, Modifier.weight(1f), Icons.Default.CheckCircle)
                    StatSummaryCard("Wrong", totalWrong.toString(), redColor, Modifier.weight(1f), Icons.Default.Cancel)
                }

                Spacer(modifier = Modifier.height(25.dp))

                Text("Accuracy Analysis", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.DarkGray)
                Text("Visualizing your practice performance", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Chart(
                            chart = lineChart(
                                lines = listOf(
                                    LineChart.LineSpec(lineColor = greenColor.toArgb(), lineThicknessDp = 3f),
                                    LineChart.LineSpec(lineColor = redColor.toArgb(), lineThicknessDp = 3f)
                                )
                            ),
                            chartModelProducer = chartEntryModelProducer,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = primaryBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("Capture Breakdown", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.DarkGray)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (detailedList.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Text("No history found.", color = Color.Gray)
                    }
                }
            } else {
                items(detailedList) { stat ->
                    DetailedStatRow(stat, greenColor, redColor)
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}


@Composable
fun StatSummaryCard(label: String, value: String, color: Color, modifier: Modifier, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun DetailedStatRow(stat: DetailedStat, green: Color, red: Color) {
    val statusColor = if (stat.isCorrect) green else red
    val statusText = if (stat.isCorrect) "Correct" else "Incorrect"

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(statusColor))
                Spacer(modifier = Modifier.width(15.dp))
                Column {
                    Text(stat.signName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(statusText, fontSize = 12.sp, color = statusColor)
                }
            }
            Text("${stat.count} times", fontWeight = FontWeight.Medium, color = Color.DarkGray, fontSize = 14.sp)
        }
    }
}