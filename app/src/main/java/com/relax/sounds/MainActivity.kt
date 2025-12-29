package com.relax.sounds

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
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

            when (gameState) {
                "SPLASH" -> {
                    LaunchedEffect(Unit) { delay(2000); gameState = "PLAYING" }
                    FullScreenMessage("LABYRINTH", "Get Ready...", Color(0xFF3E2723))
                }
                "NEXT_LEVEL" -> {
                    LaunchedEffect(Unit) { delay(1500); gameState = "PLAYING" }
                    FullScreenMessage("WELL DONE!", "Level $level is starting", Color(0xFF1B5E20))
                }
                "PLAYING" -> {
                    LabyrinthGame(
                        ax = accelX,
                        ay = accelY,
                        currentLevel = level,
                        onWin = { level++; gameState = "NEXT_LEVEL" },
                        onLose = { gameState = "PLAYING" }
                    )
                }
            }
        }
    }

    @Composable
    fun FullScreenMessage(title: String, sub: String, bgColor: Color) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
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
        var ballPos by remember { mutableStateOf(Offset(150f, 150f)) }
        var isFalling by remember { mutableStateOf(false) }
        var timeLeft by remember { mutableStateOf(60) }
        var isGameOver by remember { mutableStateOf(false) }

        val ballRadius = 30f
        val goalPos = Offset(900f, 1700f)
        val holeRadius = 50f
        
        // تولید تصادفی چاله‌ها (بیشتر شدن با لول)
        val holes = remember(currentLevel) {
            List(5 + (currentLevel * 2)) {
                Offset(Random.nextFloat() * 800f + 100f, Random.nextFloat() * 1400f + 300f)
            }
        }

        // انیمیشن کوچک شدن توپ موقع افتادن
        val ballScale by animateFloatAsState(
            targetValue = if (isFalling) 0f else 1f,
            animationSpec = tween(durationMillis = 500),
            finishedListener = { if (isFalling) onWin() }
        )

        // تایمر
        LaunchedEffect(currentLevel) {
            timeLeft = 60
            while (timeLeft > 0 && !isFalling && !isGameOver) {
                delay(1000)
                timeLeft--
            }
            if (timeLeft == 0) isGameOver = true
        }

        // منطق حرکت و برخورد
        LaunchedEffect(ax, ay) {
            if (!isFalling && !isGameOver) {
                val speed = 9f
                var newX = ballPos.x + (ax * speed)
                var newY = ballPos.y + (ay * speed)

                // برخورد با دیواره‌های کناری (قاب صفحه)
                // اینجا منطق را طوری اصلاح کردم که توپ به دیوار نچسبد
                if (newX < ballRadius + 25f) newX = ballRadius + 25f
                if (newX > 1080f - ballRadius - 25f) newX = 1080f - ballRadius - 25f
                if (newY < ballRadius + 25f) newY = ballRadius + 25f
                if (newY > 1920f - ballRadius - 150f) newY = 1920f - ballRadius - 150f

                ballPos = Offset(newX, newY)

                // چک کردن سقوط در چاله‌های سیاه (باخت)
                holes.forEach { hole ->
                    if ((ballPos - hole).getDistance() < holeRadius - 10f) isGameOver = true
                }

                // چک کردن هدف (برد)
                if ((ballPos - goalPos).getDistance() < 40f) isFalling = true
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFD7CCC8))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // ۱. قاب دور صفحه (دیوار محافظ)
                drawRect(color = Color(0xFF5D4037), style = Stroke(width = 50f))

                // ۲. رسم چاله‌های مانع
                holes.forEach { hole ->
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color.Black, Color(0xFF212121))),
                        radius = holeRadius,
                        center = hole
                    )
                }

                // ۳. رسم چاله هدف (جذاب‌تر)
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFF1B5E20), Color.Black)),
                    radius = holeRadius + 10f,
                    center = goalPos
                )
                drawCircle(color = Color.White.copy(0.3f), radius = holeRadius, center = goalPos, style = Stroke(width = 4f))

                // ۴. رسم توپ با افکت سقوط
                scale(ballScale, ballPos) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFF9E9E9E), Color(0xFF424242)),
                            center = Offset(ballPos.x - 8f, ballPos.y - 8f),
                            radius = ballRadius * 1.5f
                        ),
                        radius = ballRadius,
                        center = ballPos
                    )
                }
            }

            // رابط کاربری (تایمر و لول)
            Row(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("LEVEL: $currentLevel", color = Color(0xFF5D4037), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("TIME: $timeLeft", color = if (timeLeft < 10) Color.Red else Color(0xFF5D4037), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }

            // صفحه باخت
            if (isGameOver) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.85f)), contentAlignment = Alignment.Center) {
                    Card(shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (timeLeft == 0) "TIME OUT!" else "HOLE IN!", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = { onLose() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))) {
                                Text("TRY AGAIN")
                            }
                        }
                    }
                }
            }
        }
    }
}
