package com.asav.flexis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import static com.asav.flexis.MainActivity.APP_EDIT;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.content.ContentValues.TAG;

public class ObjectiveList extends AppCompatActivity {

    String timeblockId = "";
    TimeBlock timeBlock = null;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DatabaseHandler dbh = new DatabaseHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("***DEBUG***", "inside onCreate ObjectiveList");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_objectivelist);

        Objective objective = null;

        //Set<String> categories = getIntent().getCategories();
        //getIntent().getExtras();

        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                //user = extras.getParcelable("user");
                timeblockId = extras.getString("TimeblockId");
                Log.d("***DEBUG***", "objList timeblockId: " + timeblockId);
            }
        }

        getObjectives();
        getTimeBlock(timeblockId);

    }

    private void drawObjectives(List<Objective> objList) {
        Log.d("***DEBUG***", "inside drawObjectives");

        //get root layout, to get scrollview's linearlayout
        //get all objectives for user and populate cardview for each

        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout svLLObjlist = findViewById(R.id.ll_objlist_root).findViewById(R.id.sv_objlist).findViewById(R.id.sv_ll_objlist);


        for (Objective obj : objList) {
            //setup layout stuff

            final View objectiveCard = inflater.inflate(R.layout.objective_card_component, null, false);
            objectiveCard.setPadding(0, 0, 0, 10);
            objectiveCard.setTag(obj);

            //initialize the layout fields
            TextView objectiveName = objectiveCard.findViewById(R.id.tv_objCard_Name);
            //TextView objectiveDescription =  objectiveCard.findViewById(R.id.);
            TextView objectiveDuration = objectiveCard.findViewById(R.id.tv_objCard_Duration);
            TextView objectiveEffort = objectiveCard.findViewById(R.id.tv_objCard_Effort);
            //TextView objectiveFrequency =  objectiveCard.findViewById(R.id.tv_objCard_Frequency);

            //set values of imported components
            objectiveName.setText(obj.name);
            //objectiveDescription.setText(obj.description);
            String objDuration = "0";
            if (obj.duration != null && !obj.duration.equals("")) {
                objDuration = parseDuration(Integer.parseInt(obj.duration));
            }

            objectiveDuration.setText(objDuration);
            objectiveEffort.setText(obj.effort);
            objectiveCard.setOnClickListener(onClickAddToTimeBlock);

            //objectiveFrequency.setText(obj.frequency);

            //Add margins to card for spacing, height, width
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(0, 5, 0, 5);
            objectiveCard.setLayoutParams(params);
            svLLObjlist.addView(objectiveCard);

            objectiveCard.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            //objectivesViewMap.put(objectiveCard, obj);
        }
    }

    private View.OnClickListener onClickAddToTimeBlock = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("***DEBUG***", "inside onClickAddToTimeBlock");

            Objective o = (Objective) v.getTag();
            if(timeblockId != null) {
                o.timeblockId = timeblockId;
            }
            if(timeBlock != null) {
                o.timeblock = timeBlock;
            }

            dbh.createObjective(o);
            finishActivity(Integer.parseInt(APP_EDIT));
            finish();
        }
    };

    public void setObjectivesList(List<Objective> objList) {
        drawObjectives(objList);
    }

    private void getObjectives() {
        db.collection("objectives")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Objective> objlist = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("***DEBUG***", document.getId() + " => " + document.getData());
                                Objective obj = document.toObject(Objective.class);

                                objlist.add(obj);
                            }
                            setObjectivesList(objlist);

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private String parseDuration(int duration) {

        int minutes = duration % 60;
        int hours = duration / 60;
        int seconds = 0;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void setCurrentTimeBlock(TimeBlock tb) {
        this.timeBlock = tb;
    }

    public void getTimeBlock(String timeblockId) {
        Log.d("***DEBUG***", "inside getTimeBlocksForObjectivePage");

        db.collection("timeblocks").whereEqualTo("id", timeblockId)
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
                                setCurrentTimeBlock(tb);
                            }

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }


}
