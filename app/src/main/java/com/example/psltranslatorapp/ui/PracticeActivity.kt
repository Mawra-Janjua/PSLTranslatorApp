@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.psltranslatorapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ✅ Practice Mode Theme Colors (Greenish)
val PracticeStartColor = Color(0xFF8DA450)
val PracticeEndColor = Color(0xFF9CB462)

class PracticeActivity : ComponentActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PracticeScreen(
                labels = labels,
                onBack = { finish() },
                onStartCamera = { selectedWord ->
                    val intent = Intent(this, CaptureActivity::class.java)
                    intent.putExtra("selected_word", selectedWord.lowercase().trim())
                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun PracticeScreen(labels: List<String>, onBack: () -> Unit, onStartCamera: (String) -> Unit) {
    val context = LocalContext.current
    var selectedWord by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

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

    val practiceGradient = Brush.horizontalGradient(listOf(PracticeStartColor, PracticeEndColor))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .shadow(8.dp, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(practiceGradient, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
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
                modifier = Modifier.fillMaxSize().padding(top = 25.dp, start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Column {
                    Text(
                        text = "Practice Mode",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("پاکستان سائن لینگویج مترجم", color = Color.White.copy(0.8f), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Select a sign to practice:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedWord.isEmpty()) "Select Word..." else selectedWord,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PracticeStartColor,
                            unfocusedBorderColor = Color.LightGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Gray
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color.White)
                            .heightIn(max = 280.dp)
                    ) {
                        labels.forEach { word ->
                            DropdownMenuItem(
                                text = { Text(word.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedWord = word
                                    expanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Button(
                onClick = {
                    if (selectedWord.isEmpty()) {
                        Toast.makeText(context, "Please select a word first", Toast.LENGTH_SHORT).show()
                    } else {
                        onStartCamera(selectedWord)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(12.dp, RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PracticeStartColor),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { translationX = offsetX }
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color.White.copy(0.2f), Color.Transparent)
                                )
                            )
                    )
                    Text("Start Camera Practice", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}