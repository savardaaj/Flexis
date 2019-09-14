package com.asav.flexis

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.TextView

class TaskTimer(internal val mainActivity: MainActivity, v: View, duration: Int) {

    internal var isRunning = false

    internal var elapsedTime: Long = 0
    internal var cardAction: TextView
    internal var context: Context? = null
    internal var cmCardTimer: Chronometer
    internal var isFinished: Boolean = false

    internal var desiredTime: String = "0"
    internal var desiredTimeInMilliSeconds: Long = 0

    init {
        calculateDuration(duration)

        cmCardTimer = v.findViewById(R.id.cm_timer)
        cardAction = v.findViewById(R.id.tv_card_action)

        cmCardTimer.onChronometerTickListener = object : Chronometer.OnChronometerTickListener {
            var hours: Long = 0
            var minutes: Long = 0
            var seconds: Long = 0
            override fun onChronometerTick(arg0: Chronometer) {

                if (!isRunning) {
                    seconds = (SystemClock.elapsedRealtime() - cmCardTimer.base) / 1000 % 60
                    minutes = (SystemClock.elapsedRealtime() - cmCardTimer.base) / 1000 / 60
                    hours = minutes / 60
                    elapsedTime = SystemClock.elapsedRealtime()
                } else {
                    seconds = (elapsedTime - cmCardTimer.base) / 1000 % 60
                    minutes = (elapsedTime - cmCardTimer.base) / 1000 / 60
                    hours = minutes / 60
                    elapsedTime = elapsedTime + 1000
                }
                val output = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                cmCardTimer.text = output
                if (output == desiredTime) {
                    //call push notification
                    mainActivity.onClickCreateNotification(v)
                    cardAction.text = "Finish"
                }
            }
        }
    }

    fun startTimer() {

        isRunning = true

        cmCardTimer.base = 0
        cmCardTimer.start()

        cardAction.text = "Pause"

    }

    fun resumeTimer() {
        isRunning = true
        cmCardTimer.start()
        cardAction.text = "Pause"
    }

    fun pauseTimer() {
        isRunning = false
        cmCardTimer.stop()
        cardAction.text = "Resume"
    }

    fun calculateDuration(duration: Int) {

        val desiredHours = duration / 60
        val desiredMinutes = duration % 60
        val desiredSeconds = 0

        this.desiredTime = String.format("%02d:%02d:%02d", desiredHours, desiredMinutes, desiredSeconds)
        this.desiredTimeInMilliSeconds = ((desiredSeconds + desiredMinutes * 60 + desiredHours * 3600) * 1000).toLong()
    }
}
