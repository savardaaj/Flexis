package com.asav.flexis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    EditText et_name, et_description, et_duration, et_effort, et_frequency;
    Spinner sp_Frequency;
    Spinner sp_TimeBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("***DEBUG***", "inside onCreate ObjectivePage");
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_objectivepage);



        initializeLayout();


    }

    public void populateTimeBlocks(Map<String, TimeBlock> timeblocksMap) {
        Log.d("***DEBUG***", "inside populateTImeBlocks");
        this.timeBlocksMap = timeblocksMap;
        String[] blockList = timeblocksMap.keySet().toArray(new String[timeblocksMap.keySet().size()]);

        //sp_TimeBlock = findViewById(R.id.sp_TimeBlock);
        ArrayAdapter<String> timeBlockAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, blockList);
        timeBlockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_TimeBlock.setAdapter(timeBlockAdapter);
    }

    private void initializeLayout() {
        Log.d("***DEBUG***", "inside initializeLayout");

        dbh.getTimeBlocks(this);

        //sp_Frequency = findViewById(R.id.sp_Frequency);
        //ArrayAdapter<CharSequence> freqAdapter = ArrayAdapter.createFromResource(this,R.array.frequency_array, android.R.layout.simple_spinner_item);
        //freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //sp_Frequency.setAdapter(freqAdapter);



    }

    private void onClickSaveObjective() {
        Log.d("***DEBUG***", "inside onClickSaveObjective");

        Objective objective = new Objective();
        objective.userId = "test123";
        objective.name = et_name.getText().toString();
        objective.description = et_description.getText().toString();
        objective.duration = et_duration.getText().toString();
        objective.effort = et_effort.getText().toString();
        objective.frequency = et_frequency.getText().toString();

        dbh.createObjective(objective);
    }

}
