package com.example.projectorgit.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.projectorgit.R
import com.example.projectorgit.dialog.CampRunningDialogFragment
import com.example.projectorgit.dialog.SettingsDialogFragment
import com.example.projectorgit.login.LoginActivity

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<AppCompatButton>(R.id.btnCamrunning).setOnClickListener {
            val dialog = CampRunningDialogFragment()
            dialog.show(supportFragmentManager, "CampRunningDialogFragment")
        }

        val (userId, deviceId) = getUserInfoFromSharedPreferences()

        Log.d("MainCheck", "User ID: $userId, Device ID: $deviceId")

        findViewById<AppCompatButton>(R.id.btnSettings).setOnClickListener {
            val settingsDialog = SettingsDialogFragment()
            settingsDialog.show(supportFragmentManager, "SettingsDialogFragment")
        }

        findViewById<AppCompatButton>(R.id.btnLogout).setOnClickListener {
            logout()
        }

        val btnManagement = findViewById<AppCompatButton>(R.id.btnManagement)
        if (isDeviceConnectedToUser(deviceId)) {
            btnManagement.isEnabled = true
            btnManagement.setOnClickListener {
                val intent = Intent(this, ScheduleActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            btnManagement.isEnabled = false
            btnManagement.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        }
    }

    private fun getUserInfoFromSharedPreferences(): Pair<String?, String?> {
        val sharedPref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)
        val deviceId = sharedPref.getString("DEVICE_ID", null)
        return Pair(userId, deviceId)
    }

    private fun isDeviceConnectedToUser(deviceId: String?): Boolean {
        val sharedPref = getSharedPreferences("DEVICE_PREF", Context.MODE_PRIVATE)
        val storedDeviceId = sharedPref.getString("DEVICE_ID", null)
        val storedUserId = sharedPref.getString("USER_ID", null)
        val currentUserId = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE).getString("USER_ID", null)
        return deviceId == storedDeviceId && storedUserId == currentUserId
    }

    private fun logout() {
        Log.d("MainActivity", "Logging out")
        // Xóa token và thông tin người dùng khỏi SharedPreferences
        val sharedPref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("USER_ID")
            remove("USER_NAME")
            remove("TOKEN")
            remove("EMAIL")
            apply()
        }

        // Chuyển về màn hình đăng nhập mà không gọi checkToken
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
