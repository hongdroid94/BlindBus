package com.salus.blindbus.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.*
import com.minew.beacon.MinewBeacon
import com.minew.beacon.MinewBeaconManager
import com.salus.blindbus.database.model.UserRssi
import com.salus.blindbus.ui.activity.MainAct
import com.salus.blindbus.ui.activity.MainAct.Companion.BUS_CATCH_MODE
import java.util.*

class BeaconService : Service() {
    companion object {

        /* atansio - 테스트를 위한 주석처리 */
        const val SERVICE_CHANNEL_ID = "ServiceChannelID"
        const val FORE_GROUND_SERVICE_ID = 1919

    }






    private val localBinder = LocalBinder()
    var mMinewBeaconManager: MinewBeaconManager? = null
    val comp = UserRssi()

    val myCurrentBusList = mutableListOf<MinewBeacon>()
    val myCancelBusUUIDList = mutableListOf<String>()
    var finishCheckList:MutableList<Boolean> = mutableListOf()

    var trackingModeUUID:String? = null
    // TODO: 버스도착정보 API를 연동해서 각 정류장에 들어오는 버스의 비콘 UUID를 자동으로 자료구조에 담도록 수정해야 함. ( 현재 하드코딩 상태 )
    var busUUIDBeaconList = mutableListOf("E2C56DB5-DFFB-48D2-B060-D0F5A71096E0") // 로너 비콘
//    var busUUIDBeaconList = mutableListOf("74278BDA-B644-4520-8F0C-720EAF059935") // 아타 비콘1 (Blind_1)
//    var busUUIDBeaconList = mutableListOf("AB8190D5-D11E-4941-ACC4-42F30510B408") // 아타 비콘2 (Blind_2)

    var SSTReturnValue:String? = null
    var currentBusMode:Int?= BUS_CATCH_MODE
    lateinit var vib: Vibrator



    fun setInitService() {
        val intent = Intent(this@BeaconService, MainAct::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this@BeaconService, SERVICE_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_dialer)
                .setContentTitle("비콘 확인중입니다")
                .setContentText("비콘 테스트")
                .setContentIntent(pendingIntent)
                .build()

        startForeground(FORE_GROUND_SERVICE_ID, builder)
    }

    override fun onBind(p0: Intent?) = localBinder

    inner class LocalBinder : Binder() {
        fun getService(): BeaconService {
            return this@BeaconService
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMinewBeaconManager?.stopScan()
    }
}