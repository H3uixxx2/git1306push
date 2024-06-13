@file:Suppress("DEPRECATION")

package com.example.projectorgit.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.projectorgit.R
import com.example.projectorgit.main.MainActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {

    private lateinit var sdtTxt: TextInputEditText
    private lateinit var mkTxt: TextInputEditText
    private val client = OkHttpClient()
    private val fFF = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sdtTxt = findViewById(R.id.sdtTxt)
        mkTxt = findViewById(R.id.mkTxt)

        // Kiểm tra token khi khởi động ứng dụng
        val sharedPref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        val token = sharedPref.getString("TOKEN", null)
        Log.d(fFF, "Retrieved token on startup: $token")
        if (!token.isNullOrEmpty()) {
            checkToken(token)
        }

        findViewById<AppCompatButton>(R.id.btnLogin).setOnClickListener {
            if (isNetworkAvailable()) {
                login()
            } else {
                Toast.makeText(this, "Không có kết nối internet. Vui lòng kiểm tra kết nối mạng và thử lại.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<AppCompatButton>(R.id.btnOut).setOnClickListener {
            finishAffinity()
            exitProcess(0)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    private fun login() {
        val phone = sdtTxt.text.toString().trim()
        val password = mkTxt.text.toString().trim()

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại và mật khẩu", Toast.LENGTH_SHORT).show()
            return
        }

        // MD5 hashing
        val md5Password = getMD5Hash(password)

        val formBody = FormBody.Builder()
            .add("email", phone)
            .add("password", md5Password)
            .build()

        val request = Request.Builder()
            .url("https://api6789.web5sao.net/home/login")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(fFF, "Login request failed", e)
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Đăng nhập thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(fFF, responseBody ?: "No response")
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val status = jsonResponse.optInt("status", 0)
                    val msg = jsonResponse.optString("msg", "Unknown error")
                    val info = jsonResponse.optJSONArray("info")
                    val userId = info?.getJSONObject(0)?.optString("customer_id", "")
                    val userName = info?.getJSONObject(0)?.optString("customer_name", "")
                    val customerToken = info?.getJSONObject(0)?.optString("customer_token", "")
                    val userEmail = info?.getJSONObject(0)?.optString("email", "")

                    runOnUiThread {
                        if (status == 1 && userId != null) {
                            Toast.makeText(this@LoginActivity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                            // Lưu ID và token người dùng vào SharedPreferences
                            val sharedPref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("USER_ID", userId)
                                putString("USER_NAME", userName)
                                putString("TOKEN", customerToken)
                                putString("EMAIL", userEmail)
                                apply()
                            }

                            Log.d(fFF, "Login successful: userId=$userId, userName=$userName, token=$customerToken, email=$userEmail")

                            // Lấy mã thiết bị và lưu vào SharedPreferences
                            val deviceId = getDeviceId(this@LoginActivity)
                            saveDeviceIdToSharedPreferences(deviceId)

                            // Chuyển sang activity khác nếu đăng nhập thành công
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Log.d(fFF, "Login failed: $msg")
                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Đăng nhập thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun checkToken(token: String) {
        Log.d(fFF, "Checking token: $token")
        // Gọi API kiểm tra token
        val request = Request.Builder()
            .url("https://api6789.web5sao.net/home/GetInfoCustomer_ByToken/$token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(fFF, "Token check failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(fFF, responseBody ?: "No response")
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val status = jsonResponse.optInt("status", 0)
                    val info = jsonResponse.optJSONArray("userList")

                    runOnUiThread {
                        if (status == 1 && info != null && info.length() > 0) {
                            val userId = info.getJSONObject(0).optString("customer_id", "")
                            val userName = info.getJSONObject(0).optString("customer_name", "")
                            // Lưu lại ID và tên người dùng nếu cần
                            val sharedPref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("USER_ID", userId)
                                putString("USER_NAME", userName)
                                apply()
                            }

                            Log.d(fFF, "Token valid: userId=$userId, userName=$userName")

                            // Chuyển sang activity khác nếu token hợp lệ
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Token không hợp lệ, yêu cầu đăng nhập lại
                            Log.d(fFF, "Token invalid or expired")
                            Toast.makeText(this@LoginActivity, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.d(fFF, "Token check failed with response: ${response.code}")
                        Toast.makeText(this@LoginActivity, "Kiểm tra token thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun saveDeviceIdToSharedPreferences(deviceId: String) {
        val sharedPref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("DEVICE_ID", deviceId)
            apply()
        }
        Log.d(fFF, "Device ID saved: $deviceId")
    }

    private fun getMD5Hash(plainText: String): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(plainText.toByteArray())
        val bytes = messageDigest.digest()
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}