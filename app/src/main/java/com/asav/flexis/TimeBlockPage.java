package com.asav.flexis;

import android.app.TimePickerDialog;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimeBlockPage extends AppCompatActivity {

    DatabaseHandler dbh = new DatabaseHandler();

    ConstraintLayout cl;
    EditText et_name, et_description, et_startTime, et_endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("***DEBUG***", "inside onCreate TimeBlockPage");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blockpage);

        initializeLayout();
        setupTimePickers();


    }



    public void initializeLayout() {
        cl = findViewById(R.id.cl_blockPage);
        et_name = cl.findViewById(R.id.et_BlockName);
        et_description = cl.findViewById(R.id.et_BlockDescription);
        et_startTime = cl.findViewById(R.id.et_BlockStartTime);
        et_endTime = cl.findViewById(R.id.et_BlockEndTime);
    }

    public void setupTimePickers() {
        Log.d("***DEBUG***", "inside initializeTimePickers");
        et_startTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getApplicationContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        et_startTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, false);//No 24 hour time
                mTimePicker.setTitle("Select Start Time");
                mTimePicker.show();

            }
        });

        et_endTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getApplicationContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        et_endTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, false);//No 24 hour time
                mTimePicker.setTitle("Select Start Time");
                mTimePicker.show();

            }
        });
    }

    public void onClickSaveTimeBlock(View v) {
        Log.d("***DEBUG***", "inside onClickSaveBlockTime");

        TimeBlock timeblock = new TimeBlock();
        timeblock.userId = "test123";
        timeblock.name = et_name.getText().toString();
        timeblock.description = et_description.getText().toString();
        timeblock.startTime = et_startTime.getText().toString();
        timeblock.endTime = et_endTime.getText().toString();

        dbh.createTimeBlock(timeblock);
    }

}
