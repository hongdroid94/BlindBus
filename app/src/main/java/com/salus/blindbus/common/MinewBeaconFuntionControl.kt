package com.salus.blindbus.common

import com.minew.beacon.BeaconValueIndex
import com.minew.beacon.MinewBeacon

fun MinewBeacon.getBusStopName():String {
    return when (this.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).stringValue) {
        "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0"->"22"
        else -> "0"
    }
}

fun MinewBeacon.getBeaconUUID(): String {
    return this.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).stringValue
}
fun MinewBeacon.getBeaconRSSI(): Int {
    return this.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue
}

fun MutableList<MinewBeacon>.getBusBeacon(checkUUID: String): MinewBeacon? {
    return this.find {
        it.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).stringValue == checkUUID
    }
}