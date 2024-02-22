package edu.uw.ischool.bkp2002.awty

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Timer
import java.util.TimerTask

class MessageService : Service() {
    private var timer: Timer? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val message = intent.getStringExtra("message") ?: "Are we there yet?"
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        val timeInterval = intent.getIntExtra("time", 1)

        val formattedPhoneNumber = formatPhoneNumber(phoneNumber)
        if (formattedPhoneNumber == "Invalid phone number") {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val timeInMillis = timeInterval * 60000L

        // Check if SEND_SMS permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, we cannot send SMS, so stop the service
            Toast.makeText(this, "SEND_SMS permission is required to send SMS messages.", Toast.LENGTH_SHORT).show()
            stopSelf(startId)
        } else {
            // Permission is granted, start sending SMS
            startSendingSms(formattedPhoneNumber, message, timeInMillis)
        }

        return START_STICKY
    }

    private fun startSendingSms(phoneNumber: String, message: String, intervalMillis: Long) {
        timer?.cancel()
        timer = Timer()

        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    sendSmsMessage(phoneNumber, message)
                }
            }
        }, 0, intervalMillis)
    }

    private fun sendSmsMessage(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        val digits = phoneNumber.filter { it.isDigit() }
        return if (digits.length == 10) {
            "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
        } else {
            "Invalid phone number"
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()

        // Check SEND_SMS permission at service creation
        checkSendSmsPermission()
    }

    private fun checkSendSmsPermission() {
        val permissionStatus = if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            == PackageManager.PERMISSION_GRANTED) {
            "granted"
        } else {
            "not granted"
        }
        Toast.makeText(this, "SEND_SMS permission $permissionStatus.", Toast.LENGTH_SHORT).show()
    }

}
