package com.asav.flexis

import android.content.Context
import android.util.Log
import android.view.View

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

import android.content.ContentValues.TAG

class DatabaseHandler {

    internal var db: FirebaseFirestore

    init {
        Log.d("***DEBUG***", "inside databasehandler constructor")
        db = FirebaseFirestore.getInstance()
    }

    fun getObjectivesForMainActivity(context: Context) {
        Log.d("***DEBUG***", "inside getObjectivesForMainActivity")

        val MA = context as MainActivity
        val objectivesMap = mutableMapOf<String, Objective>()

        db.collection("objectives")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            //Log.d("***DEBUG***", document.getId() + " => " + document.getData());
                            val obj = document.toObject(Objective::class.java)
                            objectivesMap[obj.id!!] = obj

                        }
                        MA.setObjectivesMap(objectivesMap)

                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }

    fun getObjectivesForTimeBlock(context: Context, timeBlock: TimeBlock) {
        Log.d("***DEBUG***", "inside getObjectivesForTimeBlock")

        val MA = context as MainActivity
        val objectivesMap = mutableMapOf<String, Objective>()

        db.collection("objectives").whereEqualTo("timeblockId", timeBlock.id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d("***DEBUG***", document.id + " => " + document.data)
                            val obj = document.toObject(Objective::class.java)
                            obj.isComplete = false
                            objectivesMap[obj.id!!] = obj
                        }
                        MA.setObjectivesMap(objectivesMap)
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }


    fun createObjective(obj: Objective) {
        Log.d("***DEBUG***", "inside createObjectives")

        val objective = mutableMapOf<String, Any?>()
        objective["userId"] = obj.userId
        objective["name"] = obj.name
        objective["description"] = obj.description
        objective["duration"] = obj.duration
        objective["effort"] = obj.effort
        objective["frequency"] = obj.frequency
        objective["timeblock"] = obj.timeblock
        objective["timeblockId"] = obj.timeblockId


        // Add a new document with a generated ID
        db.collection("objectives")
                .add(objective)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id)
                    updateItemID(documentReference.id, "objectives")
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    fun updateObjective(obj: Objective) {
        Log.d("***DEBUG***", "inside updateObjectives")

        val objective = mutableMapOf<String, Any?>()
        objective["userId"] = obj.userId
        objective["name"] = obj.name
        objective["description"] = obj.description
        objective["duration"] = obj.duration
        objective["effort"] = obj.effort
        objective["frequency"] = obj.frequency
        objective["timeblock"] = obj.timeblock
        objective["timeblockId"] = obj.timeblockId
        objective["isComplete"] = obj.isComplete
        objective["dateCompleted"] = obj.dateCompleted

        // Update document with a generated ID
        db.collection("objectives").document(obj.id!!)
                .update(objective)
                .addOnSuccessListener {
                    Log.d("***Debug***", "DocumentSnapshot successfully updated!")
                    //Toast.makeText(context, "Updated " + wr.name, Toast.LENGTH_SHORT).show();
                }
                .addOnFailureListener { e ->
                    Log.w("***Debug***", "Error updating document", e)
                    //Toast.makeText(context, "Failed to update " + wr.name, Toast.LENGTH_SHORT).show();
                }
    }

    fun updateItemID(id: String, collection: String) {
        Log.d("***DEBUG***", "inside updateItemId")

        // Create a new user with a first and last name
        val update = mutableMapOf<String, Any?>()
        update["id"] = id

        // Add a new document with a generated ID
        db.collection(collection)
                .document(id)
                .update(update)
    }

    fun getTimeBlocksForObjectivePage(context: Context) {
        Log.d("***DEBUG***", "inside getTimeBlocksForObjectivePage")

        val OP = context as ObjectivePage
        val timeblocksMap = mutableMapOf<String?, TimeBlock>()

        db.collection("timeblocks")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d("***DEBUG***", document.id + " => " + document.data)
                            val tb = document.toObject(TimeBlock::class.java)
                            timeblocksMap[tb.id] = tb
                        }
                        OP.populateTimeBlocks(timeblocksMap)
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }

    fun getTimeBlocksForMainActivity(context: Context) {
        Log.d("***DEBUG***", "inside getTimeBlocksForMainActivity")

        val MA = context as MainActivity
        val timeblocksMap = mutableMapOf<String, TimeBlock>()

        val now = Date()
        val strDateFormat = "hh:mm a"
        val dateFormat = SimpleDateFormat(strDateFormat)
        val formattedDate = dateFormat.format(now)

        Log.d("***DEBUG***", "formattedDate: $formattedDate")

        db.collection("timeblocks")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d("***DEBUG***", document.id + " => " + document.data)
                            val tb = document.toObject(TimeBlock::class.java)
                            timeblocksMap[tb.id!!] = tb
                        }
                        MA.setTimeBlocksMap(timeblocksMap)
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }

    fun getCurrentTimeBlock(context: Context) {
        Log.d("***DEBUG***", "inside getTimeBlocksForObjectivePage")

        val MA = context as MainActivity

        db.collection("timeblocks").whereEqualTo("name", "Night")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var tb: TimeBlock? = null
                        for (document in task.result!!) {
                            Log.d("***DEBUG***", document.id + " => " + document.data)
                            tb = document.toObject(TimeBlock::class.java)
                        }
                        if (tb != null) {
                            MA.setCurrentTimeBlock(tb)
                        }

                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }

    fun createTimeBlock(tb: TimeBlock) {
        Log.d("***DEBUG***", "inside createTimeBlock")

        // Create a new user with a first and last name
        val timeblock = mutableMapOf<String, Any?>()
        timeblock["userId"] = tb.userId
        timeblock["name"] = tb.name
        timeblock["description"] = tb.description

        //convert display times to military times
        //convertToMilitary();

        timeblock["startTime"] = tb.startTime
        timeblock["endTime"] = tb.endTime
        timeblock["startTimestamp"] = tb.startTimestamp
        timeblock["endTimestamp"] = tb.endTimestamp

        // Add a new document with a generated ID
        db.collection("timeblocks")
                .add(timeblock)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id)
                    updateItemID(documentReference.id, "timeblocks")
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    fun deleteTimeBlock(v: View, tb: TimeBlock) {
        Log.d("***DEBUG***", "inside deleteTimeBlock")

        val idToDelete: String = tb.id!!

        db.collection("timeblocks")
                .document(idToDelete)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!")

                    //delete id from objectives
                    removeTimeBlockIdReferences(tb)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    fun deleteObjective(v: View, obj: Objective) {
        Log.d("***DEBUG***", "inside deleteObjective")

        db.collection("objectives")
                .document(obj.id!!)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

    }

    private fun removeTimeBlockIdReferences(tb: TimeBlock) {
        Log.d("***DEBUG***", "inside removeTimeBlockIdReferences")

        //query all objectives where timeblockid is param
        //update id to null
        db.collection("objectives").whereEqualTo("timeblockId", tb.id)
                .get()
                .addOnCompleteListener { task ->
                    val objList = ArrayList<Objective>()
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d("***DEBUG***", document.id + " => " + document.data)
                            val obj = document.toObject(Objective::class.java)
                            objList.add(obj)
                        }

                        if (!objList.isEmpty()) {
                            updateObjectives(objList)
                        }

                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }

    }

    fun updateObjectives(objList: List<Objective>) {
        Log.d("***DEBUG***", "inside updateObjectives")

        for (o in objList) {

            val objective = mutableMapOf<String, Any?>()
            objective["timeblockid"] = null
            objective["timeblock"] = null

            db.collection("objectives").document(o.id!!)
                    .update(objective)
                    .addOnSuccessListener {
                        Log.d("***Debug***", "DocumentSnapshot successfully updated!")
                        //Toast.makeText(context, "Updated " + wr.name, Toast.LENGTH_SHORT).show();
                    }
                    .addOnFailureListener { e ->
                        Log.w("***Debug***", "Error updating document", e)
                        //Toast.makeText(context, "Failed to update " + wr.name, Toast.LENGTH_SHORT).show();
                    }
        }

    }

    fun updateTimeBlock(tb: TimeBlock) {
        Log.d("***DEBUG***", "inside updateTimeBlock")

        val timeblock = mutableMapOf<String, Any?>()
        timeblock["userId"] = tb.userId
        timeblock["name"] = tb.name
        timeblock["description"] = tb.description

        //convert display times to military times
        //convertToMilitary();

        timeblock["startTime"] = tb.startTime
        timeblock["endTime"] = tb.endTime
        timeblock["startTimestamp"] = tb.startTimestamp
        timeblock["endTimestamp"] = tb.endTimestamp

        db.collection("timeblocks").document(tb.id!!)
                .update(timeblock)
                .addOnSuccessListener {
                    Log.d("***Debug***", "DocumentSnapshot successfully updated!")
                    //Toast.makeText(context, "Updated " + wr.name, Toast.LENGTH_SHORT).show();
                }
                .addOnFailureListener { e ->
                    Log.w("***Debug***", "Error updating document", e)
                    //Toast.makeText(context, "Failed to update " + wr.name, Toast.LENGTH_SHORT).show();
                }

    }

}
