package com.salus.blindbus.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

/**
 * Shared Preferences 를 간편하게 사용할 수 있는 유틸 클래스
 */

class SharedManager {
    private var mSharedPref: SharedPreferences? = null
    val AUTO_LOGIN = "AUTO_LOGIN"                           // 자동 로그인 여부

    private fun SharedManager() {}

    fun init(context: Context) {
        if (mSharedPref == null) mSharedPref =
            context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
    }

    fun read(key: String?, defValue: String?): String? {
        return mSharedPref!!.getString(key, defValue)
    }

    fun write(key: String?, value: String?) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putString(key, value)
        prefsEditor.apply()
    }

    fun read(key: String?, defValue: Boolean): Boolean {
        return mSharedPref!!.getBoolean(key, defValue)
    }

    fun write(key: String?, value: Boolean) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putBoolean(key, value)
        prefsEditor.apply()
    }

    fun read(key: String?, defValue: Int): Int? {
        return mSharedPref!!.getInt(key, defValue)
    }

    fun write(key: String?, value: Int?) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putInt(key, value!!).apply()
    }

    fun read(key: String?, defValue: Long): Long? {
        return mSharedPref!!.getLong(key, defValue)
    }

    fun write(key: String?, value: Long?) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putLong(key, value!!).apply()
    }

    fun clear() {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.clear()
        prefsEditor.apply()
    }
}