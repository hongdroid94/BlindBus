package com.salus.blindbus.ui.activity

import android.bluetooth.BluetoothAdapter
import android.content.*
import android.graphics.Color
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.minew.beacon.*
import com.salus.blindbus.R
import com.salus.blindbus.common.*
import com.salus.blindbus.common.Constants.REPEAT_OK
import com.salus.blindbus.databinding.ActivityMainBinding
import com.salus.blindbus.service.BeaconService
import com.salus.blindbus.util.SharedManager
import com.salus.blindbus.util.toastLongShow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * 메인 화면
 */

class MainAct : AppCompatActivity(), View.OnTouchListener, TextToSpeech.OnInitListener {


    private var appExit = 0

    // about binding object
    private lateinit var binding: ActivityMainBinding

    // about beacon service
    private var beaconService: BeaconService? = null
    private lateinit var bindConnection: ServiceConnection
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var systemBLEDialogCheck = false

    // about touch root view
    private var iTouchDownX = 0f
    private var iTouchDownY = 0f

    //TTS
    private var tts: TextToSpeech? = null

    //STT
    private lateinit var recognizer: SpeechRecognizer
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onError(error: Int) {
            val message: String = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 타임아웃"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버 에러"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "시간 초과"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                else -> "알수 없음"
            }

            Log.d("STT_ERROR", message)
//            Toast.makeText(this@MainAct, message, Toast.LENGTH_SHORT).show()
        }

        override fun onResults(results: Bundle?) {

            //STT
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null) {
                for (i in 0 until matches.size) {
                    binding.tvResult.text = matches[i]


                    when (beaconService!!.STTuseingMode) {
                        STT_USE_MODE_FIRST_QUESTION -> {
                            beaconService!!.yesEvent(matches[i])
                            beaconService!!.noEvent(matches[i])
                        }

                        STT_USE_MODE_END_QUESTION -> {
                            beaconService!!.yesFinishEvent(matches[i])
                            beaconService!!.noFinishEvent(matches[i])
                        }
                    }
                }


            }
        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 (View Binding)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setInitialize()

        binding.btnGoToTheTestAct.setOnClickListener {
            startActivity(Intent(this@MainAct, TestAct::class.java))
        }
    }

    /**
     * 초기화
     */
    private fun setInitialize() {
        setTedPermission()

        //TODO : 비콘이 스캐닝 콜백이 완성 되었을 때 Visible 처리 필요
        binding.apply {
            btnLogout.setOnClickListener {
                // 로그아웃
                val alertDialog = AlertDialog.Builder(this@MainAct)
                alertDialog.setTitle("[ 안내 ]")
                alertDialog.setMessage("로그아웃을 하시겠습니까 ?")
                alertDialog.setPositiveButton("확인") { dialog, id ->
                    Toast.makeText(this@MainAct, getString(R.string.LOGOUT), Toast.LENGTH_SHORT)
                        .show()
                    SharedManager.clear()
                    val loginIntent = Intent(this@MainAct, LoginAct::class.java)
                    startActivity(loginIntent)
                    finish()
                }
                alertDialog.setNegativeButton("취소") { dialog, id ->
                    dialog.dismiss()
                }
                alertDialog.show()

            }
        }

        tts = TextToSpeech(this, this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")

        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(recognitionListener)
    }

    /**
     * 권한 설정
     */
    private fun setTedPermission() {
        setPermission {
            setBluetoothAdapter()
            setBindConnection()
            startBindService()
            beaconScanBegin()
        }
    }

    private fun setPermission(afterLogic: () -> Unit) {
        val perMissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                afterLogic()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                finish()
            }
        }
        TedPermission.with(this@MainAct)
            .setPermissionListener(perMissionListener)
            .setPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.RECORD_AUDIO
            )
            .setRationaleTitle("권한을 허용하셔야 사용하실수 있습니다.")
            .check()
    }

    private fun setBluetoothAdapter() {
        bluetoothAdapter ?: BluetoothAdapter.getDefaultAdapter().also {
            bluetoothAdapter = it
        }
    }

    private fun beaconScanBegin() {
        CoroutineScope(Dispatchers.Main).launch {
            beaconService ?: beaconScanBegin()
            beaconService?.apply {
                vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                mMinewBeaconManager = MinewBeaconManager.getInstance(this)
                notificationCreate()
                setBluetoothAdapter()
                bluetoothAdapter!!.enable()
                mMinewBeaconManager!!.startScan()
                mMinewBeaconManager!!.setDeviceManagerDelegateListener(object :
                    MinewBeaconManagerListener {

                    override fun onAppearBeacons(minewBeacons: MutableList<MinewBeacon>?) {

                    }

                    override fun onDisappearBeacons(minewBeacons: MutableList<MinewBeacon>?) {

                    }

                    override fun onRangeBeacons(minewBeacons: MutableList<MinewBeacon>?) {
                        Log.d("salusTest_1second_scan", "1초마다 스캔중 일때 호출")


                        if (!bluetoothAdapter!!.isEnabled && !systemBLEDialogCheck)
                            enableDisableBT()

                        /**TODO: 미래에 이부분에 비콘LIST를 받으면 서버에 UUID를 보내서 버스 UUID리스트만
                         * 반환 받아서 사용할 예정
                         */
                        if (trackingModeUUID != null) {
                            val trackingBeacon = minewBeacons?.find {
                                it.getBeaconUUID() == trackingModeUUID
                            }
                            trackingBeacon?.getBeaconRSSI()?.vibrationIntensity(REPEAT_OK)

                            trackingBeacon ?: return
                            if (trackingBeacon.getBeaconRSSI() > -75 && !tts!!.isSpeaking) {
                                finishCheckList.add(true)
                            } else
                                finishCheckList.clear()
                            if (finishCheckList.size > 4) {
                                //도착 탑승 묻기
                                finishCheckList.clear()
                                beaconService?.currentBusMode = BUS_FINISH_MODE
                                if (!tts!!.isSpeaking)
                                    tts?.speak(
                                        "탑승 완료 체크하시겠습니까?",
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        null
                                    )
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(2000)
                                    setEnabledRootTouch(true)

                                    yesFinishEvent = {
                                        if (it == "네") {
                                            setFinishYesButton()
                                            binding.tvResult.text = it
                                        }
                                    }
                                    noFinishEvent = {
                                        if (it == "아니요") {
                                            setFinishNoButton()
                                            binding.tvResult.text = it
                                        }
                                    }
                                    STTuseingMode = STT_USE_MODE_END_QUESTION
                                    recognizer.startListening(intent)
                                }
                            }
                            return
                        }

                        val busList = minewBeacons?.filter {
                            it.getBeaconValue(
                                BeaconValueIndex
                                    .MinewBeaconValueIndex_RSSI
                            ).intValue > FIRST_BUS_PROXIMITY_AREA
                        }
                        if (busList.isNullOrEmpty())
                            return

                        beaconScanFiltering(busList)
                        setYesAndNo()

                    }

                    override fun onUpdateState(state: BluetoothState?) {

                        when (state) {

                            BluetoothState.BluetoothStatePowerOn ->
                                toastLongShow("블루투스 기능이 ON 됬습니다")
                            BluetoothState.BluetoothStatePowerOff ->
                                toastLongShow("블루투스 기능이 OFF 됬습니다")
                            else -> return
                        }
                    }
                })

            }
        }
    }

    private fun BeaconService.beaconScanFiltering(busList: List<MinewBeacon>) {

        for (uuid in busUUIDBeaconList) {
            val beacon: MinewBeacon =
                (busList as MutableList<MinewBeacon>).getBusBeacon(uuid) ?: return
            myCurrentBusList.add(beacon)
        }

        for (deleteUUid in myCancelBusUUIDList)
            myCurrentBusList.remove(
                myCurrentBusList.toMutableList().getBusBeacon(deleteUUid)
            )

        if (myCurrentBusList.isNullOrEmpty())
            return
    }

    private fun BeaconService.setYesAndNo() {


        if (myCurrentBusList.isNotEmpty()) {
            binding.apply {
                for (i in 0 until root.childCount)
                    root.getChildAt(i).visibility = View.VISIBLE

                myCurrentBusList.sortBy { it.getBeaconRSSI() }

                tvMsg.text = resources.getString(R.string.BUS_NUMBER).format(
                    myCurrentBusList.last().getBusStopName()
                )

                if (!tts!!.isSpeaking && !voiceUse) {
                    voiceUse = true
                    tts?.speak(
                        "${myCurrentBusList.last().getBusStopName()}번 버스에 탑승하시나요?",
                        TextToSpeech.QUEUE_FLUSH, null, null
                    )

                    Thread.sleep(2300)
                    setEnabledRootTouch(true)
                    yesEvent = {
                        if (it == "네") {
                            setYesAction()
                            tvResult.text = it
                        } else {
                            voiceUse = false
                        }

                    }
                    noEvent = {
                        if (it == "아니요") {
                            setNoAction()
                            tvResult.text = it
                        } else {
                            voiceUse = false
                        }
                    }
                    STTuseingMode = STT_USE_MODE_FIRST_QUESTION
                    recognizer.startListening(intent)

                }
            }
        }
    }

    private fun setEnabledRootTouch(isTouchEnabled: Boolean) {
        if (!isTouchEnabled)
            binding.layoutRoot.setOnTouchListener(null)
        else
            binding.layoutRoot.setOnTouchListener(this)
    }

    private fun setBindConnection() {
        bindConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
                val binder = service as BeaconService.LocalBinder
                beaconService = binder.getService()
            }

            override fun onServiceDisconnected(p0: ComponentName?) {}
        }
    }

    private fun startBindService() {
        Intent(this, BeaconService::class.java).let {
            this.bindService(
                it,
                bindConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun enableDisableBT() {

        if (!bluetoothAdapter!!.isEnabled) {
            Log.d("blueToothNullCheck", "블루투스가 활성화를 시도합니다.")
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBTIntent)

            val bTIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(mBroadCastReceiver, bTIntent)
        }
    }

    private val mBroadCastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {

                    BluetoothAdapter.STATE_ON ->
                        Log.d(
                            "blueToothChangeCheck",
                            "onReceive State On"
                        )

                    BluetoothAdapter.STATE_OFF ->
                        Log.d(
                            "blueToothChangeCheck",
                            "onReceive State Off"
                        )

                    BluetoothAdapter.STATE_TURNING_ON ->
                        Log.d(
                            "blueToothChangeCheck",
                            "onReceive turning on"
                        )

                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        enableDisableBT()
                        Log.d(
                            "blueToothChangeCheck",
                            "onReceive turning off"
                        )
                    }
                }
            }
        }
    }


    private var rssiChangeCheck = 0
    private var rssiTemp = 0


    // 거리별 진동 세기, 패턴 메서
    private fun Int.vibrationIntensity(repeat: Int) {


        // 내가 선택한 버스가 도착시 진동으로 알림을 준다.

        /*
            로너 비콘(i4)에 맞는 진동 셋팅
        */
//        when {
//
//            this < -180 -> {
//                vibration(1500, 70, repeat, "20~12")
//                rssiChangeCheck = 1
//                Log.d("hjh", "$rssiChangeCheck")
//
//            }
//            this < -140 -> {
//                vibration(750, 140, repeat, "12~8")
//                rssiChangeCheck = 2
//                Log.d("hjh", "$rssiChangeCheck")
//            }
//            this < -90 -> {
//                vibration(300, 200, repeat, "9~5")
//                rssiChangeCheck = 3
//                Log.d("hjh", "$rssiChangeCheck")
//            }
//            this < -75 -> {
//                vibration(50, 255, repeat, "5~1")
//                rssiChangeCheck = 4
//                Log.d("hjh", "$rssiChangeCheck")
//            }
//            else -> {
//                //버스에 탑승 완료를 하거나 버스가 떠났을시
//                beaconService?.vib?.cancel()
//            }
//        }


        /*
            아타나시오 비콘(E2)에 맞는 진동 셋팅
        */
//        when {
//            this < -3250 -> vibration(2000, 15, repeat, "20") // 가장 멀리있을때
//            this < -1625 -> vibration(1250, 50, repeat, "15")
//            this < -1300 -> vibration(700, 100, repeat, 10)
//            this < -650 -> vibration(100, 200, repeat, 5)
//            this < -130 -> vibration(50, 255, repeat, 1) // 가장 가까울 떄
//            else -> {
//                //버스에 탑승 완료를 하거나 버스가 떠났을시
//                beaconService?.vib?.cancel()
//            }
//        }

        when {

            this < -100 -> {
                vibration(1500, 70, repeat, "5")
                rssiChangeCheck = 1
                Log.d("RssiScoreCheck-100:", "$rssiChangeCheck")

            }
            this < -90 -> {
                vibration(750, 140, repeat, "4")
                rssiChangeCheck = 2
                Log.d("RssiScoreCheck-90:", "$rssiChangeCheck")
            }
            this < -80 -> {
                vibration(300, 200, repeat, "3")
                rssiChangeCheck = 3
                Log.d("RssiScoreCheck-80:", "$rssiChangeCheck")
            }
            this < -70 -> {
                vibration(50, 255, repeat, "2")
                rssiChangeCheck = 4
                Log.d("RssiScoreCheck-70:", "$rssiChangeCheck")
            }
            else -> {
                //버스에 탑승 완료를 하거나 버스가 떠났을시
                beaconService?.vib?.cancel()
            }
        }

    }

    // 거리 피드백 관련
    private fun vibration(timings: Long, amplitude: Int, repeat: Int, currentDistance: String) {

        // 거리별 음성 안내
        fun distanceTTS() {
            tts ?: return
            if (!tts!!.isSpeaking) { // 중복 음성 안내 방지 ( tts가 나오지 않을 때 )
                Log.d("TTSuseCheck", "tts가 나오지 않을 때 -> rssi값 :: $rssiChangeCheck")
                if (rssiChangeCheck != rssiTemp) {
                    // rssi값이 0이 아닐때
                    if (rssiChangeCheck < rssiTemp) {

                        // rssi값이 0보다 작을때 ex :: -60 ( rssi 값은 음수이다 )
                    } else {
                        tts?.speak(
                            "버스까지 ${currentDistance}미터 남았습니다.",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            null
                        )
                        rssiTemp = rssiChangeCheck
                    }

                }
            }

        }
        //timings 진동의 진행 시간
        //amplitude 진동의 세기
        //repeat 진동의 반복 flag
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            beaconService?.vib?.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(timings, 1000, timings, 1000),
                    intArrayOf(0, amplitude, 0, amplitude),
                    repeat
                )
            )
            distanceTTS()
        } else {
            beaconService?.vib?.vibrate(
                longArrayOf(timings, 1000, timings, 1000),
                repeat
            )
            distanceTTS()
        }
    }

    override fun onResume() {
        super.onResume()
        systemBLEDialogCheck = false
    }

    override fun onPause() {
        super.onPause()
        systemBLEDialogCheck = true
    }

    // =================== Touch Area =================== //
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        var iTouchUpX = 0f
        var iTouchUpY = 0f
        when (view.id) {
            R.id.layout_root -> {
                Log.e("onTouch", "root_layout Area")
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        iTouchDownX = event.x
                        iTouchDownY = event.y
                    }
                    MotionEvent.ACTION_UP -> {
                        iTouchUpX = event.x
                        iTouchUpY = event.y
                        Log.e("iTouchUpX", iTouchUpX.toString())
                        Log.e("iTouchDownX", iTouchDownX.toString())
                        Log.e("result", (iTouchUpX - iTouchDownX).toString())
                        val resultX: Float = iTouchUpX - iTouchDownX
                        if (Math.abs(resultX) > 100) { // 숫자를 조정하면 Swipe 허용 범위를 변경할 수 있다.
                            // Swipe Action 일 경우..
                            Log.e("onTouch", "============== SWIPE EVENT ==============")
                            if (resultX > 0) {
                                Log.e("SWIPE", "LEFT TO RIGHT")
                                // FLAG YES
                                when (beaconService!!.currentBusMode) {
                                    BUS_CATCH_MODE -> setYesAction()
                                    BUS_FINISH_MODE -> setFinishYesButton()
                                }

                                setChangeSwipeColor("#0000ff")


                            } else {
                                Log.e("SWIPE", "RIGHT TO LEFT")
                                // FLAG NO
                                when (beaconService!!.currentBusMode) {
                                    BUS_CATCH_MODE -> setNoAction()
                                    BUS_FINISH_MODE -> setFinishNoButton()
                                }

                                setChangeSwipeColor("#ff0000")
                            }

                        }
                        // Yes Or No의 기능을 수행하고나면 스와이프 이벤트는 받을 필요없으므로 block 처리
                        setEnabledRootTouch(false)
                    }
                }
            }
        }
        return true
    }

    private fun setChangeSwipeColor(colorVal: String) {
        binding.apply {
            layoutRoot.setBackgroundColor(Color.parseColor(colorVal))
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        layoutRoot.setBackgroundColor(Color.parseColor("#000000"))
                    }
                }
            }, 500)
        }
    }

    /**
     * 사용자가 아니오 스와이프 액션을 취했을 시..
     */
    private fun setNoAction() {
        binding.apply {
            beaconService?.apply {
                tvMsg.text = "${myCurrentBusList.last().getBeaconUUID()}번\n버스추적 취소"
                myCancelBusUUIDList.add(myCurrentBusList.last().getBeaconUUID())
                myCurrentBusList.removeAt(myCurrentBusList.lastIndex)
                tts?.speak(
                    "취소하셨습니다.", TextToSpeech.QUEUE_FLUSH,
                    null, null
                )
                if (myCurrentBusList.isNullOrEmpty()) {
                    tvMsg.text = "다가오는 버스 탐색중.."
                } else {
                    tvMsg.text = resources.getString(R.string.BUS_NUMBER).format(
                        myCurrentBusList.last().getBusStopName()
                    )
                }
                voiceUse = false
            }

        }
    }

    private fun setFinishNoButton() {
        binding.apply {
            tts ?: return
            if (!tts!!.isSpeaking)
                tts?.speak(
                    "탑승체크가 재요청시 다시 확인해주세요.", TextToSpeech.QUEUE_FLUSH,
                    null, null
                )
            tvMsg.text = "탑승체크가 재요청시 다시 확인해주세요."
            beaconService?.finishCheckList?.clear()
        }
    }

    /**
     * 사용자가 예 스와이프 액션을 취했을 시..
     */
    private fun setYesAction() {
        binding.apply {
            beaconService?.apply {
                trackingModeUUID = myCurrentBusList.last().getBeaconUUID()

                myCurrentBusList.clear()
                myCancelBusUUIDList.clear()
                voiceUse = false
                tts?.speak(
                    "${trackingModeUUID!!.getBusStopName()}번 버스를 따라갑니다",
                    TextToSpeech.QUEUE_FLUSH,
                    null, null
                )
                tvMsg.text = "${trackingModeUUID!!.getBusStopName()}번\n버스추적"
            }

        }
    }


    private fun setFinishYesButton() {
        binding.apply {
            beaconService?.apply {

                tvMsg.text = "${trackingModeUUID!!.getBusStopName()}번\n탑승완료"
                trackingModeUUID = null
                finishCheckList.clear()
                myCurrentBusList.clear()
                busUUIDBeaconList.clear()
                mMinewBeaconManager!!.stopScan()
                currentBusMode = BUS_CATCH_MODE
                vib.cancel()
                CoroutineScope(Dispatchers.IO).launch {

                    if (!tts!!.isSpeaking)
                        tts?.speak("탑승 완료 되었습니다", TextToSpeech.QUEUE_FLUSH,
                            null, null)

                }
            }
        }
    }

    // 진동은 구간별로 잘 동작함.
    // TTS는 진동에 매칭되지 않는다.
    // 예 :: 최대 거리 최대 -> 진동 세기 최소
    //                  -> TTS 4미터
    // 구간별로 진동은 변화하지만 ( 가까워 질수록 강해 짐 ) TTS 음성은 진동 변화와 쌍을 이루지 못하고 따로 동작함.


    //TTS Listener
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.KOREA)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {

                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)

                Toast.makeText(this, "설정 체크", Toast.LENGTH_SHORT).show()
            } else {

//                tts?.setPitch(0.5f)
//                tts?.setSpeechRate(0.4f)

                //다른곳에서도 이방식으로 사용
//                tts?.speak(
//                    "88번 버스가 진입중입니다 이 버스가 맞습니까? 소리가 나면 예 아니오로 대답해",
//                    TextToSpeech.QUEUE_FLUSH,
//                    null,
//                    null
//                )
            }
        } else {
            // 실패
        }

    }

    companion object {
        const val FIRST_BUS_PROXIMITY_AREA = -470
        const val REQUEST_ENABLE_BT = 1231
        const val BUS_CATCH_MODE = 8872
        const val BUS_FINISH_MODE = 9999

        const val STT_USE_MODE_FIRST_QUESTION = 0
        const val STT_USE_MODE_END_QUESTION = 1

    }

    override fun onBackPressed() {
        CoroutineScope(Dispatchers.Main).launch {
            if (appExit == 0) {
                appExit++
                toastLongShow("종료하시려면 뒤로가기 버튼을 한번 더 눌러주세요")
                delay(4000)
                appExit = 0
            } else {
                recognizer.stopListening()
                beaconService!!.mMinewBeaconManager!!.stopScan()
                unbindService(bindConnection)

                super.onBackPressed()

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }

        beaconService!!.mMinewBeaconManager!!.stopScan()

    }

}