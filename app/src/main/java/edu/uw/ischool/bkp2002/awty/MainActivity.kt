package edu.uw.ischool.bkp2002.awty
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

private val PERMISSION_REQUEST_SEND_SMS = 123
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
        checkAndRequestPermission()
    }


    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    PERMISSION_REQUEST_SEND_SMS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
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

