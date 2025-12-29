package com.relax.sounds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

data class Song(val title: String, val author: String, val id: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showSplash by remember { mutableStateOf(true) }

            // تایمر برای اسپلش اسکرین (۳ ثانیه)
            LaunchedEffect(Unit) {
                delay(3000)
                showSplash = false
            }

            MaterialTheme {
                if (showSplash) {
                    SplashScreen()
                } else {
                    MusicPlayerScreen()
                }
            }
        }
    }

    @Composable
    fun SplashScreen() {
        val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF1E4597), Color(0xFFD4A373)))
        Box(modifier = Modifier.fillMaxSize().background(bgGradient), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌿", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Text("Music Player", color = Color.White, fontSize = 35.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(100.dp))
                Text("Developed by HsH. © Copyright", color = Color.White.copy(0.7f), fontSize = 12.sp)
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
        val focusManager = LocalFocusManager.current
        val songList = remember { mutableStateListOf<Song>() }

        fun performSearch(query: String) {
            if (query.isEmpty()) return
            focusManager.clearFocus()
            scope.launch {
                val results = withContext(Dispatchers.IO) {
                    try {
                        val url = "https://www.youtube.com/results?search_query=${query.replace(" ", "+")}"
                        val html = URL(url).readText()
                        val regex = "videoRenderer\":\\{\"videoId\":\"(.*?)\".*?\"title\":\\{\"runs\":\\[\\{\"text\":\"(.*?)\"\\}\\]".toRegex()
                        regex.findAll(html).take(15).map {
                            Song(it.groupValues[2], "YouTube Result", it.groupValues[1])
                        }.toList()
                    } catch (e: Exception) { emptyList<Song>() }
                }
                songList.clear()
                songList.addAll(results)
            }
        }

        val mainGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E4597), Color(0xFF2B3A67), Color(0xFF8E6B58), Color(0xFFD4A373))
        )

        Box(modifier = Modifier.fillMaxSize().background(mainGradient)) {
            AndroidView(
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(player: YouTubePlayer) { ytPlayer = player }
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

                // باکس سرچ اصلاح شده
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search music...", color = Color.White.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth().clip(CircleShape).background(Color.White.copy(0.15f)),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
                    shape = CircleShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(15.dp))

                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("آهنگ جدید", "شاد ایرانی", "ریمیکس", "غمگین").forEach { label ->
                        Button(
                            onClick = { searchQuery = label; performSearch(label) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.12f)),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        ) { Text(label, color = Color.White, fontSize = 12.sp) }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(songList) { song ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color.White.copy(0.07f))
                                .clickable { selectedSong = song; isPlaying = true; ytPlayer?.loadVideo(song.id, 0f) }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(45.dp).background(Color.White.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Text("🎵") }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
                                Text(song.author, color = Color.White.copy(0.5f), fontSize = 11.sp)
                            }
                        }
                    }
                }

                // دکمه‌های کنترلر پایین
                if (selectedSong != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).height(100.dp)
                            .clip(RoundedCornerShape(50.dp)).background(Color.White.copy(0.1f))
                            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(50.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                            Text("⏮", color = Color.White, fontSize = 30.sp, modifier = Modifier.clickable { /* Prev logic */ })
                            Surface(
                                modifier = Modifier.size(65.dp).clickable { 
                                    if (isPlaying) ytPlayer?.pause() else ytPlayer?.play()
                                    isPlaying = !isPlaying
                                },
                                shape = CircleShape, color = Color.White.copy(0.2f)
                            ) { Box(contentAlignment = Alignment.Center) { Text(if (isPlaying) "⏸" else "▶", color = Color.White, fontSize = 35.sp) } }
                            Text("⏭", color = Color.White, fontSize = 30.sp, modifier = Modifier.clickable { /* Next logic */ })
                        }
                    }
                }

                Text("Developed by HsH. © Copyright", color = Color.White.copy(0.3f), fontSize = 10.sp, modifier = Modifier.padding(bottom = 10.dp))
            }
        }
    }
}
