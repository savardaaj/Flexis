package com.asav.flexis

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker

import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

import com.google.firebase.Timestamp
import com.google.gson.Gson

//import java.sql.Date;
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

import com.asav.flexis.MainActivity.Companion.APP_CREATE
import com.asav.flexis.MainActivity.Companion.APP_EDIT

class TimeBlockPage : AppCompatActivity() {

    internal var dbh = DatabaseHandler()

    internal var existingTimeBlock: TimeBlock? = null


    internal lateinit var et_name: EditText
    internal lateinit var et_description: EditText

    internal var category = ""

    internal lateinit var tv_BlockStartTime: TextView
    internal lateinit var tv_BlockEndTime: TextView
    internal lateinit var btnDeleteTimeBlock: Button

    internal var timeblock: TimeBlock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("***DEBUG***", "inside onCreate TimeBlockPage")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blockpage)

        initializeLayout()
        setupTimePickers()

        val categories = intent.categories

        intent.extras

        val intent = intent
        if (intent != null) {
            val extras = intent.extras
            if (extras != null) {
                //user = extras.getParcelable("user");
                val JSON = extras.getString("details")
                timeblock = Gson().fromJson(JSON, TimeBlock::class.java)
                if (categories != null && categories.contains(APP_EDIT)) {
                    category = APP_EDIT
                    existingTimeBlock = timeblock
                    populateFields()
                }
            }
        }

    }

    fun initializeLayout() {
        Log.d("***DEBUG***", "inside setTodaysDate")

        val cl: ConstraintLayout = findViewById(R.id.cl_blockPage)
        et_name = cl.findViewById(R.id.et_BlockName)
        et_description = cl.findViewById(R.id.et_BlockDescription)
        tv_BlockStartTime = cl.findViewById(R.id.tv_BlockStartTime)
        tv_BlockEndTime = cl.findViewById(R.id.tv_BlockEndTime)
        btnDeleteTimeBlock = cl.findViewById(R.id.btn_deleteTimeBlock)
        btnDeleteTimeBlock.visibility = View.INVISIBLE
    }

    fun setupTimePickers() {
        Log.d("***DEBUG***", "inside setupTimePickers")

        tv_BlockStartTime.setOnClickListener {
            val mcurrentTime = Calendar.getInstance()
            var hour: Int
            val minute: Int
            if (existingTimeBlock == null) {
                hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
                minute = mcurrentTime.get(Calendar.MINUTE)
            } else {
                hour = Integer.parseInt(existingTimeBlock!!.startTime!!.substring(0, 2))
                minute = Integer.parseInt(existingTimeBlock!!.startTime!!.substring(3, 5))

                if (existingTimeBlock!!.startTime!!.toString().substring(6, 8) == "pm") {
                    hour += 12
                }
            }

            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(this@TimeBlockPage, TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                val time: String

                if (selectedHour > 12) {
                    time = String.format("%02d:%02d", selectedHour - 12, selectedMinute) + " pm"
                } else {
                    time = String.format("%02d:%02d", selectedHour, selectedMinute) + " am"
                }
                tv_BlockStartTime.text = time
            }, hour, minute, false) //No 24 hour time
            mTimePicker.setTitle("Select Start Time")
            mTimePicker.show()
        }

        tv_BlockEndTime.setOnClickListener {
            val mcurrentTime = Calendar.getInstance()
            var hour: Int
            val minute: Int
            if (existingTimeBlock == null) {
                hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
                minute = mcurrentTime.get(Calendar.MINUTE)
            } else {
                hour = Integer.parseInt(existingTimeBlock!!.endTime!!.substring(0, 2))
                minute = Integer.parseInt(existingTimeBlock!!.endTime!!.substring(3, 5))

                if (existingTimeBlock!!.startTime!!.substring(6, 8) == "pm") {
                    hour += 12
                }
            }

            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(this@TimeBlockPage, TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                val time: String

                if (selectedHour > 12) {
                    time = String.format("%02d:%02d", selectedHour - 12, selectedMinute) + " pm"
                } else {
                    if (selectedHour == 0) {
                        time = String.format("%02d:%02d", 12, selectedMinute) + " am"
                    } else {
                        time = String.format("%02d:%02d", selectedHour, selectedMinute) + " am"
                    }

                }
                tv_BlockEndTime.text = time
            }, hour, minute, false)//No 24 hour time
            mTimePicker.setTitle("Select Start Time")
            mTimePicker.show()
        }
    }

    private fun populateFields() {
        Log.d("***DEBUG***", "inside populateFields")

        btnDeleteTimeBlock.visibility = View.VISIBLE

        et_name.setText(existingTimeBlock!!.name)
        et_description.setText(existingTimeBlock!!.description)
        //start and end times
        tv_BlockStartTime.text = existingTimeBlock!!.startTime
        tv_BlockEndTime.text = existingTimeBlock!!.endTime
        btnDeleteTimeBlock.setOnClickListener { view -> onClickDeleteTimeBlock(view, existingTimeBlock) }
    }

    fun onClickSaveTimeBlock(v: View) {
        Log.d("***DEBUG***", "inside onClickSaveBlockTime")

        var startDate = Date()
        var endDate = Date()

        val formatter = SimpleDateFormat("hh:mm a")
        try {
            startDate = formatter.parse(tv_BlockStartTime.text.toString())
            endDate = formatter.parse(tv_BlockEndTime.text.toString())
        } catch (e: Exception) {

        }

        val timeblock = TimeBlock()
        timeblock.userId = "test123"

        if (TextUtils.isEmpty(et_name.text)) {
            et_name.error = "Name is required!"
        }
        if (tv_BlockStartTime.text.toString() == "Start Time") {
            tv_BlockStartTime.error = "Start time is required!"
        }
        if (tv_BlockEndTime.text.toString() == "End Time") {
            tv_BlockEndTime.error = "End time is required!"
        }

        if (et_name.error == null && tv_BlockStartTime.error == null && tv_BlockEndTime.error == null) {
            timeblock.name = et_name.text.toString()
            timeblock.description = et_description.text.toString()
            timeblock.startTime = tv_BlockStartTime.text.toString()
            timeblock.endTime = tv_BlockEndTime.text.toString()

            Log.d("***DEBUG***", "Before conversion: " + timeblock.startTime!!)
            timeblock.startTimestamp = TimeConverter.convertToGMTFromDisplay(timeblock.startTime!!)
            Log.d("***DEBUG***", "After conversion: " + timeblock.startTimestamp!!)
            timeblock.endTimestamp = TimeConverter.convertToGMTFromDisplay(timeblock.endTime!!)

            if (existingTimeBlock != null) {
                timeblock.id = existingTimeBlock!!.id
                dbh.updateTimeBlock(timeblock)
            } else {
                dbh.createTimeBlock(timeblock)
            }

            val mainActivity = Intent(this, MainActivity::class.java)
            startActivity(mainActivity)
        }

    }

    fun onClickDeleteTimeBlock(v: View, tb: TimeBlock?) {
        dbh.deleteTimeBlock(v, tb!!)
        finishActivity(Integer.parseInt(APP_CREATE))
        finish()
    }

}