package com.salus.blindbus.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.salus.blindbus.databinding.ActivitySplashBinding
import com.salus.blindbus.util.SharedManager
import java.util.*

/**
 * 시작 화면
 */
class SplashAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 (View Binding)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setInitialize(binding)
    }

    private fun setInitialize(binding: ActivitySplashBinding) {
        SharedManager.init(applicationContext)

        // check enabled auto login
        if (SharedManager.read(SharedManager.AUTO_LOGIN, false)) {
            val mainIntent = Intent(this@SplashAct, MainAct::class.java)
            startActivity(mainIntent)
            finish()
        } else {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    val mainIntent = Intent(this@SplashAct, LoginAct::class.java)
                    startActivity(mainIntent)
                    finish()
                }
            }, 1500)
        }

    }
}