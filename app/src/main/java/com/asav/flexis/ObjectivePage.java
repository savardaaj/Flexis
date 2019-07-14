package com.asav.flexis;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static com.asav.flexis.MainActivity.APP_EDIT;
import static org.xmlpull.v1.XmlPullParser.TYPES;

public class ObjectivePage extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DatabaseHandler dbh = new DatabaseHandler();

    Map<String, TimeBlock> timeBlocksMap;
    String[] timeblockList;

    ConstraintLayout cl;
    EditText et_name, et_description, et_duration, et_effort;
    Spinner sp_Frequency;
    Spinner sp_TimeBlock;

    String category = "";
    String timeblockName = null;

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
                timeblockName = extras.getString("TimeblockName");
                Log.d("***DEBUG***", "GET IT onCreate ObjectivePage" + timeblockName);
                objective = new Gson().fromJson(JSON, Objective.class);
                if(categories != null && categories.contains(APP_EDIT)) {
                    category = APP_EDIT;
                    existingObj = objective;
                }
            }
        }
    }

    public void populateTimeBlocks(Map<String, TimeBlock> timeblocksMap) {
        Log.d("***DEBUG***", "inside populateTImeBlocks");
        this.timeBlocksMap = timeblocksMap;
        this.timeblockList = timeblocksMap.keySet().toArray(new String[timeblocksMap.keySet().size()]);

        sp_TimeBlock = findViewById(R.id.sp_ObjTimeBlock);
        ArrayAdapter<String> timeBlockAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, this.timeblockList);
        timeBlockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_TimeBlock.setAdapter(timeBlockAdapter);

        Log.d("***DEBUG***", "inside timeblockname: " + timeblockName);

        if(existingObj != null) {
            populateValues();
        }
        else if(timeblockName != null) {
            setDefaultTimeBlock();
        }
    }

    private void initializeLayout() {
        Log.d("***DEBUG***", "inside initializeLayout");

        dbh.getTimeBlocksForObjectivePage(this);

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
        objective.timeblock = timeBlocksMap.get(sp_TimeBlock.getSelectedItem().toString());
        objective.timeblockId = timeBlocksMap.get(sp_TimeBlock.getSelectedItem().toString()).id;

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

    private void populateValues() {
        Log.d("***DEBUG***", "inside populateValues");

        et_name.setText(existingObj.name);
        et_description.setText(existingObj.description);
        et_duration.setText(existingObj.duration);
        et_effort.setText(existingObj.effort);
        //sp_Frequency.setSelection(existingObj.frequency.);
        sp_TimeBlock.setSelection(Arrays.asList(timeblockList).indexOf(timeBlocksMap.get(existingObj.timeblock.name).name));
    }

    private void setDefaultTimeBlock() {
        Log.d("***DEBUG***", "inside setDefaultTimeBlock");
        sp_TimeBlock.setSelection(Arrays.asList(timeblockList).indexOf(timeBlocksMap.get(timeblockName).name));
    }

}
