package com.salus.blindbus.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.salus.blindbus.database.api.MainApi
import com.salus.blindbus.databinding.ActivityRegisterBinding
import com.salus.blindbus.database.model.ResponseModel

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Matcher
import java.util.regex.Pattern

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
                val strId : String = etId.text.toString()
                val strPwd : String = etPwd.text.toString()
                if (strId.isEmpty() || strPwd.isEmpty()) {
                    Toast.makeText(this@RegisterAct,
                        "입력되지 않는 필드가 존재합니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // check email type pattern
                if(!isCheckEmail(strId)) {
                    Toast.makeText(this@RegisterAct,
                        "이메일 형식에 맞지 않습니다.", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@RegisterAct,
                                "회원가입이 정상적으로 처리 되었습니다\n로그인을 진행해주세요",
                                Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        else {
                            Toast.makeText(this@RegisterAct,
                                "회원가입 과정에서 문제가 발생되었습니다\n잠시 후 다시 시도해주세요",
                                Toast.LENGTH_SHORT).show()
                        }
//                        Toast.makeText(this@RegisterAct, "Response ${response.success} ${response.userID} ${response.userPassword}", Toast.LENGTH_SHORT).show()
                    }, { error: Throwable ->
                        Log.d("RegisterAct", error.localizedMessage)
                        Toast.makeText(this@RegisterAct, "Error ${error.localizedMessage}",
                            Toast.LENGTH_SHORT).show()
                    }))
            }
        }
    }

    /**
     * 이메일 형식 유효성 검사
     */
    fun isCheckEmail(email: String?): Boolean {
        var returnValue = false
        val regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$"
        val p: Pattern = Pattern.compile(regex)
        val m: Matcher = p.matcher(email)
        if (m.matches()) {
            returnValue = true
        }
        return returnValue
    }
}