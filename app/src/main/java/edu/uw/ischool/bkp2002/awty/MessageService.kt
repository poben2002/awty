package edu.uw.ischool.bkp2002.awty

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import android.view.View
import java.util.Timer
import java.util.TimerTask

class MessageService : Service() {
    private var timer: Timer? = null
    private lateinit var inflater: LayoutInflater

    override fun onCreate() {
        super.onCreate()
        inflater = LayoutInflater.from(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val message = intent.getStringExtra("message") ?: "Are we there yet?"
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        val timeInterval = intent.getIntExtra("time", 1)

        val formattedPhoneNumber = formatPhoneNumber(phoneNumber)
        if (formattedPhoneNumber == "Invalid number") {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val timeInMillis = timeInterval * 60000L

        initializeTimer(timeInMillis, message, formattedPhoneNumber)

        return START_STICKY
    }

    private fun initializeTimer(timeInMillis: Long, message: String, formattedPhoneNumber: String) {
        timer?.cancel()
        timer = Timer()

        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    showCustomToast(formattedPhoneNumber, message)
                }
            }
        }, 0, timeInMillis)
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        val digits = phoneNumber.filter { it.isDigit() }
        return if (digits.length == 10) {
            "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
        } else {
            "Invalid number"
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    private fun showCustomToast(phoneNumber: String, message: String) {
        val layout = inflater.inflate(R.layout.custom_toast, null)

        val caption = layout.findViewById<TextView>(R.id.toastCaption)
        val body = layout.findViewById<TextView>(R.id.toastBody)

        caption.text = getString(R.string.texting_caption, phoneNumber)
        body.text = message

        showToast(layout)
    }

    private fun showToast(layout: View) {
        Toast(applicationContext).apply {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }
}
