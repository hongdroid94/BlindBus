package com.salus.blindbus.common

import com.minew.beacon.BeaconValueIndex
import com.minew.beacon.MinewBeacon

fun MinewBeacon.getBusStopName():String {
    return when (this.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).stringValue) {
        "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0"->"96E0" // 로너비콘, UUID 끝번호 4자리를 버스번호로 변경함. - atansio
        "74278BDA-B644-4520-8F0C-720EAF059935"->"9935" // 아타비콘 (Blind_1)
        "AB8190D5-D11E-4941-ACC4-42F30510B408"->"B408" // 아타비콘 (Blind_2)
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


fun String.getBusStopName():String {
    return when (this) {
        "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0"->"96E0" // 로너 비콘, UUID 끝번호 4자리를 버스번호로 변경함. - atansio
        "74278BDA-B644-4520-8F0C-720EAF059935"->"9935" // 아타 비콘( Blind_1 )
        "AB8190D5-D11E-4941-ACC4-42F30510B408"->"B408" // 아타 비콘( Blind_2 )
        else -> "0"
    }
}