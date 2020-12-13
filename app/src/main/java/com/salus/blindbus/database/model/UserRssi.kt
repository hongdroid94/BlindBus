package com.salus.blindbus.database.model

import com.minew.beacon.BeaconValueIndex
import com.minew.beacon.MinewBeacon
import java.util.*

class UserRssi : Comparator<MinewBeacon?> {

    override fun compare(minewBeacon: MinewBeacon?, t1: MinewBeacon?): Int {
        val floatValue1 =
            minewBeacon?.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI)?.floatValue
        val floatValue2 =
            t1?.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI)?.floatValue
        return if (floatValue1!! < floatValue2!!)
            1
        else if (floatValue1 == floatValue2)
            0
        else
            -1

    }
}