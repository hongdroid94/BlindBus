package com.salus.blindbus.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.minew.beacon.BeaconValueIndex
import com.minew.beacon.MinewBeacon
import com.salus.blindbus.R
import com.salus.blindbus.common.getBeaconRSSI
import com.salus.blindbus.common.getBeaconUUID
import com.salus.blindbus.databinding.ItemBeaconDataBinding

class BeaconDataAdapter(
    val beaconList: MutableList<MinewBeacon> = mutableListOf()
) : RecyclerView.Adapter<BeaconDataAdapter.ViewHolder>() {
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val binding = ItemBeaconDataBinding.bind(v)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_beacon_data, parent, false)
        )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.also {
            val beacon = beaconList[position]
            it.tvMajor.text =
                "메이저:${beacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).stringValue}"
            it.tvMinor.text =
                "마이너:${beacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).stringValue}"
            it.tvRssi.text = "RSSI:${beacon.getBeaconRSSI()}"
            it.tvTxpower.text =
                "TxPower:${beacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_TxPower).stringValue}"
            it.tvUuid.text = "UUID${beacon.getBeaconUUID()}"
            it.tvName.text =
                "Name:${beacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).stringValue}"
            it.tvMac.text =
                "MAC:${beacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue}"
            it.tvBattery.text =
                "Battery:${beacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_BatteryLevel)
                    .stringValue}"
        }
    }

    override fun getItemCount() = beaconList.size
}