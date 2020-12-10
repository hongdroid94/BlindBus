package com.salus.blindbus.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.salus.blindbus.api.MainApi
import com.salus.blindbus.databinding.ActivityRegisterBinding
import com.salus.blindbus.model.ResponseModel

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * 회원가입 화면
 */

class RegisterAct : AppCompatActivity() {
    lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 (View Binding)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 회원가입 버튼
        binding.apply {
            ivRegister.setOnClickListener {
                val registerMap : HashMap<String, String> = HashMap()
                registerMap["userID"] = binding.etId.text.toString()
                registerMap["userPassword"] = binding.etPwd.text.toString()
                registerMap["emailVerify"] = ""
                compositeDisposable = CompositeDisposable()
                compositeDisposable.add(MainApi.postRegister(registerMap)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe({ response: ResponseModel ->
                        Toast.makeText(this@RegisterAct, "Response ${response.success} ${response.userID} ${response.userPassword}", Toast.LENGTH_SHORT).show()
                    }, { error: Throwable ->
                        Log.d("RegisterAct", error.localizedMessage)
                        Toast.makeText(this@RegisterAct, "Error ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }))
            }
        }
    }
}