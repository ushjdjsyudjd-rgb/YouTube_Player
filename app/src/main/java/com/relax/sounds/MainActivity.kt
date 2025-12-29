package com.relax.sounds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

data class Song(val title: String, val author: String, val id: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MusicPlayerScreen()
            }
        }
    }

    @Composable
    fun MusicPlayerScreen() {
        var searchQuery by remember { mutableStateOf("") }
        var isPlaying by remember { mutableStateOf(false) }
        var selectedSong by remember { mutableStateOf<Song?>(null) }
        var ytPlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
        val scope = rememberCoroutineScope()
        
        // لیست آهنگ‌ها که از سرور (یوتیوب) پر می‌شود
        val songList = remember { mutableStateListOf<Song>() }

        // تابع جستجوی واقعی در یوتیوب
        fun performSearch(query: String) {
            scope.launch {
                val results = withContext(Dispatchers.IO) {
                    try {
                        // این یک ترفند برای دریافت نتایج جستجو بدون نیاز به API Key سنگین است
                        val url = "https://www.youtube.com/results?search_query=${query.replace(" ", "+")}"
                        val html = URL(url).readText()
                        val regex = "videoRenderer\":\\{\"videoId\":\"(.*?)\".*?\"title\":\\{\"runs\":\\[\\{\"text\":\"(.*?)\"\\}\\]".toRegex()
                        regex.findAll(html).take(10).map {
                            Song(it.groupValues[2], "YouTube Result", it.groupValues[1])
                        }.toList()
                    } catch (e: Exception) {
                        emptyList<Song>()
                    }
                }
                songList.clear()
                songList.addAll(results)
            }
        }

        // لود اولیه آهنگ‌های ایرانی
        LaunchedEffect(Unit) { performSearch("Persian Music 2025") }

        val mainGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E4597), Color(0xFF2B3A67), Color(0xFF8E6B58), Color(0xFFD4A373))
        )

        Box(modifier = Modifier.fillMaxSize().background(mainGradient)) {
            // پلیر ۱ پیکسلی کاملاً مخفی
            AndroidView(
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(player: YouTubePlayer) {
                                ytPlayer = player
                            }
                        })
                    }
                },
                modifier = Modifier.size(1.dp).alpha(0f)
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                Text("Music Player", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(20.dp))

                // باکس سرچ با قابلیت اینتر و جستجوی واقعی
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search music...", color = Color.White.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth().clip(CircleShape).background(Color.White.copy(0.1f)),
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = null, 
                            tint = Color.White,
                            modifier = Modifier.clickable { performSearch(searchQuery) }
                        ) 
                    },
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(0.3f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(15.dp))

                // دکمه‌های میانبر (پلی‌لیست ایرانی)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("آهنگ جدید", "شاد ایرانی", "ریمیکس", "غمگین").forEach { label ->
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(0.12f),
                            modifier = Modifier.clickable { 
                                searchQuery = label
                                performSearch(label) 
                            }
                        ) {
                            Text(label, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // نمایش لیست نتایج از سرور
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(songList) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color.White.copy(0.07f))
                                .clickable { 
                                    selectedSong = song
                                    isPlaying = true
                                    ytPlayer?.loadVideo(song.id, 0f)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(45.dp).background(Color.White.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Text("🎵", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
                                Text(song.author, color = Color.White.copy(0.5f), fontSize = 11.sp)
                            }
                        }
                    }
                }

                // کنترلر پخش پایین
                if (selectedSong != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp, top = 10.dp)
                            .height(90.dp)
                            .clip(RoundedCornerShape(45.dp))
                            .background(Color.White.copy(0.1f))
                            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(45.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(35.dp)) {
                            Text("⏮", color = Color.White, fontSize = 28.sp)
                            Surface(
                                modifier = Modifier.size(60.dp).clickable { 
                                    if (isPlaying) ytPlayer?.pause() else ytPlayer?.play()
                                    isPlaying = !isPlaying
                                },
                                shape = CircleShape,
                                color = Color.White.copy(0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(if (isPlaying) "⏸" else "▶", color = Color.White, fontSize = 30.sp)
                                }
                            }
                            Text("⏭", color = Color.White, fontSize = 28.sp)
                        }
                    }
                }
            }
        }
    }
}
