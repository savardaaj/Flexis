package com.asav.flexis;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Calendar;

public class TimeBlockPage extends AppCompatActivity {

    DatabaseHandler dbh = new DatabaseHandler();

    ConstraintLayout cl;
    EditText et_name, et_description;
    TextView tv_BlockStartTime, tv_BlockEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("***DEBUG***", "inside onCreate TimeBlockPage");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blockpage);

        initializeLayout();
        setupTimePickers();


    }

    public void initializeLayout() {
        Log.d("***DEBUG***", "inside initializeLayout");

        cl = findViewById(R.id.cl_blockPage);
        et_name = cl.findViewById(R.id.et_BlockName);
        et_description = cl.findViewById(R.id.et_BlockDescription);
        tv_BlockStartTime = cl.findViewById(R.id.tv_BlockStartTime);
        tv_BlockEndTime = cl.findViewById(R.id.tv_BlockEndTime);
    }

    public void setupTimePickers() {
        Log.d("***DEBUG***", "inside initializeTimePickers");
        tv_BlockStartTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
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
                }, hour, minute, false);//No 24 hour time
                mTimePicker.setTitle("Select Start Time");
                mTimePicker.show();

            }
        });

        tv_BlockEndTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
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

}
