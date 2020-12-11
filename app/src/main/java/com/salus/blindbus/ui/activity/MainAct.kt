package com.salus.blindbus.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.salus.blindbus.databinding.ActivityMainBinding
import com.salus.blindbus.util.SharedManager

/**
 * 메인 화면
 */

class MainAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 (View Binding)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setInitialize(binding)

    }

    private fun setInitialize(binding: ActivityMainBinding) {
        SharedManager.init(applicationContext)
        val strName = SharedManager.read(SharedManager.USER_NAME, "")
        binding.apply {
            if(!strName.equals(""))
                tvWelcome.text = "환영합니다 ${strName} 님 !"
        }
    }
}