package com.asav.flexis;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectivePage extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DatabaseHandler dbh = new DatabaseHandler();

    Map<String, TimeBlock> timeBlocksMap;

    ConstraintLayout cl;
    EditText et_name, et_description, et_duration, et_effort;
    Spinner sp_Frequency;
    Spinner sp_TimeBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("***DEBUG***", "inside onCreate ObjectivePage");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objectivepage);



        initializeLayout();


    }

    public void populateTimeBlocks(Map<String, TimeBlock> timeblocksMap) {
        Log.d("***DEBUG***", "inside populateTImeBlocks");
        this.timeBlocksMap = timeblocksMap;
        String[] blockList = timeblocksMap.keySet().toArray(new String[timeblocksMap.keySet().size()]);

        sp_TimeBlock = findViewById(R.id.sp_ObjTimeBlock);
        ArrayAdapter<String> timeBlockAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, blockList);
        timeBlockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_TimeBlock.setAdapter(timeBlockAdapter);
    }

    private void initializeLayout() {
        Log.d("***DEBUG***", "inside initializeLayout");

        dbh.getTimeBlocks(this);

        cl = findViewById(R.id.cl_objectivePage);
        et_name = cl.findViewById(R.id.et_ObjName);
        et_description = cl.findViewById(R.id.et_ObjDescription);
        et_duration = cl.findViewById(R.id.et_ObjDuration);
        et_effort = cl.findViewById(R.id.et_ObjEffort);
        sp_Frequency = findViewById(R.id.sp_ObjFrequency);
        et_name = cl.findViewById(R.id.et_ObjName);

        ArrayAdapter<CharSequence> freqAdapter = ArrayAdapter.createFromResource(this,R.array.frequency_array, android.R.layout.simple_spinner_item);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_Frequency.setAdapter(freqAdapter);
    }

    public void onClickSaveObjective(View v) {
        Log.d("***DEBUG***", "inside onClickSaveObjective");

        Objective objective = new Objective();
        objective.userId = "test123";
        objective.name = et_name.getText().toString();
        objective.description = et_description.getText().toString();
        objective.duration = et_duration.getText().toString();
        objective.effort = et_effort.getText().toString();
        objective.frequency = sp_Frequency.getSelectedItem().toString();
        objective.timeblock = timeBlocksMap.get(sp_Frequency.getSelectedItem().toString());

        dbh.createObjective(objective);

        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
    }

}
