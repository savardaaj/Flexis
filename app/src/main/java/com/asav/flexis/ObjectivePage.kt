package com.asav.flexis

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner

import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

import com.asav.flexis.MainActivity.Companion.APP_EDIT

abstract class ObjectivePage : AppCompatActivity() {

    internal var freqArray = arrayOf("1 Day", "2 Days", "3 Days", "4 Days", "5 Days", "6 Days", "7 Days")

    internal var db = FirebaseFirestore.getInstance()
    internal var dbh = DatabaseHandler()

    internal var timeBlockIdMap: MutableMap<String?, TimeBlock> = mutableMapOf()
    internal var timeBlockNameMap: MutableMap<String?, TimeBlock> = mutableMapOf()
    internal var timeblockNameList: MutableList<String> = mutableListOf()

    internal abstract var cl: ConstraintLayout
    internal abstract var et_name: EditText
    internal abstract var et_description: EditText
    internal abstract var et_hours: EditText
    internal abstract var et_minutes: EditText
    internal abstract var et_effort: EditText
    internal abstract var sp_Frequency: Spinner
    internal abstract var sp_TimeBlock: Spinner
    internal abstract var deleteObjective: Button

    internal var category = ""
    internal var timeblockId: String? = null

    internal var existingObj: Objective? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("***DEBUG***", "inside onCreate ObjectivePage")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_objectivepage)
        initializeLayout()

        val objective: Objective?

        val categories = intent.categories
        intent.extras

        val intent = intent
        if (intent != null) {
            val extras = intent.extras
            if (extras != null) {
                //user = extras.getParcelable("user");
                val JSON = extras.getString("details")
                timeblockId = extras.getString("TimeblockId")
                objective = Gson().fromJson(JSON, Objective::class.java)
                if (categories != null && categories.contains(APP_EDIT)) {
                    category = APP_EDIT
                    existingObj = objective
                }
            }
        }
    }

    //map of ID, timeblock
    fun populateTimeBlocks(timeblocksMap: MutableMap<String?, TimeBlock>) {
        Log.d("***DEBUG***", "inside populateTImeBlocks")

        this.timeblockNameList = ArrayList()
        this.timeBlockIdMap = timeblocksMap


        //iterate over loop, get all names.
        for (tb in timeblocksMap.values) {
            if (tb.name != null) {
                tb.name?.let(this.timeblockNameList::add) //safe call
                this.timeBlockNameMap[tb.name] = tb
            }
        }

        sp_TimeBlock = findViewById(R.id.sp_ObjTimeBlock)
        val timeBlockAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, this.timeblockNameList)
        timeBlockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_TimeBlock.adapter = timeBlockAdapter

        if (existingObj != null) {
            populateValues()

        } else if (timeblockId != null) {
            setDefaultTimeBlock()
        }
    }

    private fun initializeLayout() {
        Log.d("***DEBUG***", "inside setTodaysDate")

        dbh.getTimeBlocksForObjectivePage(this)

        cl = findViewById(R.id.cl_objectivePageContent)
        et_name = cl.findViewById(R.id.et_ObjName)
        et_description = cl.findViewById(R.id.et_ObjDescription)
        et_hours = cl.findViewById(R.id.et_ObjHours)
        et_minutes = cl.findViewById(R.id.et_ObjMinutes)
        et_effort = cl.findViewById(R.id.et_ObjEffort)
        sp_Frequency = findViewById(R.id.sp_ObjFrequency)
        et_name = cl.findViewById(R.id.et_ObjName)
        deleteObjective = cl.findViewById(R.id.btn_deleteTimeBlock)
        deleteObjective.visibility = View.INVISIBLE

        val freqAdapter = ArrayAdapter.createFromResource(this, R.array.frequency_array, android.R.layout.simple_spinner_item)
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_Frequency.adapter = freqAdapter
    }

    fun onClickSaveObjective(v: View) {
        Log.d("***DEBUG***", "inside onClickSaveObjective")

        val objective = Objective()
        objective.userId = "test123"

        if (TextUtils.isEmpty(et_name.text)) {
            et_name.error = "Name is required!"
        }
        if (TextUtils.isEmpty(et_hours.text) && TextUtils.isEmpty(et_minutes.text)) {
            et_hours.error = "Some duration is required!"
            et_minutes.error = "Some duration is required!"
        }

        //Don't move on if there are errors
        if (et_name.error == null && et_hours.error == null && et_minutes.error == null) {
            val hours: Int
            val mins: Int
            objective.name = et_name.text.toString()
            objective.description = et_description.text.toString()
            if (et_hours.text.toString() != "") {
                hours = Integer.valueOf(et_hours.text.toString()) * 60
            } else {
                hours = 0
            }
            if (et_minutes.text.toString() != "") {
                mins = Integer.valueOf(et_minutes.text.toString())
            } else {
                mins = 0
            }
            objective.duration = (hours + mins).toString() + "" //convert to minutes
            objective.effort = et_effort.text.toString()
            objective.frequency = sp_Frequency.selectedItem.toString()

            objective.timeblock = timeBlockNameMap[sp_TimeBlock.selectedItem.toString()]
            objective.timeblockId = timeBlockNameMap[sp_TimeBlock.selectedItem.toString()]?.id

            if (category == APP_EDIT) {
                objective.id = existingObj!!.id
                dbh.updateObjective(objective)
            } else {
                dbh.createObjective(objective)
            }

            val mainActivity = Intent(this, MainActivity::class.java)
            startActivity(mainActivity)
        }

    }

    fun onClickDeleteObjective(v: View, obj: Objective?) {
        dbh.deleteObjective(v, obj!!)
        val mainActivity = Intent(this, MainActivity::class.java)
        startActivity(mainActivity)
    }

    private fun populateValues() {
        Log.d("***DEBUG***", "inside populateValues")

        val hours = (Integer.valueOf(existingObj!!.duration!!) / 60).toString()
        val minutes = (Integer.valueOf(existingObj!!.duration!!) % 60).toString()

        val timeblockId: String? = timeBlockIdMap[existingObj!!.timeblock!!.id]?.name

        et_name.setText(existingObj!!.name)
        et_description.setText(existingObj!!.description)
        et_hours.setText(hours)
        et_minutes.setText(minutes)
        et_effort.setText(existingObj!!.effort)
        //sp_TimeBlock.setSelection(Arrays.asList<List<String>>(timeblockNameList).indexOf(timeblockId))
        sp_Frequency.setSelection(Arrays.asList(*freqArray).indexOf(existingObj!!.frequency))

        deleteObjective.visibility = View.VISIBLE

        deleteObjective.setOnClickListener { view -> onClickDeleteObjective(view, existingObj) }
    }

    private fun setDefaultTimeBlock() {
        Log.d("***DEBUG***", "inside setDefaultTimeBlock")

        sp_TimeBlock.setSelection(timeblockNameList.indexOf(timeBlockIdMap[timeblockId]?.name))
    }

}
