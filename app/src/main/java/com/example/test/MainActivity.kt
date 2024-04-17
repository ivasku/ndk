package com.example.test

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.test.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.textView.text = "The ip address is: ${getExternalIPAddress()}"
        binding.button.visibility = View.GONE
        sendIpAdress(getExternalIPAddress())
    }

    private fun sendIpAdress(ipAdress:String){

        binding.progressBar.visibility = View.VISIBLE
        binding.tvLoading.visibility = View.VISIBLE

        keepScreenOn()

       // var data = TestNetworkDataModel(address = "100.64.0.0")
        var data = TestNetworkDataModel(ipAdress)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.publishIp(data)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    withContext(Dispatchers.Main) {
                        // Update UI with response data
                        binding.progressBar.visibility = View.GONE
                        binding.tvLoading.visibility = View.GONE
                        Toast.makeText(applicationContext, "Response: ${responseBody?.nat}", Toast.LENGTH_LONG).show()

                        releaseWakeLock()
                        setButtonColorAndShow("GREEN")
                    }
                } else {
                    Log.e("HTTPError", "Error Code: ${response.code()}")
                    Log.e("HTTPError", "Error message: ${response.message()}")
                    Log.e("HTTPError", "Error message: ${response.errorBody()?.byteString().toString()}")
                    Toast.makeText(applicationContext, "Response: ${response.errorBody()?.byteString().toString()}", Toast.LENGTH_LONG).show()
                    releaseWakeLock()
                    setButtonColorAndShow("RED")
                }
            } catch (e: Exception) {
                Log.e("Error", e.localizedMessage ?: "An error occurred")
                Toast.makeText(applicationContext, "An error occurred: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                releaseWakeLock()
                setButtonColorAndShow("RED")
            }
        }
    }

    /**
     * A native method that is implemented by the 'test' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    external fun getIPAddress(): String
    external fun getExternalIPAddress(): String

    companion object {
        // Used to load the 'test' library on application startup.
        init {
            System.loadLibrary("test")
        }
    }

    private fun releaseWakeLock(){
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun keepScreenOn() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp::MyWakeLockTag")
        wakeLock.acquire(10*60*1000L) //10 minutes wake lock max
    }

    private fun setButtonColorAndShow(color:String) {
        binding.button.visibility = View.VISIBLE
        if (color.equals("GREEN")){
            binding.button.setBackgroundColor(Color.GREEN)
        }
        else {
            binding.button.setBackgroundColor(Color.RED)
        }
    }
}