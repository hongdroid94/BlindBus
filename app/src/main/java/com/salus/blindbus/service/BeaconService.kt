package com.salus.blindbus.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.Vibrator
import androidx.annotation.RequiresApi
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


    private val localBinder = LocalBinder()
    var mMinewBeaconManager: MinewBeaconManager? = null
    val comp = UserRssi()

    val myCurrentBusList = mutableListOf<MinewBeacon>()
    val myCancelBusUUIDList = mutableListOf<String>()
    var finishCheckList: MutableList<Boolean> = mutableListOf()

    var trackingModeUUID: String? = null


    //STT Listener
    lateinit var yesEvent: (String) -> Unit
    lateinit var noEvent: (String) -> Unit
    lateinit var SSTfalseEvent: (String) -> Unit
    lateinit var yesFinishEvent: (String) -> Unit
    lateinit var noFinishEvent: (String) -> Unit
    var STTuseingMode = 0

    //TTS Response Delay
    var voiceUse = false

    //STT PlayING
    var STTplaying = false

    // TODO: 버스도착정보 API를 연동해서 각 정류장에 들어오는 버스의 비콘 UUID를 자동으로 자료구조에 담도록 수정해야 함. ( 현재 하드코딩 상태 )
//    var busUUIDBeaconList = mutableListOf("E2C56DB5-DFFB-48D2-B060-D0F5A71096E0") // 로너 비콘
    var busUUIDBeaconList = mutableListOf("74278BDA-B644-4520-8F0C-720EAF059935") // 아타 비콘1 (Blind_1)
//    var busUUIDBeaconList = mutableListOf("AB8190D5-D11E-4941-ACC4-42F30510B408") // 아타 비콘2 (Blind_2)

    var currentBusMode: Int? = BUS_CATCH_MODE
    lateinit var vib: Vibrator
    var trueCheck:Int = -1


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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    fun notificationCreate() {

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(SERVICE_BEACON_CHANNEL_ID, "TheWalkerMusicStart")
            } else {
                SERVICE_BEACON_CHANNEL_ID
            }


        val intent = Intent(this@BeaconService, MainAct::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_dialer)
            .setContentTitle("비콘 확인중입니다")
            .setContentText("비콘 시작중")
            /**
             * TODO: 테스트용으로 임시 설정함 이슈 고침시 변경 예정
             */
            .setContentIntent(pendingIntent)
            .build()

        startForeground(FORE_BEACON_GROUND_SERVICE_ID, builder)
    }

    companion object {
        const val SERVICE_BEACON_CHANNEL_ID = "BeaconChannelID"
        const val FORE_BEACON_GROUND_SERVICE_ID = 191919
    }

}