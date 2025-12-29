package com.relax.sounds

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class Track(val name: String, val resId: Int)

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val trackList = listOf(
            Track("Dark Heart", R.raw.dark_heart),
            Track("Sentimental", R.raw.sentimental),
            Track("Harmony", R.raw.harmony),
            Track("Careful", R.raw.careful),
            Track("Worlds", R.raw.worlds),
            Track("Pure Dream", R.raw.pure_dream),
            Track("For You", R.raw.for_you),
            Track("Thoughtful", R.raw.thoughtful),
            Track("Bread", R.raw.bread),
            Track("Enlivening", R.raw.enlivening)
        )

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) { delay(3000); showSplash = false }

            MaterialTheme {
                Crossfade(targetState = showSplash) { isSplash ->
                    if (isSplash) SplashScreen() else MainPlayerScreen(trackList)
                }
            }
        }
    }

    @Composable
    fun SplashScreen() {
        val infiniteTransition = rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200), repeatMode = RepeatMode.Reverse
            )
        )

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF185A9D)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nature Relax", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold, modifier = Modifier.alpha(alpha))
                Spacer(modifier = Modifier.height(250.dp))
                Text("Developed by HsH. © Copyright", color = Color.White.copy(0.4f), fontSize = 12.sp)
            }
        }
    }

    @Composable
    fun MainPlayerScreen(tracks: List<Track>) {
        var currentIndex by remember { mutableStateOf(0) }
        var isPlaying by remember { mutableStateOf(false) }
        var currentPos by remember { mutableStateOf(0) }
        var duration by remember { mutableStateOf(1) }
        var sleepTimer by remember { mutableStateOf(0) }

        val bgGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF43CEA2), Color(0xFF185A9D), Color(0xFF6A11CB))
        )

        fun playTrack(index: Int) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            currentIndex = (index + tracks.size) % tracks.size
            mediaPlayer = MediaPlayer.create(this@MainActivity, tracks[currentIndex].resId)
            mediaPlayer?.isLooping = true
            duration = mediaPlayer?.duration ?: 1
            mediaPlayer?.start()
            isPlaying = true
        }

        LaunchedEffect(isPlaying, currentIndex) {
            while (isPlaying) {
                currentPos = mediaPlayer?.currentPosition ?: 0
                delay(1000)
            }
        }

        LaunchedEffect(sleepTimer) {
            if (sleepTimer > 0 && isPlaying) {
                delay(sleepTimer * 60000L)
                mediaPlayer?.pause()
                isPlaying = false
                sleepTimer = 0
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
            // نوار زمان عمودی سمت راست
            Column(
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(formatTime(currentPos), color = Color.White, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier.width(4.dp).height(180.dp).clip(CircleShape).background(Color.White.copy(0.2f))
                ) {
                    val progressHeight = (currentPos.toFloat() / duration.toFloat()) * 180f
                    Box(modifier = Modifier.fillMaxWidth().height(progressHeight.dp).background(Color(0xFFFFB347)))
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text("-" + formatTime(duration - currentPos), color = Color.White.copy(0.7f), fontSize = 10.sp)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White.copy(0.2f)), contentAlignment = Alignment.Center) {
                    Text("🌿", fontSize = 30.sp)
                }
                Text("Logoor", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Light)

                Spacer(modifier = Modifier.height(40.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Relax.", color = Color(0xFFFFB347), fontSize = 45.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Breathe.", color = Color.White, fontSize = 45.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Listen.", color = Color.White, fontSize = 45.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(40.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(tracks) { index, track ->
                        Surface(
                            onClick = { playTrack(index) },
                            color = if (currentIndex == index) Color.White.copy(0.35f) else Color.White.copy(0.1f),
                            shape = RoundedCornerShape(25.dp),
                            border = if (currentIndex == index) BorderStroke(1.dp, Color.White.copy(0.5f)) else null
                        ) {
                            Text(track.name, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = Color.White, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))

                // باکس کنترلر (اصلاح شده)
                Row(
                    modifier = Modifier.fillMaxWidth().height(85.dp).clip(RoundedCornerShape(42.dp))
                        .background(Color.White.copy(0.12f)).border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(42.dp)),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // عقب
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).clickable { playTrack(currentIndex - 1) }, contentAlignment = Alignment.Center) {
                        Text("⏮", color = Color.White, fontSize = 28.sp)
                    }
                    
                    // دکمه وسط (فقط شیشه‌ای بدون رنگ نارنجی)
                    Box(
                        modifier = Modifier
                            .size(65.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.15f)) // رنگ شیشه‌ای ثابت
                            .clickable { 
                                if (mediaPlayer == null) {
                                    playTrack(currentIndex)
                                } else {
                                    if (isPlaying) {
                                        mediaPlayer?.pause()
                                        isPlaying = false
                                    } else {
                                        mediaPlayer?.start()
                                        isPlaying = true
                                    }
                                }
                            }, 
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isPlaying) "⏸" else "▶", color = Color.White, fontSize = 34.sp)
                    }

                    // جلو
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).clickable { playTrack(currentIndex + 1) }, contentAlignment = Alignment.Center) {
                        Text("⏭", color = Color.White, fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // دکمه تایمر خواب
                Surface(
                    onClick = { if (sleepTimer < 60) sleepTimer += 10 else sleepTimer = 0 },
                    color = Color(0xFFFFB347).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, Color.White.copy(0.4f)),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(
                        text = "Sleep Timer: ${if(sleepTimer > 0) "$sleepTimer min" else "Off"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Text("Developed by HsH. © Copyright", modifier = Modifier.padding(bottom = 20.dp), color = Color.White.copy(0.3f), fontSize = 11.sp)
            }
        }
    }

    private fun formatTime(ms: Int): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
