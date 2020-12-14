package com.salus.blindbus.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import com.minew.beacon.MinewBeacon
import com.minew.beacon.MinewBeaconManager
import com.salus.blindbus.database.model.UserRssi
import com.salus.blindbus.ui.activity.MainAct
import com.salus.blindbus.ui.activity.MainAct.Companion.BUS_CATCH_MODE
import java.util.*

class BeaconService : Service() {
    companion object {
        const val SERVICE_CHANNEL_ID = "ServiceChannelID"
        const val FORE_GROUND_SERVICE_ID = 1919
    }
    private val localBinder = LocalBinder()
    var mMinewBeaconManager: MinewBeaconManager? = null
    val comp = UserRssi()

    val myCurrentBusList = mutableListOf<MinewBeacon>()
    val myCancelBusUUIDList = mutableListOf<String>()
    var finishCheckList:MutableList<Boolean> = mutableListOf()

    var trackingModeUUID:String? =null
    var busUUIDBeaconList = mutableListOf("E2C56DB5-DFFB-48D2-B060-D0F5A71096E0")
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