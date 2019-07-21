package com.asav.flexis;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Set;

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
    }

    public void setupTimePickers() {
        Log.d("***DEBUG***", "inside setupTimePickers");

        tv_BlockStartTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour, minute;
                if(existingTimeBlock == null) {
                    hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    minute = mcurrentTime.get(Calendar.MINUTE);
                }
                else {
                    hour = Integer.parseInt(existingTimeBlock.startTime.substring(0, 2));
                    minute = Integer.parseInt(existingTimeBlock.startTime.substring(3, 5));

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
                // TODO Auto-generated method stub
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

        TimeBlock timeblock = new TimeBlock();
        timeblock.userId = "test123";
        timeblock.name = et_name.getText().toString();
        timeblock.description = et_description.getText().toString();
        timeblock.startTime = tv_BlockStartTime.getText().toString();
        timeblock.endTime = tv_BlockEndTime.getText().toString();

        dbh.createTimeBlock(timeblock);

        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
    }

    public void onClickDeleteTimeBlock(View v, TimeBlock tb) {
        dbh.deleteTimeBlock(v, tb);
        finishActivity(Integer.parseInt(APP_CREATE));
    }

}
