package com.salus.blindbus.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.minew.beacon.BluetoothState
import com.minew.beacon.MinewBeacon
import com.minew.beacon.MinewBeaconManager
import com.minew.beacon.MinewBeaconManagerListener
import com.salus.blindbus.R
import com.salus.blindbus.databinding.ActivityMainBinding
import com.salus.blindbus.databinding.ActivityTestBinding
import com.salus.blindbus.ui.adapter.BeaconDataAdapter

class TestAct : AppCompatActivity() {
    var mMinewBeaconManager: MinewBeaconManager? = null
    private lateinit var binding: ActivityTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mMinewBeaconManager = MinewBeaconManager.getInstance(this@TestAct)
        mMinewBeaconManager!!.startScan()

        mMinewBeaconManager!!.setDeviceManagerDelegateListener(object :MinewBeaconManagerListener{
            override fun onAppearBeacons(p0: MutableList<MinewBeacon>?) {}
            override fun onDisappearBeacons(p0: MutableList<MinewBeacon>?) {}
            override fun onRangeBeacons(list: MutableList<MinewBeacon>?) {
                binding.apply{
                    rvBeaconListData.adapter = list?.let { BeaconDataAdapter(it) }
                    (rvBeaconListData.adapter as BeaconDataAdapter).notifyDataSetChanged()
                }

            }
            override fun onUpdateState(p0: BluetoothState?) {}

        })
    }
}