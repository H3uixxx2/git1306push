package com.example.projectorgit.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.projectorgit.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SettingsDialogFragment : DialogFragment() {

    private lateinit var tenTxt: TextView
    private lateinit var sdtTxt: TextView
    private lateinit var maThietBiTxt: EditText
    private lateinit var tenThietBiTxt: EditText
    private lateinit var trangThaiTxt: EditText
    private lateinit var btnConnect: Button
    private lateinit var btnDialogExit: Button
    private val client = OkHttpClient()
    private val sTD = "SettingsDialogFragment"

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_settings, container, false)

        // Initialize views
        tenTxt = view.findViewById(R.id.tenTxt)
        sdtTxt = view.findViewById(R.id.sdttTxt)
        maThietBiTxt = view.findViewById(R.id.mathietbiTxt)
        tenThietBiTxt = view.findViewById(R.id.tenthietbiTxt)
        trangThaiTxt = view.findViewById(R.id.trangthaiTxt)

        btnConnect = view.findViewById(R.id.btnConnect)
        btnDialogExit = view.findViewById(R.id.btnClose)

        btnConnect.setOnClickListener {
            showConnectConfirmationDialog()
        }

        btnDialogExit.setOnClickListener {
            dismiss()
        }

        // Retrieve user ID from SharedPreferences
        val sharedPref = activity?.getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        val userId = sharedPref?.getString("USER_ID", null)

        if (userId != null) {
            fetchUserInfo(userId)
        }

        // Retrieve device information
        fetchDeviceInfo()

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun fetchUserInfo(userId: String) {
        Log.d(sTD, "Fetching user info for ID: $userId")
        val request = Request.Builder()
            .url("https://api6789.web5sao.net/home/GetInfoCustomer_ById/$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(sTD, "Fetch user info failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(sTD, responseBody ?: "No response")
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val userList = jsonResponse.getJSONArray("userList")
                    if (userList.length() > 0) {
                        val userInfo = userList.getJSONObject(0)
                        val name = userInfo.optString("customer_name", "")
                        val phone = userInfo.optString("phone_number", "")

                        activity?.runOnUiThread {
                            tenTxt.text = name
                            sdtTxt.text = phone
                            Log.d(sTD, "User info updated: name=$name, phone=$phone")
                        }
                    }
                } else {
                    Log.e(sTD, "Response not successful")
                }
            }
        })
    }

    private fun fetchDeviceInfo() {
        val deviceId = getDeviceId(requireContext())
        val deviceName = getDeviceName()

        maThietBiTxt.setText(deviceId)
        tenThietBiTxt.setText(deviceName)
        Log.d(sTD, "Device info fetched: deviceId=$deviceId, deviceName=$deviceName")

        // Check connection status from SharedPreferences
        val connected = isDeviceConnectedToUser(deviceId)
        updateConnectionStatus(connected)

        // Update connection status every 5 seconds
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val isConnected = isDeviceConnectedToUser(deviceId)
                updateConnectionStatus(isConnected)
                handler.postDelayed(this, 5000) // Run again after 5 seconds
                Log.d(sTD, "Connection status updated: isConnected=$isConnected")
            }
        }
        handler.post(runnable)
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        trangThaiTxt.setText(if (isConnected) "Đang kết nối" else "Không kết nối")
        trangThaiTxt.setTextColor(
            if (isConnected) ContextCompat.getColor(requireContext(), R.color.green)
            else ContextCompat.getColor(requireContext(), R.color.red)
        )
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    private fun isDeviceConnectedToUser(deviceId: String): Boolean {
        val sharedPref = activity?.getSharedPreferences("DEVICE_PREF", Context.MODE_PRIVATE)
        val storedDeviceId = sharedPref?.getString("DEVICE_ID", null)
        val storedUserId = sharedPref?.getString("USER_ID", null)
        val currentUserId = activity?.getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)?.getString("USER_ID", null)
        return deviceId == storedDeviceId && storedUserId != null && storedUserId != currentUserId
    }

    private fun showConnectConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận kết nối")
            .setMessage("Bạn có chắc chắn muốn kết nối mã thiết bị với ID người dùng không?")
            .setPositiveButton("Kết nối") { dialog, _ ->
                handleConnect()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy bỏ") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun handleConnect() {
        // Retrieve user ID and name from SharedPreferences
        val sharedPref = activity?.getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        val userId = sharedPref?.getString("USER_ID", null) ?: return
        val userName = sharedPref.getString("USER_NAME", null) ?: return

        val deviceId = maThietBiTxt.text.toString()

        // Check if the device is already connected to another user
        val isAlreadyConnected = isDeviceConnectedToUser(deviceId)
        if (isAlreadyConnected) {
            val ownerName = getDeviceOwnerName()
            activity?.runOnUiThread {
                showAlreadyConnectedDialog(ownerName)
            }
        } else {
            // Save device information and user ID to SharedPreferences
            saveDeviceInfo(userId, userName, deviceId)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun saveDeviceInfo(userId: String, userName: String, deviceId: String) {
        val sharedPref = activity?.getSharedPreferences("DEVICE_PREF", Context.MODE_PRIVATE)
        with(sharedPref?.edit()) {
            this?.putString("USER_ID", userId)
            this?.putString("USER_NAME", userName)
            this?.putString("DEVICE_ID", deviceId)
            this?.apply()
        }
        activity?.runOnUiThread {
            btnConnect.isEnabled = false
            updateConnectionStatus(true)
            Toast.makeText(requireContext(), "Kết nối thành công", Toast.LENGTH_SHORT).show()
            AlertDialog.Builder(requireContext())
                .setTitle("Thông báo")
                .setMessage("Thiết bị đã được kết nối thành công với tài khoản của bạn.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    private fun showAlreadyConnectedDialog(ownerName: String?) {
        val message = if (ownerName != null) {
            "Thiết bị này đã được kết nối với tài khoản $ownerName."
        } else {
            "Thiết bị này đã được kết nối với một người dùng khác."
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Kết nối thất bại")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun getDeviceOwnerName(): String? {
        val sharedPref = activity?.getSharedPreferences("DEVICE_PREF", Context.MODE_PRIVATE)
        return sharedPref?.getString("USER_NAME", null)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable) // Stop updating connection status
    }
}
