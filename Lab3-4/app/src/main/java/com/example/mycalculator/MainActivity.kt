package com.example.mycalculator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.mycalculator.PassKey.LoginActivity
import com.example.mycalculator.PassKey.SetupPassKeyActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        if (!prefs.contains("password")) {
            startActivity(Intent(this, SetupPassKeyActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}