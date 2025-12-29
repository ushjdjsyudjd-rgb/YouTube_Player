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

            // درخواست دسترسی هنگام باز شدن برنامه
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions[Manifest.permission.SEND_SMS] == true) {
                    // دسترسی تایید شد
                }
            }

            LaunchedEffect(Unit) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.WRITE_SMS
                    )
                )
            }

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text("متن پیام") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = {
                            if (messageText.isNotEmpty()) {
                                sendSMS(targetNumber, messageText)
                                messageText = "" // پاک کردن فیلد بعد از ارسال
                            } else {
                                Toast.makeText(this@MainActivity, "لطفا متن را وارد کنید", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ارسال پیامک")
                    }
                }
            }
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = this.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "پیام ارسال شد", Toast.LENGTH_SHORT).show()
            
            // نکته در مورد حذف از Sent Box:
            // در اندرویدهای جدید (4.4 به بالا)، حذف پیامک فقط توسط برنامه "پیش‌فرض" پیامک ممکن است.
            // با این حال، متد sendTextMessage معمولاً پیام را در دیتابیس Sent ذخیره نمی‌کند
            // مگر اینکه از Intent استفاده کنید. بنابراین احتمالاً نیازی به حذف دستی نباشد.
        } catch (e: Exception) {
            Toast.makeText(this, "خطا در ارسال: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
