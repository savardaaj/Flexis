package com.asav.flexis

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.util.ArrayList

import android.content.ContentValues.TAG
import com.asav.flexis.MainActivity.Companion.APP_EDIT

class ObjectiveList : AppCompatActivity() {

    internal var timeblockId: String? = ""
    internal var timeBlock: TimeBlock? = null
    internal var db = FirebaseFirestore.getInstance()
    internal var dbh = DatabaseHandler()

    private val onClickAddToTimeBlock = View.OnClickListener { v ->
        Log.d("***DEBUG***", "inside onClickAddToTimeBlock")

        val o = v.tag as Objective
        if (timeblockId != null) {
            o.timeblockId = timeblockId
        }
        if (timeBlock != null) {
            o.timeblock = timeBlock
        }

        dbh.createObjective(o)
        finishActivity(Integer.parseInt(APP_EDIT))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("***DEBUG***", "inside onCreate ObjectiveList")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_objectivelist)

        val objective: Objective? = null

        //Set<String> categories = getIntent().getCategories();
        //getIntent().getExtras();

        val intent = intent
        if (intent != null) {
            val extras = intent.extras
            if (extras != null) {
                //user = extras.getParcelable("user");
                timeblockId = extras.getString("TimeblockId")
                Log.d("***DEBUG***", "objList timeblockId: " + timeblockId!!)
            }
        }

        getObjectives()
        getTimeBlock(timeblockId)

    }

    private fun drawObjectives(objList: List<Objective>) {
        Log.d("***DEBUG***", "inside drawObjectives")

        //get root layout, to get scrollview's linearlayout
        //get all objectives for user and populate cardview for each

        val inflater = LayoutInflater.from(this)
        val svLLObjlist = findViewById<View>(R.id.ll_objlist_root).findViewById<View>(R.id.sv_objlist).findViewById<LinearLayout>(R.id.sv_ll_objlist)


        for (obj in objList) {
            //setup layout stuff

            val objectiveCard = inflater.inflate(R.layout.objective_card_component, null, false)
            objectiveCard.setPadding(0, 0, 0, 10)
            objectiveCard.tag = obj

            //initialize the layout fields
            val objectiveName = objectiveCard.findViewById<TextView>(R.id.tv_objCard_Name)
            //TextView objectiveDescription =  objectiveCard.findViewById(R.id.);
            val objectiveDuration = objectiveCard.findViewById<TextView>(R.id.tv_objCard_Duration)
            val objectiveEffort = objectiveCard.findViewById<TextView>(R.id.tv_objCard_Effort)
            //TextView objectiveFrequency =  objectiveCard.findViewById(R.id.tv_objCard_Frequency);

            //set values of imported components
            objectiveName.text = obj.name
            //objectiveDescription.setText(obj.description);
            var objDuration = "0"
            if (obj.duration != null && obj.duration != "") {
                objDuration = parseDuration(Integer.parseInt(obj.duration!!))
            }

            objectiveDuration.text = objDuration
            objectiveEffort.text = obj.effort
            objectiveCard.setOnClickListener(onClickAddToTimeBlock)

            //objectiveFrequency.setText(obj.frequency);

            //Add margins to card for spacing, height, width
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.setMargins(0, 5, 0, 5)
            objectiveCard.layoutParams = params
            svLLObjlist.addView(objectiveCard)

            objectiveCard.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            //objectivesViewMap.put(objectiveCard, obj);
        }
    }

    fun setObjectivesList(objList: List<Objective>) {
        drawObjectives(objList)
    }

    private fun getObjectives() {
        db.collection("objectives")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val objlist = ArrayList<Objective>()
                        for (document in task.result!!) {
                            Log.d("***DEBUG***", document.id + " => " + document.data)
                            val obj = document.toObject(Objective::class.java)

                            objlist.add(obj)
                        }
                        setObjectivesList(objlist)

                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }

    private fun parseDuration(duration: Int): String {

        val minutes = duration % 60
        val hours = duration / 60
        val seconds = 0
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun setCurrentTimeBlock(tb: TimeBlock?) {
        this.timeBlock = tb
    }

    fun getTimeBlock(timeblockId: String?) {
        Log.d("***DEBUG***", "inside getTimeBlocksForObjectivePage")

        db.collection("timeblocks").whereEqualTo("id", timeblockId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var tb: TimeBlock? = null
                        for (document in task.result!!) {
                            Log.d("***DEBUG***", document.id + " => " + document.data)
                            tb = document.toObject(TimeBlock::class.java)
                        }
                        if (tb != null) {
                            setCurrentTimeBlock(tb)
                        }

                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }
}
