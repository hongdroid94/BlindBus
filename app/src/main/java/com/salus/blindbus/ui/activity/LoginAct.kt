package com.salus.blindbus.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.salus.blindbus.R
import com.salus.blindbus.database.api.MainApi
import com.salus.blindbus.databinding.ActivityLoginBinding
import com.salus.blindbus.database.model.ResponseModel
import com.salus.blindbus.util.SharedManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 로그인 화면
 */
class LoginAct : AppCompatActivity() {

    lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 (View Binding)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setInitialize(binding)
    }

    private fun setInitialize(binding: ActivityLoginBinding) {
        SharedManager.init(applicationContext)

        binding.apply {
            // 회원가입
            tvRegister.setOnClickListener {
                val intent = Intent(this@LoginAct, RegisterAct::class.java)
                startActivity(intent)
            }
            // 로그인
            btnLogin.setOnClickListener {
                val strId : String = etId.text.toString()
                val strPw : String = etPwd.text.toString()
                // check empty input field
                if (strId.isEmpty() || strPw.isEmpty()) {
                    Toast.makeText(this@LoginAct, getString(R.string.EMPTY_INPUT_FIELD), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // check email type pattern
                if(!isCheckEmail(strId)) {
                    Toast.makeText(this@LoginAct, getString(R.string.NOT_MATCHED_EMAIL_TYPE), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val loginMap : HashMap<String, String> = HashMap()
                loginMap["userID"] = etId.text.toString()
                loginMap["userPassword"] = etPwd.text.toString()
                compositeDisposable = CompositeDisposable()
                compositeDisposable.add(
                    MainApi.postLogin(loginMap)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.newThread())
                        .subscribe({ response: ResponseModel ->

                            if (response.success) {
                                // save local DB for user account info

                                // set auto login
                                if(chkAutoLogin.isChecked)
                                    SharedManager.write(SharedManager.AUTO_LOGIN, true)

                                SharedManager.write(SharedManager.USER_NAME, response.userName)

                                Toast.makeText(this@LoginAct, "환영합니다 ${response.userName} 님 !", Toast.LENGTH_SHORT).show()
                                val loginIntent = Intent(this@LoginAct, MainAct::class.java)
                                startActivity(loginIntent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginAct, getString(R.string.INVALID_LOGIN_INFO), Toast.LENGTH_SHORT).show()
                            }
//                        Toast.makeText(this@RegisterAct, "Response ${response.success} ${response.userID} ${response.userPassword}", Toast.LENGTH_SHORT).show()
                        }, { error: Throwable ->
                            Log.d("RegisterAct", error.localizedMessage)
                            Toast.makeText(this@LoginAct, "Error ${error.localizedMessage}", Toast.LENGTH_SHORT
                            ).show()
                        })
                )
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