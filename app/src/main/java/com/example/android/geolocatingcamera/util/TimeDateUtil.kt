package com.example.android.geolocatingcamera.util

import java.util.*

fun formatTimestamp(long: Long):String{
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = long

    val hourText = addZeroText(calendar.get(Calendar.HOUR_OF_DAY))
    val minuteText = addZeroText(calendar.get(Calendar.MINUTE))

    val dateText = calendar.get(Calendar.DAY_OF_MONTH)
    val month = formatMonth(calendar.get(Calendar.MONTH))
    val year = calendar.get(Calendar.YEAR)
    return "$dateText $month $year $hourText:$minuteText"
}

fun addZeroText(int:Int):String{
    return if(int<10){
        "0$int"
    }else{
        "$int"
    }
}

fun formatMonth(int:Int):String{
    return when(int){
        0->"January"
        1->"February"
        2->"March"
        3->"April"
        4->"May"
        5->"June"
        6->"July"
        7->"August"
        8->"September"
        9->"October"
        10->"November"
        11->"December"
        else -> throw IllegalArgumentException("No other number should be received")
    }

}