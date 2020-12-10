package com.salus.blindbus.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.salus.blindbus.databinding.ActivityLoginBinding

/**
 * 로그인 화면
 */
class LoginAct : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 (View Binding)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            tvRegister.setOnClickListener {
                val intent = Intent(this@LoginAct, RegisterAct::class.java)
                startActivity(intent)
                finish()
            }
        }


    }
}