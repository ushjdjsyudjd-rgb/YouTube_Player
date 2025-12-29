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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    
    // مقادیر شتاب‌سنج
    private var accelX by mutableStateOf(0f)
    private var accelY by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            var gameState by remember { mutableStateOf("SPLASH") }

            LaunchedEffect(Unit) {
                delay(3000)
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
            // معکوس کردن مقادیر برای هماهنگی با حرکت نمایشی
            accelX = -event.values[0] 
            accelY = event.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @Composable
    fun LabyrinthSplash() {
        val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF0F2027), Color(0xFF2C5364)))
        Box(modifier = Modifier.fillMaxSize().background(bgGradient), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌀", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Text("LABYRINTH", color = Color(0xFF00E5FF), fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(100.dp))
                Text("Tilt your phone to move", color = Color.White.copy(0.6f), fontSize = 14.sp)
                Text("Developed by HsH. © Copyright", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }

    @Composable
    fun LabyrinthGame(ax: Float, ay: Float) {
        val ballRadius = 35f
        var ballPos by remember { mutableStateOf(Offset(150f, 150f)) }
        var gameWon by remember { mutableStateOf(false) }

        val goalPos = Offset(850f, 1600f)
        val goalRadius = 55f

        // طراحی مرحله ۱
        val walls = listOf(
            RectData(Offset(400f, 0f), Size(30f, 1200f)),
            RectData(Offset(0f, 800f), Size(400f, 30f)),
            RectData(Offset(700f, 500f), Size(30f, 1500f))
        )

        // آپدیت فریم‌های بازی
        LaunchedEffect(ax, ay) {
            if (!gameWon) {
                val speedMultiplier = 5f
                val nextX = ballPos.x + (ax * speedMultiplier)
                val nextY = ballPos.y + (ay * speedMultiplier)
                val nextPos = Offset(nextX, nextY)

                // بررسی برخورد با دیوار
                var hasCollision = false
                for (wall in walls) {
                    if (nextX + ballRadius > wall.pos.x && 
                        nextX - ballRadius < wall.pos.x + wall.size.width &&
                        nextY + ballRadius > wall.pos.y && 
                        nextY - ballRadius < wall.pos.y + wall.size.height) {
                        hasCollision = true
                    }
                }

                // محدودیت صفحه
                if (!hasCollision && nextX > 0 && nextY > 0) {
                    ballPos = nextPos
                }

                // چک کردن برد
                if ((ballPos - goalPos).getDistance() < goalRadius) {
                    gameWon = true
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // رسم هدف
                drawCircle(color = Color(0xFF00FF88), radius = goalRadius, center = goalPos)
                
                // رسم دیوارها
                for (wall in walls) {
                    drawRect(color = Color(0xFFFF5252), topLeft = wall.pos, size = wall.size)
                }

                // رسم توپ
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.Yellow, Color(0xFFFFA000))),
                    radius = ballRadius,
                    center = ballPos
                )
            }

            if (gameWon) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.8f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LEVEL COMPLETE!", color = Color.Cyan, fontSize = 35.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = { 
                            ballPos = Offset(150f, 150f)
                            gameWon = false 
                        }) {
                            Text("Play Again")
                        }
                    }
                }
            }
        }
    }
}

data class RectData(val pos: Offset, val size: Size)
