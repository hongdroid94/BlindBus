package com.salus.blindbus.util

import android.content.Context
import android.widget.Toast

fun Context.toastShortShow(stringMessage: String) =
     Toast.makeText(this, stringMessage, Toast.LENGTH_SHORT).show()

 fun Context.toastShortShow(resId: Int) =
     Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

 fun Context.toastLongShow(stringMessage: String?) =
     Toast.makeText(this, stringMessage, Toast.LENGTH_SHORT).show()

 fun Context.toastLongShow(resId: Int) =
     Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

