package com.relax.sounds

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    
    private var accelX by mutableStateOf(0f)
    private var accelY by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            var gameState by remember { mutableStateOf("SPLASH") }
            LaunchedEffect(Unit) {
                delay(2500)
                gameState = "PLAYING"
            }

            MaterialTheme {
                when (gameState) {
                    "SPLASH" -> LabyrinthSplash()
                    "PLAYING" -> LabyrinthGame(accelX, accelY)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            accelX = -event.values[0] 
            accelY = event.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @Composable
    fun LabyrinthSplash() {
        val bg = Brush.verticalGradient(colors = listOf(Color(0xFF000428), Color(0xFF004E92)))
        Box(modifier = Modifier.fillMaxSize().background(bg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💎", fontSize = 70.sp)
                Text("LABYRINTH", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
                Text("PRO EDITION", color = Color.Cyan, fontSize = 14.sp, letterSpacing = 4.sp)
                Spacer(modifier = Modifier.height(50.dp))
                CircularProgressIndicator(color = Color.Cyan)
                Spacer(modifier = Modifier.height(20.dp))
                Text("Developed by HsH. © 2025", color = Color.White.copy(0.5f), fontSize = 10.sp)
            }
        }
    }

    @Composable
    fun LabyrinthGame(ax: Float, ay: Float) {
        val ballRadius = 30f
        var ballPos by remember { mutableStateOf(Offset(100f, 100f)) }
        var gameWon by remember { mutableStateOf(false) }

        // نقطه شروع: بالا سمت چپ (100, 100)
        // نقطه پایان: پایین سمت راست
        val goalPos = Offset(900f, 1800f)
        val goalRadius = 50f

        // طراحی مارپیچ حرفه‌ای (دیوارهای تو در تو)
        val walls = listOf(
            // حاشیه های بیرونی
            RectData(Offset(0f, 0f), Size(20f, 2000f)), // چپ
            RectData(Offset(1060f, 0f), Size(20f, 2000f)), // راست
            RectData(Offset(0f, 0f), Size(1080f, 20f)), // بالا
            RectData(Offset(0f, 1900f), Size(1080f, 20f)), // پایین

            // پیچ‌های داخلی مرحله ۱
            RectData(Offset(200f, 200f), Size(20f, 600f)),
            RectData(Offset(200f, 800f), Size(600f, 20f)),
            RectData(Offset(800f, 200f), Size(20f, 1000f)),
            RectData(Offset(400f, 400f), Size(400f, 20f)),
            RectData(Offset(400f, 1000f), Size(20f, 600f)),
            RectData(Offset(0f, 1300f), Size(400f, 20f)),
            RectData(Offset(600f, 1500f), Size(500f, 20f))
        )

        LaunchedEffect(ax, ay) {
            if (!gameWon) {
                val sensitivity = 7f
                val nextPos = Offset(ballPos.x + (ax * sensitivity), ballPos.y + (ay * sensitivity))

                var collision = false
                for (wall in walls) {
                    if (nextPos.x + ballRadius > wall.pos.x && 
                        nextPos.x - ballRadius < wall.pos.x + wall.size.width &&
                        nextPos.y + ballRadius > wall.pos.y && 
                        nextPos.y - ballRadius < wall.pos.y + wall.size.height) {
                        collision = true
                    }
                }

                if (!collision) {
                    ballPos = nextPos
                }

                if ((ballPos - goalPos).getDistance() < goalRadius) {
                    gameWon = true
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // رسم مسیر پایان با افکت درخشش
                drawCircle(color = Color(0xFF00FF88), radius = goalRadius, center = goalPos)
                drawCircle(color = Color(0xFF00FF88), radius = goalRadius + 10f, center = goalPos, style = Stroke(width = 2f))
                
                // رسم دیوارها با تم نئونی قرمز/صورتی
                for (wall in walls) {
                    drawRect(
                        brush = Brush.linearGradient(listOf(Color(0xFFFF0055), Color(0xFFFF5500))),
                        topLeft = wall.pos,
                        size = wall.size
                    )
                }

                // رسم توپ به صورت متالیک
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFFE0E0E0), Color(0xFF757575))),
                    radius = ballRadius,
                    center = ballPos
                )
            }

            // لایه رابط کاربری
            Column(modifier = Modifier.padding(20.dp)) {
                Text("LEVEL 1", color = Color.White.copy(0.5f), fontWeight = FontWeight.Bold)
                Text("Find the Green Exit", color = Color.White, fontSize = 12.sp)
            }

            if (gameWon) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.85f)), contentAlignment = Alignment.Center) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(30.dp)
                    ) {
                        Column(modifier = Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("VICTORY!", color = Color(0xFF00FF88), fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Level 1 Completed", color = Color.White)
                            Spacer(modifier = Modifier.height(30.dp))
                            Button(
                                onClick = { ballPos = Offset(100f, 100f); gameWon = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88))
                            ) {
                                Text("PLAY AGAIN", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class RectData(val pos: Offset, val size: Size)
