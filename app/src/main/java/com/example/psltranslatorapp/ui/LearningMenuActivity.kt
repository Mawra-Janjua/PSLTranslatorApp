@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.example.psltranslatorapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LearnStartColor = Color(0xFF70539A)
val LearnEndColor = Color(0xFF9480C7)

class LearningMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LearningMenuScreen()
        }
    }
}

@Composable
fun LearningMenuScreen() {
    val context = LocalContext.current
    val activity = (context as? Activity)

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp) // Matched height
                .shadow(8.dp, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(
                    Brush.horizontalGradient(listOf(LearnStartColor, LearnEndColor)),
                    RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                )
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

        IconButton(
            onClick = { activity?.finish() },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 20.dp, start = 8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Learning Mode",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "پاکستان سائن لینگویج مترجم",
                color = Color.White.copy(0.8f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(30.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryCard("English Alphabets", offsetX) {
                context.startActivity(Intent(context, EnglishAlphabetActivity::class.java))
            }

            CategoryCard("Urdu Alphabets", offsetX) {
                context.startActivity(Intent(context, UrduAlphabetActivity::class.java))
            }

            CategoryCard("Numbers", offsetX) {
                context.startActivity(Intent(context, NumbersActivity::class.java))
            }
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    offsetX: Float,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "card_scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick,
        interactionSource = interaction,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(LearnStartColor, LearnEndColor)))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(120.dp)
                    .graphicsLayer { translationX = offsetX }
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color.White.copy(0.2f), Color.Transparent)
                        )
                    )
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
