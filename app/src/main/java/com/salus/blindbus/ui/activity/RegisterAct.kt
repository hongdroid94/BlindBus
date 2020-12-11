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
        setInitialize(binding)
    }

    private fun setInitialize(binding: ActivityRegisterBinding) {
        // 회원가입 버튼
        binding.apply {
            ivRegister.setOnClickListener {

                if (etId.text?.isEmpty() == true || etPwd.text?.isEmpty() == true) {
                    Toast.makeText(this@RegisterAct, "입력되지 않는 필드가 존재합니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val registerMap : HashMap<String, String> = HashMap()
                registerMap["userID"] = etId.text.toString()
                registerMap["userPassword"] = etPwd.text.toString()
                registerMap["userName"] = etName.text.toString()
                registerMap["emailVerify"] = ""
                compositeDisposable = CompositeDisposable()
                compositeDisposable.add(MainApi.postRegister(registerMap)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe({ response: ResponseModel ->

                        if(response.success) {
                            Toast.makeText(this@RegisterAct, "회원가입이 정상적으로 처리 되었습니다\n로그인을 진행해주세요", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        else {
                            Toast.makeText(this@RegisterAct, "회원가입 과정에서 문제가 발생되었습니다\n잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                        }
//                        Toast.makeText(this@RegisterAct, "Response ${response.success} ${response.userID} ${response.userPassword}", Toast.LENGTH_SHORT).show()
                    }, { error: Throwable ->
                        Log.d("RegisterAct", error.localizedMessage)
                        Toast.makeText(this@RegisterAct, "Error ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }))
            }
        }
    }
}