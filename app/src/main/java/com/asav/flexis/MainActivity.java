package com.asav.flexis;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;

public class MainActivity extends AppCompatActivity implements FragmentAddObjectiveOptions.FragmentAddObjectiveOptionsListener {

    private DrawerLayout mDrawerLayout;

    static final String APP_CREATE = "0";
    static final String APP_READ = "1";
    static final String APP_EDIT = "2";
    static final String APP_DELETE = "3";


    DatabaseHandler dbh = new DatabaseHandler();
    NotificationHandler notificationHandler;

    TimeBlock currentTimeBlock = null;

    Map<String, List<Objective>> timeblockObjectivesMap = new HashMap<>();
    Map<String, Objective> objectivesMap = new HashMap<>();
    Map<String, TimeBlock> timeblocksMap = new HashMap<>();
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

        setTodaysDate();
        getTimeBlocksAndObjectives();
        //initializeNavigationView();

        //reset isComplete flag if necessary

    }

    public void setTodaysDate() {
        Log.d("***DEBUG***", "inside setTodaysDate");

        tv_main_todaydate = findViewById(R.id.tv_main_todaydate);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
        String todaysDate = format.format(cal.getTime());
        tv_main_todaydate.setText(todaysDate);
    }

    public void onClickNewTimeBlock(View v) {
        Log.d("***DEBUG***", "inside onClickNewTimeBlock");
        Intent openNewTimeBlock = new Intent(this, TimeBlockPage.class);
        //startActivity(openNewTimeBlock);
        startActivityForResult(openNewTimeBlock, Integer.parseInt(APP_CREATE));
    }

    public void onClickEditTimeBlock(View v) {
        Log.d("***Debug***", "inside onClickEditTimeBlock");

        try {
            for (TimeBlock obj : timeblocksMap.values()) {
                if (obj.id == v.getTag()) {

                    Gson gson = new Gson();
                    String timeblockJSON = gson.toJson(obj);
                    Bundle extras = new Bundle();
                    Intent editTimeBlock = new Intent(this, TimeBlockPage.class);
                    editTimeBlock.addCategory(APP_EDIT);
                    //extras.putParcelable("user", user);
                    extras.putString("details", timeblockJSON);
                    editTimeBlock.putExtras(extras);
                    editTimeBlock.addCategory(APP_EDIT);
                    startActivity(editTimeBlock);
                }
            }
        } catch(Exception e) {
            Log.d("***ERROR***", "objectiveActionClicked: " + e.getMessage());
            Log.d("***ERROR***", "objectiveActionClicked: " + e);
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickNewObjective(View v) {
        Log.d("***DEBUG***", "inside onClickNewObjective");
        Intent openNewObjective = new Intent(this, ObjectivePage.class);
        startActivity(openNewObjective);
    }

    public void onClickNewObjectiveForTimeblock(String tbId) {
        Log.d("***DEBUG***", "inside onClickNewObjectiveForTimeblock");

        Intent openNewObjective = new Intent(this, ObjectivePage.class);
        openNewObjective.putExtra("TimeblockId", tbId);
        startActivity(openNewObjective);
    }

    public void onClickAddObjectiveForTimeblock(String tbId) {
        Log.d("***DEBUG***", "inside onClickNewObjectiveForTimeblock");

        Intent openNewObjective = new Intent(this, ObjectiveList.class);
        openNewObjective.putExtra("TimeblockId", tbId);
        startActivity(openNewObjective);
    }

    public void getTimeBlocksAndObjectives() {
        Log.d("***Debug***", "inside getTimeBlocksAndObjectives");

        //populates timeblocksmap
        dbh.getTimeBlocksForMainActivity(this);

        //populates , when finished, drawTimeBlockObjs will be called
        dbh.getObjectivesForMainActivity(this);

    }

    public void drawTimeBlockObjectives(Map<String, List<Objective>> timeblockObjsMap) {
        Log.d("***Debug***", "inside drawTimeBlockObjectives");

        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout ll_group_wrapper = findViewById(R.id.ll_group_wrapper);
        LinearLayout newGroup = new LinearLayout(this);
        newGroup.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        newGroup.setOrientation(LinearLayout.VERTICAL);
        ll_group_wrapper.addView(newGroup);

        try {
            // for each timeblock
            for(String tbId : timeblockObjsMap.keySet()) {
                Log.d("***Debug***", "timeblock: " + tbId);
                //if (timeblockObjsMap.get(tbId).size() > 0) { //get objectives for time block
                    View v = inflater.inflate(R.layout.timeblock_header, null);
                    ConstraintLayout clTimeblockHeader = v.findViewById(R.id.cl_timeblock_header);
                    TextView tv_timeblock_name = clTimeblockHeader.findViewById(R.id.tv_timeblock_name);

                    TextView tv_timeblock_timerange = clTimeblockHeader.findViewById(R.id.tv_timeblock_timerange);
                    ImageView iv_addTask = clTimeblockHeader.findViewById(R.id.iv_add_objective);
                    iv_addTask.setOnClickListener(onClickAddObjective);
                    TimeBlock currentTB = null;
                    if(timeblocksMap.containsKey(tbId)) {
                        currentTB = timeblocksMap.get(tbId);
                        tv_timeblock_name.setTag(currentTB.id);
                        tv_timeblock_name.setText(currentTB.name);
                        tv_timeblock_name.setOnClickListener(onClickEditTimeBlock);

                        String timeRange = currentTB.startTime + " - " + currentTB.endTime;
                        tv_timeblock_timerange.setText(timeRange);

                        newGroup.addView(clTimeblockHeader);
                    }

                    // for each objective in a timeblock
                    if(currentTB != null && timeblockObjsMap.containsKey(currentTB.id)) {
                        for (Objective obj : timeblockObjsMap.get(currentTB.id)) {

                            if (!obj.isComplete) {
                                //setup layout stuff

                                final View objectiveCard = inflater.inflate(R.layout.objective_card_component, null, false);
                                objectiveCard.setPadding(0, 0, 0, 10);
                                objectiveCard.setTag(obj);

                                CardView cvActionObjective = objectiveCard.findViewById(R.id.cv_card_action);
                                cvActionObjective.setOnClickListener(onClickActionObjective);

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
                                    TaskTimer timer = new TaskTimer(this, objectiveCard, Integer.parseInt(obj.duration));

                                    timerMap.put(obj, timer);
                                }

                                objectiveDuration.setText(objDuration);
                                objectiveEffort.setText(obj.effort);
                                objectiveCard.setOnClickListener(onClickEditObjective);

                                //objectiveFrequency.setText(obj.frequency);

                                //Add margins to card for spacing, height, width
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT
                                );
                                params.setMargins(0, 5, 0, 5);
                                objectiveCard.setLayoutParams(params);
                                newGroup.addView(objectiveCard);

                                objectiveCard.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                                objectivesViewMap.put(objectiveCard, obj);
                            }
                        }
                    }
                //}
            }
        } catch(Exception e) {
            Log.d("***ERROR***", "drawTimeBlockObjectives: " + e.getMessage());
            Log.d("***ERROR***", "drawTimeBlockObjectives: " + e);
            printStackTrace(e.getStackTrace());
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }
    }



    private View.OnClickListener onClickActionObjective = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            objectiveActionClicked(v);
        }
    };

    private View.OnClickListener onClickEditObjective = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            editObjective(v);
        }
    };

    private void editObjective(View v) {
        Log.d("***Debug***", "inside editObjective");

        try {
            //ViewGroup objectiveCard = (ViewGroup) v.getParent();

            for (Objective obj : objectivesMap.values()) {
                if (obj == v.getTag()) {

                    Gson gson = new Gson();
                    String objectiveJSON = gson.toJson(obj);
                    Bundle extras = new Bundle();
                    Intent editObjective = new Intent(this, ObjectivePage.class);
                    editObjective.addCategory(APP_EDIT);
                    //extras.putParcelable("user", user);
                    extras.putString("details", objectiveJSON);
                    editObjective.putExtras(extras);
                    startActivity(editObjective);
                }
            }
        } catch(Exception e) {
            Log.d("***ERROR***", "objectiveActionClicked: " + e.getMessage());
            Log.d("***ERROR***", "objectiveActionClicked: " + e);
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }
    }

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

        objective.dateCompleted = Calendar.getInstance().getTime();

        //update database to show complete?
        dbh.updateObjective(objective);

        //prompt user to select next task to begin?
        //redraw list of objectives
        drawTimeBlockObjectives(timeblockObjectivesMap);
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

    public void setTimeBlockObjectivesMap(Map<String, TimeBlock> tbm, Map<String, Objective> objm) {
        Log.d("***Debug***", "inside setTimeBlockObjectivesMap");

        for(TimeBlock tb : tbm.values()) {
            if (!timeblockObjectivesMap.containsKey(tb.id)) {
                timeblockObjectivesMap.put(tb.id, new ArrayList<Objective>());
            }

            for(Objective obj : objm.values()) {
                if (obj.timeblockId != null && obj.timeblock.id.equals(tb.id)) {
                    timeblockObjectivesMap.get(obj.timeblockId).add(obj);
                }
            }
        }

        drawTimeBlockObjectives(timeblockObjectivesMap);
    }


    public void setCurrentTimeBlock(TimeBlock tb) {
        Log.d("***DEBUG***", "inside MA.CurrentTimeBlock");
        this.currentTimeBlock = tb;

        dbh.getObjectivesForTimeBlock(this, currentTimeBlock);
    }

    public void setTimeBlocksMap(Map<String, TimeBlock> timeblocksMap) {
        Log.d("***DEBUG***", "inside MA.setTimeBlocksMap");
        this.timeblocksMap = timeblocksMap;

    }

    public void setObjectivesMap(Map<String, Objective> objectivesMap) {
        Log.d("***Debug***", "inside setObjectivesMap");

        this.objectivesMap = objectivesMap;

        //db call finished, create the final map
        setTimeBlockObjectivesMap(timeblocksMap, objectivesMap);

    }

    private View.OnClickListener onClickAddObjective = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            View parent = (ViewGroup) v.getParent();
            String tbId = (String) parent.findViewById(R.id.tv_timeblock_name).getTag();

            showObjectiveOptionsDialog(tbId);
        }
    };

    private View.OnClickListener onClickEditTimeBlock = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickEditTimeBlock(v);
        }
    };

    private void printStackTrace(StackTraceElement[] ste) {
        for(StackTraceElement s : ste) {
            Log.d("***ERROR***", "drawTimeBlockObjectives" + s.toString());
        }
    }

    private void showObjectiveOptionsDialog(String tbId) {
        FragmentManager fm = getSupportFragmentManager();

        FragmentAddObjectiveOptions objOptionsFragment = FragmentAddObjectiveOptions.newInstance("Choose from...", tbId);

        objOptionsFragment.show(fm, "fragment_add_objective_options");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String reqCode = requestCode + "";
        // Check which request we're responding to
        if (reqCode.equals(APP_CREATE) || reqCode.equals(APP_EDIT)) {

            //run queries again and refresh the display to show new / edited / deleted data


            // Make sure the request was successful
            /*if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }*/
        }
    }

    @Override
    public void onFinishEditDialog(String inputText, String tbId) {
        Toast.makeText(this, "Hi, " + inputText, Toast.LENGTH_SHORT).show();
        if(inputText.equals("New")) {
            onClickNewObjectiveForTimeblock(tbId);
        }
        else {
            onClickAddObjectiveForTimeblock(tbId);
        }
    }
}
