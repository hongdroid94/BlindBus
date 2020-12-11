package com.salus.blindbus.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.salus.blindbus.databinding.ActivitySplashBinding
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

        Timer().schedule(object : TimerTask() {
            override fun run() {
                // TODO: 추후 자동로그인 구현 시 분기를 줘서 Login 으로 갈지 Main 으로 바로 갈지 처리필요
                val mainIntent = Intent(this@SplashAct, LoginAct::class.java)
                startActivity(mainIntent)
                finish()
            }
        }, 1500)
    }
}