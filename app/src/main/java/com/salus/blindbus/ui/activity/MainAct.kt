package com.salus.blindbus.ui.activity

import android.os.Bundle
import android.view.View
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
        //TODO : 비콘이 스캐닝 콜백이 완성 되었을 때 Visible 처리 필요
        binding.apply {
            frameCompleteScan.visibility = View.INVISIBLE
            tvGuideMsg.visibility = View.VISIBLE
        }
    }
}