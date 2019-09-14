package com.asav.flexis

import android.util.Log

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

import com.asav.flexis.MainActivity.Companion.printStackTrace

class TimeConverter {
    companion object {

        //converts from 5:06 pm to GMT time, 05:06:00Z-5:00
        fun convertToGMTFromDisplay(timeParam: String): String {

            var gmt = ""

            try {
                val time = timeParam.substring(0, 5)
                val AMPM = timeParam.substring(6, 8)

                val inputDateFormat = SimpleDateFormat("HH:mm")
                inputDateFormat.timeZone = TimeZone.getDefault()

                val inputDate = inputDateFormat.parse(timeParam)

                val cal = Calendar.getInstance()
                cal.time = inputDate
                //cal.add(Calendar.HOUR_OF_DAY, 12);

                val outputDateFormat = SimpleDateFormat("HH:mm:ss 'GMT'")
                outputDateFormat.timeZone = TimeZone.getTimeZone("GMT")

                var militaryTime = Date()

                if (AMPM == "am") {
                    militaryTime = cal.time
                } else {
                    cal.add(Calendar.HOUR_OF_DAY, 12)
                    militaryTime = cal.time
                }

                //convert military to GMT
                gmt = outputDateFormat.format(militaryTime)

            } catch (e: Exception) {
                Log.d("***ERROR***", "convertToGMTFromDisplay: " + e.message)
                Log.d("***ERROR***", "convertToGMTFromDisplay: $e")
                printStackTrace(e.stackTrace)
            }

            return gmt
        }

        fun convertFromGMTDisplay(timeParam: String) {

        }

        fun convertDisplayToDate(timeParam: String): Date {

            var inputDate = Date()
            val AMPM = timeParam.substring(6, 8)

            try {
                val inputDateFormat = SimpleDateFormat("HH:mm")
                //inputDateFormat.setTimeZone(TimeZone.getDefault());

                inputDate = inputDateFormat.parse(timeParam)

                val cal = Calendar.getInstance()
                cal.time = inputDate

                var militaryTime = Date()

                if (AMPM == "am") {
                    militaryTime = cal.time
                } else {
                    cal.add(Calendar.HOUR_OF_DAY, 12)
                    militaryTime = cal.time
                }

                val dateFormat = SimpleDateFormat("HH:mm")
                val time = dateFormat.format(militaryTime)
                inputDate = inputDateFormat.parse(time)
            } catch (e: Exception) {
                Log.d("***ERROR***", "convertDisplayToDate: " + e.message)
                Log.d("***ERROR***", "convertDisplayToDate: $e")
                printStackTrace(e.stackTrace)
            }

            return inputDate
        }

        val currentTime: Date
            get() {
                var date = Calendar.getInstance().time
                val inputDateFormat = SimpleDateFormat("HH:mm")
                val dateFormat = SimpleDateFormat("HH:mm")
                val time = dateFormat.format(date)
                try {
                    date = inputDateFormat.parse(time)
                } catch (e: Exception) {
                    Log.d("***ERROR***", "getCurrentTime: " + e.message)
                    Log.d("***ERROR***", "getCurrentTime: $e")
                    printStackTrace(e.stackTrace)
                }

                return date
            }
    }
}
