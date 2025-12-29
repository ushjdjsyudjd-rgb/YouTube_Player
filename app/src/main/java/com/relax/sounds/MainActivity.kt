package com.relax.sounds

import android.Manifest
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var messageText by remember { mutableStateOf("") }
            val targetNumber = "09036776333"

            // درخواست دسترسی به پیامک هنگام اجرای برنامه
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    Toast.makeText(this, "دسترسی پیامک داده نشد", Toast.LENGTH_SHORT).show()
                }
            }

            LaunchedEffect(Unit) {
                permissionLauncher.launch(Manifest.permission.SEND_SMS)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "ارسال پیام به $targetNumber", style = MaterialTheme.typography.headlineSmall)
                
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("متن پیام را بنویسید") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (messageText.isNotEmpty()) {
                            sendDirectSMS(targetNumber, messageText)
                            messageText = "" // فیلد را بعد از ارسال خالی کن
                        } else {
                            Toast.makeText(this@MainActivity, "پیام خالی است!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ارسال")
                }
            }
        }
    }

    private fun sendDirectSMS(number: String, message: String) {
        try {
            val smsManager: SmsManager = this.getSystemService(SmsManager::class.java)
            // ارسال مستقیم بدون دخالت اپلیکیشن پیامک گوشی
            smsManager.sendTextMessage(number, null, message, null, null)
            Toast.makeText(this, "ارسال شد", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "خطا در ارسال: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
