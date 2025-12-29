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
import kotlin.random.Random

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
            var level by remember { mutableStateOf(1) }

            MaterialTheme {
                when (gameState) {
                    "SPLASH" -> {
                        LaunchedEffect(Unit) { delay(2000); gameState = "PLAYING" }
                        FullScreenMessage("LABYRINTH", "PRO EDITION", Color(0xFF3E2723))
                    }
                    "NEXT_LEVEL" -> {
                        LaunchedEffect(Unit) { delay(1500); gameState = "PLAYING" }
                        FullScreenMessage("NEXT LEVEL", "Level $level is starting...", Color(0xFF1B5E20))
                    }
                    "PLAYING" -> {
                        LabyrinthGame(
                            ax = accelX,
                            ay = accelY,
                            currentLevel = level,
                            onWin = {
                                level++
                                gameState = "NEXT_LEVEL"
                            },
                            onLose = {
                                // اگر باخت، لول تغییر نمی‌کند و فقط بازی ریست می‌شود
                                gameState = "PLAYING" 
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun FullScreenMessage(title: String, sub: String, bgColor: Color) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = Color.White, fontSize = 45.sp, fontWeight = FontWeight.Black)
                Text(sub, color = Color.White.copy(0.7f), fontSize = 18.sp)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
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
    fun LabyrinthGame(ax: Float, ay: Float, currentLevel: Int, onWin: () -> Unit, onLose: () -> Unit) {
        val ballRadius = 28f
        var ballPos by remember { mutableStateOf(Offset(100f, 100f)) }
        var isGameOver by remember { mutableStateOf(false) }
        var isWin by remember { mutableStateOf(false) }

        // تولید محتوای تصادفی برای هر مرحله
        val holes = remember(currentLevel) {
            List(6 + currentLevel) { 
                Offset(Random.nextFloat() * 800f + 100f, Random.nextFloat() * 1400f + 200f)
            }
        }
        val walls = remember(currentLevel) {
            val list = mutableListOf(
                RectData(Offset(0f, 0f), Size(25f, 2000f)),
                RectData(Offset(1055f, 0f), Size(25f, 2000f)),
                RectData(Offset(0f, 1875f), Size(1080f, 25f))
            )
            repeat(4) {
                list.add(RectData(
                    Offset(Random.nextFloat() * 700f, Random.nextFloat() * 1400f + 200f),
                    Size(if(Random.nextBoolean()) 300f else 35f, if(Random.nextBoolean()) 35f else 300f)
                ))
            }
            list
        }

        val goalPos = Offset(900f, 1750f)

        LaunchedEffect(ax, ay) {
            if (!isGameOver && !isWin) {
                val speed = 8f
                val nextPos = Offset(ballPos.x + (ax * speed), ballPos.y + (ay * speed))

                var collision = false
                for (wall in walls) {
                    if (nextPos.x + ballRadius > wall.pos.x && nextPos.x - ballRadius < wall.pos.x + wall.size.width &&
                        nextPos.y + ballRadius > wall.pos.y && nextPos.y - ballRadius < wall.pos.y + wall.size.height) {
                        collision = true
                    }
                }

                if (!collision) ballPos = nextPos

                for (hole in holes) {
                    if ((ballPos - hole).getDistance() < 40f) isGameOver = true
                }

                if ((ballPos - goalPos).getDistance() < 50f) isWin = true
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEBC9A0))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // رسم چاله‌ها
                for (hole in holes) {
                    drawCircle(brush = Brush.radialGradient(listOf(Color.Black, Color(0xFF3E2723))), radius = 45f, center = hole)
                }

                // رسم هدف
                drawCircle(color = Color.Black, radius = 55f, center = goalPos, style = Stroke(width = 10f))
                drawCircle(color = Color.Red, radius = 20f, center = goalPos)

                // رسم دیوارها
                for (wall in walls) {
                    drawRect(color = Color(0xFF5D4037), topLeft = wall.pos, size = wall.size)
                }

                // رسم توپ فلزی براق
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFBDBDBD), Color(0xFF424242)),
                        center = Offset(ballPos.x - 8f, ballPos.y - 8f),
                        radius = ballRadius * 1.5f
                    ),
                    radius = ballRadius,
                    center = ballPos
                )
            }

            // نمایش سطح فعلی در بالای صفحه
            Text("LEVEL: $currentLevel", modifier = Modifier.padding(30.dp), color = Color(0xFF5D4037), fontWeight = FontWeight.Bold, fontSize = 18.sp)

            if (isGameOver || isWin) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.8f)), contentAlignment = Alignment.Center) {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isWin) "WINNER!" else "GAME OVER", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    if (isWin) onWin() else {
                                        ballPos = Offset(100f, 100f)
                                        isGameOver = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                            ) {
                                Text(if (isWin) "NEXT LEVEL" else "TRY AGAIN")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class RectData(val pos: Offset, val size: Size)
