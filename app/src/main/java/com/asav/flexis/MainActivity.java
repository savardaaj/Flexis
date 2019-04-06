package com.asav.flexis;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.gson.internal.ObjectConstructor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;


    DatabaseHandler dbh = new DatabaseHandler();
    NotificationHandler notificationHandler;

    Map<String, Objective> objectivesMap = new HashMap<>();
    Map<View, Objective> objectivesViewMap = new HashMap<>();
    Map<Objective, TaskTimer> timerMap = new HashMap<>();

    TextView tv_main_todaydate;
    TextView tv_card_begin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("***DEBUG***", "inside onCreate MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationHandler = new NotificationHandler(this, MainActivity.class);

        //mDrawerLayout = findViewById(R.id.drawer_layout);

        initializeLayout();
        //initializeNavigationView();

        dbh.getObjectives(this);
    }

    public void initializeLayout() {
        Log.d("***DEBUG***", "inside initializeLayout");

        tv_main_todaydate = findViewById(R.id.tv_main_todaydate);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
        String todaysDate = format.format(cal.getTime());
        tv_main_todaydate.setText(todaysDate);
    }

    public void onClickNewTimeBlock(View v) {
        Log.d("***DEBUG***", "inside onClickNewTimeBlock");
        Intent openNewTimeBLock = new Intent(this, TimeBlockPage.class);
        startActivity(openNewTimeBLock);
    }

    public void onClickNewObjective(View v) {
        Log.d("***DEBUG***", "inside onClickNewObjective");
        Intent openNewObjective = new Intent(this, ObjectivePage.class);
        startActivity(openNewObjective);
    }

    public void populateTodaysTasks(Map<String, Objective> objectivesMap) {
        Log.d("***Debug***", "inside populateTodaysTasks");

        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            LinearLayout ll_upcoming = findViewById(R.id.ll_main_today);
            ll_upcoming.removeAllViews();

            for(Objective obj : objectivesMap.values()) {
                if (!obj.isComplete) {
                    //setup layout stuff
                    final View objectiveCard = inflater.inflate(R.layout.objective_card_component, null, false);
                    objectiveCard.setPadding(0, 0, 0, 10);
                    objectiveCard.setTag(obj);
                    Log.d("***Debug***", "stored tag: " + objectiveCard.getTag());


                    CardView cvActionObjective = objectiveCard.findViewById(R.id.cv_card_action);
                    cvActionObjective.setOnClickListener(onClickActionObjective);

                    //initialize the layout fields
                    TextView objectiveName = objectiveCard.findViewById(R.id.tv_objCard_Name);
                    //TextView objectiveDescription =  objectiveCard.findViewById(R.id.);
                    TextView objectiveDuration = objectiveCard.findViewById(R.id.tv_objCard_Duration);
                    TextView objectiveEffort = objectiveCard.findViewById(R.id.tv_objCard_Effort);
                    //TextView objectiveFrequency =  objectiveCard.findViewById(R.id.tv_objCard_Frequency);
                    //TextView objectiveTimeBlock =  objectiveCard.findViewById(R.id.tv_objCard_TimeBlock);

                    //set values of imported components
                    objectiveName.setText(obj.name);
                    //objectiveDescription.setText(obj.description);
                    String objDuration = parseDuration(Integer.parseInt(obj.duration));
                    objectiveDuration.setText(objDuration);
                    objectiveEffort.setText(obj.effort);
                    //objectiveFrequency.setText(obj.frequency);
                    //if(obj.timeblock != null) {
                    // objectiveTimeBlock.setText(obj.timeblock.name);
                    //}

                    //Add margins to card for spacing, height, width
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    params.setMargins(0, 5, 0, 5);
                    objectiveCard.setLayoutParams(params);

                    ll_upcoming.addView(objectiveCard);
                    objectiveCard.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                    //add card to view

                    TaskTimer timer = new TaskTimer(this, objectiveCard, Integer.parseInt(obj.duration));

                    timerMap.put(obj, timer);
                    objectivesViewMap.put(objectiveCard, obj);
                }
            }
        } catch(Exception e) {
            Log.d("***ERROR***", "populateTodaysTasks: " + e.getMessage());
            Log.d("***ERROR***", "populateTodaysTasks: " + e);
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener onClickActionObjective = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            objectiveActionClicked(v);
        }
    };

    public void objectiveActionClicked(View v) {
        Log.d("***Debug***", "inside objectiveActionClicked");

        Objective objective = null;

        try {
            ViewGroup objectiveCard = (ViewGroup) v.getParent();
            TaskTimer timer = null;

            for(Objective obj : objectivesMap.values()) {
                if(obj == objectiveCard.getTag()) {
                    timer = timerMap.get(obj);
                    objective = obj;
                }
            }

            if(timer != null) {
                if (timer.isRunning) {
                    Log.d("***Debug***", "Timer: " + timer.elapsedTime + " : " + timer.desiredTimeInMilliSeconds );
                    if(timer.elapsedTime > timer.desiredTimeInMilliSeconds) {
                        finishObjective(objective);
                    }
                    else if(timer.elapsedTime > 0) {
                        timer.pauseTimer();
                    }
                } else {
                    if(timer.elapsedTime == 0) {
                        timer.startTimer();

                    } else {
                        timer.resumeTimer();
                    }
                }
            }
        }
        catch(Exception e) {
            Log.d("***ERROR***", "objectiveActionClicked: " + e.getMessage());
            Log.d("***ERROR***", "objectiveActionClicked: " + e);
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishObjective(Objective objective) {
        Log.d("***Debug***", "inside finishObjective");

        //mark objective to be excluded from list
        objective.isComplete = true;

        //update database to show complete?

        //prompt user to select next task to begin
        //redraw list of objectives
        populateTodaysTasks(objectivesMap);
    }

    public void setObjectives(Map<String, Objective> objectivesMap) {
        Log.d("***Debug***", "inside setObjectives");

        this.objectivesMap = objectivesMap;
        populateTodaysTasks(this.objectivesMap);
    }

    private String parseDuration(int duration) {
        Log.d("***Debug***", "inside parseDuration");

        int minutes = duration % 60;
        int hours = duration / 60;
        int seconds = 0;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void onClickCreateNotification(View v) {
        Log.d("***Debug***", "inside onClickCreateNotification");

        notificationHandler.createNotification(v);
    }

    private void initializeNavigationView() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
    }


}
