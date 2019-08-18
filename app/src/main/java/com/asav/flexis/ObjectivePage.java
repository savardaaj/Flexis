package com.asav.flexis;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.asav.flexis.MainActivity.APP_EDIT;

public class ObjectivePage extends AppCompatActivity {

    String[] freqArray = {"1 Day", "2 Days", "3 Days", "4 Days", "5 Days", "6 Days", "7 Days"};

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DatabaseHandler dbh = new DatabaseHandler();

    Map<String, TimeBlock> timeBlockIdMap;
    Map<String, TimeBlock> timeBlockNameMap = new HashMap<>();
    List<String> timeblockNameList;

    ConstraintLayout cl;
    EditText et_name, et_description, et_hours, et_minutes, et_effort;
    Spinner sp_Frequency;
    Spinner sp_TimeBlock;
    Button deleteObjective;

    String category = "";
    String timeblockId = null;

    Objective existingObj = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("***DEBUG***", "inside onCreate ObjectivePage");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_objectivepage);
        initializeLayout();

        Objective objective = null;

        Set<String> categories = getIntent().getCategories();
        getIntent().getExtras();

        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                //user = extras.getParcelable("user");
                String JSON = extras.getString("details");
                timeblockId = extras.getString("TimeblockId");
                objective = new Gson().fromJson(JSON, Objective.class);
                if(categories != null && categories.contains(APP_EDIT)) {
                    category = APP_EDIT;
                    existingObj = objective;
                }
            }
        }
    }

    //map of ID, timeblock
    public void populateTimeBlocks(Map<String, TimeBlock> timeblocksMap) {
        Log.d("***DEBUG***", "inside populateTImeBlocks");

        this.timeblockNameList = new ArrayList<>();
        this.timeBlockIdMap = timeblocksMap;


        //iterate over loop, get all names.
        for(TimeBlock tb : timeblocksMap.values()) {
            if(tb.name != null) {
                this.timeblockNameList.add(tb.name);
                this.timeBlockNameMap.put(tb.name, tb);
            }
        }

        sp_TimeBlock = findViewById(R.id.sp_ObjTimeBlock);
        ArrayAdapter<String> timeBlockAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, this.timeblockNameList);
        timeBlockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_TimeBlock.setAdapter(timeBlockAdapter);

        if(existingObj != null) {
            populateValues();

        }
        else if(timeblockId != null) {
            setDefaultTimeBlock();
        }
    }

    private void initializeLayout() {
        Log.d("***DEBUG***", "inside setTodaysDate");

        dbh.getTimeBlocksForObjectivePage(this);

        cl = findViewById(R.id.cl_objectivePageContent);
        et_name = cl.findViewById(R.id.et_ObjName);
        et_description = cl.findViewById(R.id.et_ObjDescription);
        et_hours = cl.findViewById(R.id.et_ObjHours);
        et_minutes = cl.findViewById(R.id.et_ObjMinutes);
        et_effort = cl.findViewById(R.id.et_ObjEffort);
        sp_Frequency = findViewById(R.id.sp_ObjFrequency);
        et_name = cl.findViewById(R.id.et_ObjName);
        deleteObjective = cl.findViewById(R.id.btn_deleteTimeBlock);
        deleteObjective.setVisibility(View.INVISIBLE);

        ArrayAdapter<CharSequence> freqAdapter = ArrayAdapter.createFromResource(this,R.array.frequency_array, android.R.layout.simple_spinner_item);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_Frequency.setAdapter(freqAdapter);
    }

    public void onClickSaveObjective(View v) {
        Log.d("***DEBUG***", "inside onClickSaveObjective");

        Objective objective = new Objective();
        objective.userId = "test123";

        if(TextUtils.isEmpty(et_name.getText())) {
            et_name.setError( "Name is required!" );
        }
        if (TextUtils.isEmpty(et_hours.getText()) && TextUtils.isEmpty(et_minutes.getText())) {
            et_hours.setError( "Some duration is required!");
            et_minutes.setError( "Some duration is required!");
        }

        //Don't move on if there are errors
        if(et_name.getError() == null && et_hours.getError() == null && et_minutes.getError() == null) {
            objective.name = et_name.getText().toString();
            objective.description = et_description.getText().toString();
            objective.duration = (Integer.valueOf(et_hours.getText().toString()) * 60 ) + Integer.valueOf(et_minutes.getText().toString()) + ""; //convert to minutes
            objective.effort = et_effort.getText().toString();
            objective.frequency = sp_Frequency.getSelectedItem().toString();

            objective.timeblock = timeBlockNameMap.get(sp_TimeBlock.getSelectedItem().toString());
            objective.timeblockId = timeBlockNameMap.get(sp_TimeBlock.getSelectedItem().toString()).id;

            if(category.equals(APP_EDIT)) {
                objective.id = existingObj.id;
                dbh.updateObjective(objective);
            }
            else {
                dbh.createObjective(objective);
            }

            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
        }

    }

    public void onClickDeleteObjective(View v, Objective obj) {
        dbh.deleteObjective(v, obj);
        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
    }

    private void populateValues() {
        Log.d("***DEBUG***", "inside populateValues");

        String hours = String.valueOf(Integer.valueOf(existingObj.duration) / 60);
        String minutes = String.valueOf(Integer.valueOf(existingObj.duration) % 60);

        et_name.setText(existingObj.name);
        et_description.setText(existingObj.description);
        et_hours.setText(hours);
        et_minutes.setText(minutes);
        et_effort.setText(existingObj.effort);
        sp_TimeBlock.setSelection(Arrays.asList(timeblockNameList).indexOf(timeBlockIdMap.get(existingObj.timeblock.id).name));
        sp_Frequency.setSelection(Arrays.asList(freqArray).indexOf(existingObj.frequency));

        deleteObjective.setVisibility(View.VISIBLE);

        deleteObjective.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickDeleteObjective(view, existingObj);
            }
        });
    }

    private void setDefaultTimeBlock() {
        Log.d("***DEBUG***", "inside setDefaultTimeBlock");

        sp_TimeBlock.setSelection(timeblockNameList.indexOf(timeBlockIdMap.get(timeblockId).name));
    }

}
