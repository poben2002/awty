package edu.uw.ischool.bkp2002.awty

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var startStopButton: Button
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupStartStopButton()
    }

    private fun initializeViews() {
        messageEditText = findViewById(R.id.editTextMessage)
        phoneNumberEditText = findViewById(R.id.editTextPhoneNumber)
        timeEditText = findViewById(R.id.editTextTime)
        startStopButton = findViewById(R.id.buttonStartStop)
    }

    private fun setupStartStopButton() {
        startStopButton.setOnClickListener {
            val message = messageEditText.text.toString()
            val phoneNumber = phoneNumberEditText.text.toString()
            val timeString = timeEditText.text.toString()
            val time = timeString.toIntOrNull()

            when {
                message.isEmpty() -> showToast("Enter a message.")
                phoneNumber.isEmpty() -> showToast("Enter a phone number.")
                !isPhoneNumberValid(phoneNumber) -> showToast("Enter a valid 10-digit phone number.")
                timeString.isEmpty() -> showToast("Enter the time interval.")
                time == null || time <= 0 -> showToast("Time interval must be a positive integer.")
                isServiceRunning -> stopService()
                else -> startService(message, phoneNumber, time)
            }
        }
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        val digits = phoneNumber.filter { it.isDigit() }
        return digits.length == 10
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun startService(message: String, phoneNumber: String, time: Int) {
        val intent = Intent(this, MessageService::class.java).apply {
            putExtra("msg", message)
            putExtra("phoneNumber", phoneNumber)
            putExtra("time", time)
        }
        startService(intent)
        updateUIForServiceState(true)
    }

    private fun stopService() {
        stopService(Intent(this, MessageService::class.java))
        updateUIForServiceState(false)
    }

    private fun updateUIForServiceState(running: Boolean) {
        startStopButton.text = if (running) getString(R.string.stop) else getString(R.string.start)
        isServiceRunning = running
    }
}
