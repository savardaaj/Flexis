package com.asav.flexis;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class DatabaseHandler {

    FirebaseFirestore db;

    public DatabaseHandler () {
        Log.d("***DEBUG***", "inside databasehandler constructor");

        db = FirebaseFirestore.getInstance();
    }

    public void getObjectivesForMainActivity(Context context) {
        Log.d("***DEBUG***", "inside getObjectivesForMainActivity");

        final MainActivity MA = (MainActivity) context;
        final Map<String, Objective> objectivesMap = new HashMap<>();

        db.collection("objectives")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("***DEBUG***", document.getId() + " => " + document.getData());
                                Objective obj = document.toObject(Objective.class);
                                obj.isComplete = false;
                                objectivesMap.put(obj.id, obj);
                            }
                            MA.setObjectivesMap(objectivesMap);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void getObjectivesForTimeBlock(Context context, TimeBlock timeBlock) {
        Log.d("***DEBUG***", "inside getObjectivesForTimeBlock");

        final MainActivity MA = (MainActivity) context;
        final Map<String, Objective> objectivesMap = new HashMap<>();

        db.collection("objectives").whereEqualTo("timeblockId", timeBlock.id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("***DEBUG***", document.getId() + " => " + document.getData());
                                Objective obj = document.toObject(Objective.class);
                                obj.isComplete = false;
                                objectivesMap.put(obj.id, obj);
                            }
                            MA.setObjectivesMap(objectivesMap);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }



    public void createObjective(Objective obj) {
        Log.d("***DEBUG***", "inside createObjectives");

        // Create a new user with a first and last name
        Map<String, Object> objective = new HashMap<>();
        objective.put("userId", obj.userId);
        objective.put("name", obj.name);
        objective.put("description", obj.description);
        objective.put("duration", obj.duration);
        objective.put("effort", obj.effort);
        objective.put("frequency", obj.frequency);
        objective.put("timeblock", obj.timeblock);
        objective.put("timeblockId", obj.timeblockId);


        // Add a new document with a generated ID
        db.collection("objectives")
                .add(objective)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        updateItemID(documentReference.getId(), "objectives");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void updateObjective(Objective obj) {
        Log.d("***DEBUG***", "inside createObjectives");

        Map<String, Object> objective = new HashMap<>();
        objective.put("userId", obj.userId);
        objective.put("name", obj.name);
        objective.put("description", obj.description);
        objective.put("duration", obj.duration);
        objective.put("effort", obj.effort);
        objective.put("frequency", obj.frequency);
        objective.put("timeblock", obj.timeblock);
        objective.put("timeblockId", obj.timeblockId);

        // Update document with a generated ID
        db.collection("objectives").document(obj.id)
                .update(objective)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("***Debug***", "DocumentSnapshot successfully updated!");
                        //Toast.makeText(context, "Updated " + wr.name, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("***Debug***", "Error updating document", e);
                        //Toast.makeText(context, "Failed to update " + wr.name, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void updateItemID(String id, String collection) {
        Log.d("***DEBUG***", "inside updateItemId");

        // Create a new user with a first and last name
        Map<String, Object> update = new HashMap<>();
        update.put("id", id);

        // Add a new document with a generated ID
        db.collection(collection)
            .document(id)
            .update(update);
    }

    public void getTimeBlocksForObjectivePage(Context context) {
        Log.d("***DEBUG***", "inside getTimeBlocksForObjectivePage");

        final ObjectivePage OP = (ObjectivePage) context;
        final Map<String, TimeBlock> timeblocksMap = new HashMap<>();

        db.collection("timeblocks")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("***DEBUG***", document.getId() + " => " + document.getData());
                                TimeBlock tb = document.toObject(TimeBlock.class);
                                timeblocksMap.put(tb.name, tb);
                            }
                            OP.populateTimeBlocks(timeblocksMap);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void getTimeBlocksForMainActivity(Context context) {
        Log.d("***DEBUG***", "inside getTimeBlocksForMainActivity");

        final MainActivity MA = (MainActivity) context;
        final Map<String, TimeBlock> timeblocksMap = new HashMap<>();

        db.collection("timeblocks")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("***DEBUG***", document.getId() + " => " + document.getData());
                                TimeBlock tb = document.toObject(TimeBlock.class);
                                timeblocksMap.put(tb.id, tb);
                            }
                            MA.setTimeBlocksMap(timeblocksMap);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void getCurrentTimeBlock(Context context) {
        Log.d("***DEBUG***", "inside getTimeBlocksForObjectivePage");

        final MainActivity MA = (MainActivity) context;

        db.collection("timeblocks").whereEqualTo("name", "Night")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            TimeBlock tb = null;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("***DEBUG***", document.getId() + " => " + document.getData());
                                tb = document.toObject(TimeBlock.class);
                            }
                            if(tb != null) {
                                MA.setCurrentTimeBlock(tb);
                            }

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void createTimeBlock(TimeBlock tb) {
        Log.d("***DEBUG***", "inside createTimeBlock");

        // Create a new user with a first and last name
        Map<String, Object> timeblock = new HashMap<>();
        timeblock.put("userId", tb.userId);
        timeblock.put("name", tb.name);
        timeblock.put("description", tb.description);
        timeblock.put("startTime", tb.startTime);
        timeblock.put("endTime", tb.endTime);

        // Add a new document with a generated ID
        db.collection("timeblocks")
                .add(timeblock)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        updateItemID(documentReference.getId(), "timeblocks");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void deleteTimeBLocks() {


    }
}
