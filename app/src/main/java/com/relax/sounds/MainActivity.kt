package com.relax.sounds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

// مدل داده برای آهنگ‌های یوتیوب
data class YTTrack(val title: String, val author: String, val videoId: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                YouTubeMusicApp()
            }
        }
    }

    @Composable
    fun YouTubeMusicApp() {
        var searchQuery by remember { mutableStateOf("") }
        var isPlaying by remember { mutableStateOf(false) }
        var selectedTrack by remember { mutableStateOf<YTTrack?>(null) }
        var ytPlayer by remember { mutableStateOf<YouTubePlayer?>(null) }

        // لیست آهنگ‌های واقعی و فعال یوتیوب برای تست اولیه
        val tracks = remember(searchQuery) {
            if (searchQuery.isEmpty()) {
                listOf(
                    YTTrack("Weightless (Original Case)", "Marconi Union", "UfcAVejslrU"),
                    YTTrack("Deep Sleep Piano", "Yellow Brick Cinema", "lFcSrYw-ARY"),
                    YTTrack("Rain on Window", "Nature Sounds", "mPZkdNFqeps")
                )
            } else {
                // شبیه‌سازی نتایج بر اساس تایپ شما
                listOf(
                    YTTrack("$searchQuery - Relax Mix", "YouTube Music", "670fN_8Vyn0"),
                    YTTrack("$searchQuery - Calm Piano", "Relax Studio", "m1MYLge2zqc"),
                    YTTrack("$searchQuery - Study Focus", "Lofi Girl", "jfKfPfyJRdk")
                )
            }
        }

        // پس‌زمینه گرادینت تیره و شیک
        val bgGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF141E30), Color(0xFF243B55))
        )

        Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
            
            // بخش فنی: پلیر یوتیوب (مخفی برای پخش فقط صدا)
            AndroidView(
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                ytPlayer = youTubePlayer
                            }
                        })
                    }
                },
                modifier = Modifier.size(1.dp).alpha(0f)
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Text("YouTube Player", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(25.dp))

                // باکس جستجوی شیشه‌ای
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search on YouTube...", color = Color.White.copy(0.4f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(0.08f)),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // لیست آهنگ‌ها با قابلیت اسکرول
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tracks) { track ->
                        Surface(
                            onClick = { 
                                selectedTrack = track
                                isPlaying = true
                                ytPlayer?.loadVideo(track.videoId, 0f) 
                            },
                            color = if (selectedTrack == track) Color.White.copy(0.15f) else Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (selectedTrack == track) BorderStroke(1.dp, Color.White.copy(0.3f)) else null
                        ) {
                            Row(modifier = Modifier.padding(15.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(Color.White.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                    Text("🎵", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(15.dp))
                                Column {
                                    Text(track.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text(track.author, color = Color.White.copy(0.5f), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // کنترلر پخش (بدون هاله نارنجی و کاملاً شیشه‌ای)
                if (selectedTrack != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                            .clip(RoundedCornerShape(42.dp))
                            .background(Color.White.copy(0.1f))
                            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(42.dp)),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏮", color = Color.White, fontSize = 26.sp)
                        
                        Box(
                            modifier = Modifier
                                .size(65.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.12f))
                                .clickable { 
                                    if (isPlaying) ytPlayer?.pause() else ytPlayer?.play()
                                    isPlaying = !isPlaying 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isPlaying) "⏸" else "▶", color = Color.White, fontSize = 32.sp)
                        }

                        Text("⏭", color = Color.White, fontSize = 26.sp)
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = if (selectedTrack != null) "Playing: ${selectedTrack?.title}" else "Developed by HsH. © 2025",
                    color = Color.White.copy(0.3f),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
