package com.asav.flexis;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.Timestamp;
import com.google.gson.Gson;

//import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.Date;

import static com.asav.flexis.MainActivity.APP_CREATE;
import static com.asav.flexis.MainActivity.APP_EDIT;

public class TimeBlockPage extends AppCompatActivity {

    DatabaseHandler dbh = new DatabaseHandler();

    TimeBlock existingTimeBlock = null;

    ConstraintLayout cl;
    EditText et_name, et_description;

    String category = "";

    TextView tv_BlockStartTime, tv_BlockEndTime;
    Button btnDeleteTimeBlock;

    TimeBlock timeblock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("***DEBUG***", "inside onCreate TimeBlockPage");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blockpage);

        initializeLayout();
        setupTimePickers();

        Set<String> categories = getIntent().getCategories();

        getIntent().getExtras();

        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                //user = extras.getParcelable("user");
                String JSON = extras.getString("details");
                 timeblock = new Gson().fromJson(JSON, TimeBlock.class);
                if(categories != null && categories.contains(APP_EDIT)) {
                    category = APP_EDIT;
                    existingTimeBlock = timeblock;
                    populateFields();
                }
            }
        }

    }

    public void initializeLayout() {
        Log.d("***DEBUG***", "inside setTodaysDate");

        cl = findViewById(R.id.cl_blockPage);
        et_name = cl.findViewById(R.id.et_BlockName);
        et_description = cl.findViewById(R.id.et_BlockDescription);
        tv_BlockStartTime = cl.findViewById(R.id.tv_BlockStartTime);
        tv_BlockEndTime = cl.findViewById(R.id.tv_BlockEndTime);
        btnDeleteTimeBlock = cl.findViewById(R.id.btn_deleteTimeBlock);
        btnDeleteTimeBlock.setVisibility(View.INVISIBLE);
    }

    public void setupTimePickers() {
        Log.d("***DEBUG***", "inside setupTimePickers");

        tv_BlockStartTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour, minute;
                if(existingTimeBlock == null) {
                    hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    minute = mcurrentTime.get(Calendar.MINUTE);
                }
                else {
                    hour = Integer.parseInt(existingTimeBlock.startTime.substring(0, 2));
                    minute = Integer.parseInt(existingTimeBlock.startTime.substring(3, 5));

                    if(existingTimeBlock.startTime.toString().substring(6, 8).equals("pm")) {
                        hour += 12;
                    }
                }

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(TimeBlockPage.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String time;

                        if(selectedHour > 12) {
                            time = String.format("%02d:%02d", (selectedHour - 12), selectedMinute) + " pm";
                        }
                        else {
                            time = String.format("%02d:%02d", (selectedHour), selectedMinute) + " am";
                        }
                        tv_BlockStartTime.setText(time);
                    }
                }, hour, minute, false); //No 24 hour time
                mTimePicker.setTitle("Select Start Time");
                mTimePicker.show();

            }
        });

        tv_BlockEndTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour, minute;
                if(existingTimeBlock == null) {
                    hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    minute = mcurrentTime.get(Calendar.MINUTE);
                }
                else {
                    hour = Integer.parseInt(existingTimeBlock.endTime.substring(0, 2));
                    minute = Integer.parseInt(existingTimeBlock.endTime.substring(3, 5));

                    if(existingTimeBlock.startTime.substring(6, 8).equals("pm")) {
                        hour += 12;
                    }
                }

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(TimeBlockPage.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String time;

                        if(selectedHour > 12) {
                            time = String.format("%02d:%02d", (selectedHour - 12), selectedMinute) + " pm";
                        }
                        else {
                            if(selectedHour == 0) {
                                time = String.format("%02d:%02d", 12, selectedMinute) + " am";
                            }
                            else {
                                time = String.format("%02d:%02d", (selectedHour), selectedMinute) + " am";
                            }

                        }
                        tv_BlockEndTime.setText(time);
                    }
                }, hour, minute, false);//No 24 hour time
                mTimePicker.setTitle("Select Start Time");
                mTimePicker.show();

            }
        });
    }

    private void populateFields() {
        Log.d("***DEBUG***", "inside populateFields");

        btnDeleteTimeBlock.setVisibility(View.VISIBLE);

        et_name.setText(existingTimeBlock.name);
        et_description.setText(existingTimeBlock.description);
        //start and end times
        tv_BlockStartTime.setText(existingTimeBlock.startTime);
        tv_BlockEndTime.setText(existingTimeBlock.endTime);
        btnDeleteTimeBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickDeleteTimeBlock(view, existingTimeBlock);
            }
        });
    }

    public void onClickSaveTimeBlock(View v) {
        Log.d("***DEBUG***", "inside onClickSaveBlockTime");

        Date startDate = new Date();
        Date endDate = new Date();

        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        try {
            startDate = formatter.parse(tv_BlockStartTime.getText().toString());
            endDate = formatter.parse(tv_BlockEndTime.getText().toString());
        }
        catch (Exception e) {

        }

        TimeBlock timeblock = new TimeBlock();
        timeblock.userId = "test123";

        if(TextUtils.isEmpty(et_name.getText())) {
            et_name.setError( "Name is required!" );
        }
        if(tv_BlockStartTime.getText().toString().equals("Start Time")) {
            tv_BlockStartTime.setError( "Start time is required!" );
        }
        if(tv_BlockEndTime.getText().toString().equals("End Time")) {
            tv_BlockEndTime.setError( "End time is required!" );
        }

        if(et_name.getError() == null && tv_BlockStartTime.getError() == null && tv_BlockEndTime.getError() == null) {
            timeblock.name = et_name.getText().toString();
            timeblock.description = et_description.getText().toString();
            timeblock.startTime = tv_BlockStartTime.getText().toString();
            timeblock.endTime = tv_BlockEndTime.getText().toString();

            Log.d("***DEBUG***", "Before conversion: " + timeblock.startTime);
            timeblock.startTimestamp = TimeConverter.convertToGMTFromDisplay(timeblock.startTime);
            Log.d("***DEBUG***", "After conversion: " + timeblock.startTimestamp);
            timeblock.endTimestamp = TimeConverter.convertToGMTFromDisplay(timeblock.endTime);

            if(existingTimeBlock != null) {
                timeblock.id = existingTimeBlock.id;
                dbh.updateTimeBlock(timeblock);
            }
            else {
                dbh.createTimeBlock(timeblock);
            }

            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
        }

    }

    public void onClickDeleteTimeBlock(View v, TimeBlock tb) {
        dbh.deleteTimeBlock(v, tb);
        finishActivity(Integer.parseInt(APP_CREATE));
        finish();
    }

}
