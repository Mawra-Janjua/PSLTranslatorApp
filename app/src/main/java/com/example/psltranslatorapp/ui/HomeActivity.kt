@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalGetImage::class,
    ExperimentalFoundationApi::class
)

package com.example.psltranslatorapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.psltranslatorapp.admin.AdminActivity

val PrimaryPurple = Color(0xFF6E5CF6)
val BgGray = Color(0xFFF7F8FC)
val TextPrimary = Color(0xFF1F2937)
val White = Color(0xFFFFFFFF)

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen() }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("Home") }
    var openAdmin by remember { mutableStateOf(false) }

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

    if (openAdmin) {
        LaunchedEffect(Unit) {
            try {
                context.startActivity(Intent(context, AdminActivity::class.java))
                Toast.makeText(context, "Admin Access Granted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Admin Panel Not Found", Toast.LENGTH_SHORT).show()
            }
            openAdmin = false
        }
    }

    Scaffold(
        containerColor = BgGray,
        bottomBar = {
            FloatingBottomNav(activeTab) { tab ->
                activeTab = tab
                when (tab) {
                    "Capture" -> context.startActivity(Intent(context, CaptureActivity::class.java))
                    "Practice" -> context.startActivity(Intent(context, PracticeActivity::class.java))
                    "Progress" -> context.startActivity(Intent(context, ProgressActivity::class.java))
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {

            HeaderSection(offsetX = offsetX, onAdminClick = { openAdmin = true })

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    "Welcome!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                NavigationCard(
                    "Learning Mode", "سیکھنے کا طریقہ", Icons.Outlined.Bookmark,
                    Color(0xFF70539A), Color(0xFF9480C7), Color(0xFF9067C9), offsetX
                ) {
                    context.startActivity(Intent(context, LearningMenuActivity::class.java))
                }

                NavigationCard(
                    "Capture Mode", "ترجمہ کا طریقہ", Icons.Outlined.CameraAlt,
                    Color(0xFFB8729B), Color(0xFFC979A5), Color(0xFFB8729B), offsetX
                ) {
                    context.startActivity(Intent(context, CaptureActivity::class.java))
                }

                NavigationCard(
                    "Practice Mode", "مشق کا طریقہ", Icons.Outlined.School,
                    Color(0xFF8DA450), Color(0xFF8DA450), Color(0xFF8DA450), offsetX
                ) {
                    context.startActivity(Intent(context, PracticeActivity::class.java))
                }

                NavigationCard(
                    "My Progress", "میری پیشرفت", Icons.Outlined.BarChart,
                    Color(0xFF508DA4), Color(0xFF508DA4), Color(0xFF508DA4), offsetX

                ) {
                    context.startActivity(Intent(context, ProgressActivity::class.java))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun HeaderSection(offsetX: Float, onAdminClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "header_scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF6750A4), Color(0xFF856FC2))))
                .combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = {},
                    onLongClick = { onAdminClick() }
                )
        ) {
            Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp).size(65.dp)) {
                Box(
                    modifier = Modifier.matchParentSize().graphicsLayer { translationX = offsetX }
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.15f), Color.Transparent)), RoundedCornerShape(22.dp))
                )
                Box(
                    modifier = Modifier.matchParentSize()
                        .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PanTool, null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
            }

            Column(modifier = Modifier.align(Alignment.Center).padding(start = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PSL Translator", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("پاکستانی اشاروں کی زبان", fontSize = 16.sp, color = Color.White.copy(0.8f))
            }
        }
    }
}

@Composable
fun NavigationCard(
    title: String,
    urdu: String,
    icon: ImageVector,
    startColor: Color,
    endColor: Color,
    iconColor: Color,
    offsetX: Float,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "card_scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        onClick = onClick,
        interactionSource = interaction
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(startColor, endColor)))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(120.dp)
                    .graphicsLayer { translationX = offsetX }
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.18f), Color.Transparent)))
            )

            Row(modifier = Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(26.dp))
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(urdu, fontSize = 14.sp, color = Color.White.copy(0.85f))
                }

                Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun FloatingBottomNav(activeTab: String, onTabClick: (String) -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 12.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.height(70.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("Home", Icons.Outlined.Home, activeTab == "Home") { onTabClick("Home") }
            NavItem("Capture", Icons.Outlined.CameraAlt, activeTab == "Capture") { onTabClick("Capture") }
            NavItem("Practice", Icons.Outlined.School, activeTab == "Practice") { onTabClick("Practice") }
            NavItem("Progress", Icons.Outlined.BarChart, activeTab == "Progress") { onTabClick("Progress") }
        }
    }
}

@Composable
fun NavItem(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    val color = if (active) PrimaryPurple else Color.Gray
    Column(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(label, fontSize = 11.sp, color = color, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}