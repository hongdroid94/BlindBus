package com.salus.blindbus.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.salus.blindbus.databinding.ActivityMainBinding

/**
 * 메인 화면
 */

class MainAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 (View Binding)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.tvWelcome.text = "hello hongdroid"
        setContentView(binding.root)

    }
}