package com.salus.blindbus.ui.activity

import android.bluetooth.BluetoothAdapter
import android.content.*
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.minew.beacon.*
import com.salus.blindbus.R
import com.salus.blindbus.common.Constants.REPEAT_OK
import com.salus.blindbus.common.getBeaconRSSI
import com.salus.blindbus.common.getBeaconUUID
import com.salus.blindbus.common.getBusBeacon
import com.salus.blindbus.common.getBusStopName
import com.salus.blindbus.databinding.ActivityMainBinding
import com.salus.blindbus.service.BeaconService
import com.salus.blindbus.util.toastLongShow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 메인 화면
 */

class MainAct : AppCompatActivity() , View.OnTouchListener {

    companion object {
        const val FIRST_BUS_PROXIMITY_AREA = -470
        const val REQUEST_ENABLE_BT = 1231
    }

    // about binding object
    private lateinit var binding : ActivityMainBinding

    // about beacon service
    private var beaconService: BeaconService? = null
    private lateinit var bindConnection: ServiceConnection
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var systemBLEDialogCheck = false

    // about touch root view
    private var iTouchDownX = 0f
    private var iTouchDownY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 (View Binding)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setInitialize()
    }

    /**
     * 초기화
     */
    private fun setInitialize() {
        setTedPermission()

        //TODO : 비콘이 스캐닝 콜백이 완성 되었을 때 Visible 처리 필요
        binding.apply {
            tvTtsMsg.visibility = View.INVISIBLE
        }
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
                android.Manifest.permission.BLUETOOTH_ADMIN
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
                setInitService()
                setBluetoothAdapter()
                bluetoothAdapter!!.enable()
                mMinewBeaconManager!!.startScan()
                mMinewBeaconManager!!.setDeviceManagerDelegateListener(object :
                    MinewBeaconManagerListener {

                    override fun onAppearBeacons(minewBeacons: MutableList<MinewBeacon>?) {
                        toastLongShow("새로운 스캔을 했을때 호출")
                    }

                    override fun onDisappearBeacons(minewBeacons: MutableList<MinewBeacon>?) {
                        toastLongShow("10초이상 스캔이 호출되지 않으면 이 메서드 호출")
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
            val beacon: MinewBeacon = (busList as MutableList<MinewBeacon>).getBusBeacon(uuid) ?: return
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

                setEnabledRootTouch(true)
                tvTtsMsg.text = resources.getString(R.string.busNumber).format(
                    myCurrentBusList.last().getBusStopName()
                )
            }
        }
    }

    private fun setEnabledRootTouch(isTouchEnabled: Boolean) {
        if(!isTouchEnabled)
            binding.layoutRoot.setOnTouchListener(null)
        else
            binding.layoutRoot.setOnTouchListener(this)
    }

    private fun setBindConnection() {
        bindConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
                Log.d("asd", "왜안돼")
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

    private fun Int.vibrationIntensity(repeat: Int) {

//        if (내가 선택한 버스가 접근할시) {
        when {
            this < -80 -> vibration(1000, 20, repeat)
            this < -70 -> vibration(750, 80, repeat)
            this < -80 -> vibration(500, 150, repeat)
            this < -60 -> vibration(50, 255, repeat)
            else -> {
                //버스에 탑승 완료를 하거나 버스가 떠났을시
                beaconService?.vib?.cancel()
            }
        }
//        } else {
//            vib.cancel()
//        }
    }

    private fun vibration(timings: Long, amplitude: Int, repeat: Int) {

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
            return
        }
        beaconService?.vib?.vibrate(
            longArrayOf(timings, 1000, timings, 1000),
            repeat
        )
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
                                setYesAction()
                            }

                            else {
                                Log.e("SWIPE", "RIGHT TO LEFT")
                                // FLAG NO
                                setNoAction()

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

    /**
     * 사용자가 예 스와이프 액션을 취했을 시..
     */
    private fun setYesAction() {
        binding.apply {
            beaconService?.apply {
                myCancelBusUUIDList.add(myCurrentBusList.last().getBeaconUUID())
                myCurrentBusList.removeAt(myCurrentBusList.lastIndex)

                if (myCurrentBusList.isNullOrEmpty()) {
                    for (i in 0 until root.childCount) {
                        root.getChildAt(i).visibility = View.GONE
                    }
                } else {
                    tvTtsMsg.text = resources.getString(R.string.busNumber).format(
                        myCurrentBusList.last().getBusStopName()
                    )
                }
            }
        }
    }

    /**
     * 사용자가 아니오 스와이프 액션을 취했을 시..
     */
    private fun setNoAction() {
        binding.apply {
            beaconService?.apply {
                trackingModeUUID = myCurrentBusList.last().getBeaconUUID()

                for (i in 0 until root.childCount) {
                    root.getChildAt(i).visibility = View.GONE
                    myCurrentBusList.clear()
                    myCancelBusUUIDList.clear()
                }
            }
        }
    }




}