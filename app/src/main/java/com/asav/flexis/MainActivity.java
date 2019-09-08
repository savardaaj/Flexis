package com.asav.flexis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.time.LocalDateTime.now;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class MainActivity extends AppCompatActivity implements FragmentAddObjectiveOptions.FragmentAddObjectiveOptionsListener {

    private DrawerLayout mDrawerLayout;

    static final String APP_CREATE = "0";
    static final String APP_READ = "1";
    static final String APP_EDIT = "2";
    static final String APP_DELETE = "3";

    public static String LOG_DEBUG = "***DEBUG***";


    DatabaseHandler dbh = new DatabaseHandler();
    NotificationHandler notificationHandler;

    TimeBlock currentTimeBlock = null;

    Map<String, List<Objective>> timeblockObjectivesMap = new HashMap<>();
    Map<String, Objective> objectivesMap = new HashMap<>();
    Map<String, TimeBlock> timeblocksMap = new HashMap<>();
    Map<View, Objective> objectivesViewMap = new HashMap<>();
    Map<Objective, TaskTimer> timerMap = new HashMap<>();

    List<LinearLayout> llGroupList = new ArrayList<>();

    TextView toolbar_title;
    TextView tv_card_begin;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_DEBUG, "inside onCreate MainActivity");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        //getMenuInflater().inflate(R.menu.options, menu);

        initDrawerItems();

        notificationHandler = new NotificationHandler(this, MainActivity.class);

        setTodaysDate();
        getTimeBlocksAndObjectives();

        //reset isComplete flag if necessary

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String objJSON = extras.getString("objectiveJSON");
            Log.d(LOG_DEBUG, "objJSON: " + objJSON);
            Objective obj = new Gson().fromJson(objJSON, Objective.class);

            if(obj != null) {
                View v = getKeyByValue(objectivesViewMap, obj);
                finishObjective(obj, v);
            }
        }
    }

    @Override
    public void onResume() {
        Log.d(LOG_DEBUG, "inside onResume");
        super.onResume();
        //registerReceiver(br, new IntentFilter(BroadcastService.TASKTIMER_BR));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.d(LOG_DEBUG, "get extras on Resume");
            String objJSON = extras.getString("objectiveJSON");
            int uniqueId = extras.getInt("notifUniqueId");
            Log.d(LOG_DEBUG, "objJSON: " + objJSON);
            Objective obj = new Gson().fromJson(objJSON, Objective.class);

            if(obj != null) {
                notificationHandler.cancelNotification(uniqueId);
                View v = getKeyByValue(objectivesViewMap, obj);
                finishObjective(obj, v);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregisterReceiver(br);
        Log.d(LOG_DEBUG, "Unregistered broadcast receiver");
    }

    @Override
    public void onStop() {
        try {
            //unregisterReceiver(br);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        super.onStop();
    }
    @Override
    public void onDestroy() {
        stopService(new Intent(this, BroadcastService.class));
        Log.i(LOG_DEBUG, "Stopped service");
        super.onDestroy();
    }

    private void initDrawerItems() {

        dl = findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this, dl, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        dl.addDrawerListener(t);
        t.syncState();

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nv = findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {

                    case R.id.new_timeblock:
                        Toast.makeText(MainActivity.this, "New Timeblock",Toast.LENGTH_SHORT).show();
                        onClickNewTimeBlock(null);
                        break;

                    case R.id.objective_list:
                        Toast.makeText(MainActivity.this, "Objective List",Toast.LENGTH_SHORT).show();

                        break;


                    case R.id.settings:
                        Toast.makeText(MainActivity.this, "Settings",Toast.LENGTH_SHORT).show();

                        break;


                    case R.id.timeblock_list:
                        Toast.makeText(MainActivity.this, "Timeblock List",Toast.LENGTH_SHORT).show();

                        break;



                    default:
                        return true;
                }

                return true;

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.options_notify) {
            Toast.makeText(MainActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
            notificationHandler.createNotification(null);
            return true;
        }

        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }


    public void setTodaysDate() {
        Log.d("***DEBUG***", "inside setTodaysDate");

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
        String todaysDate = format.format(cal.getTime());
        getSupportActionBar().setTitle(todaysDate);
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
                    startActivityForResult(editTimeBlock, Integer.parseInt(APP_EDIT));
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
        startActivityForResult(openNewObjective, Integer.parseInt(APP_CREATE));
    }

    public void onClickAddObjectiveForTimeblock(String tbId) {
        Log.d("***DEBUG***", "inside onClickNewObjectiveForTimeblock");

        Intent openNewObjective = new Intent(this, ObjectiveList.class);
        openNewObjective.putExtra("TimeblockId", tbId);
        startActivityForResult(openNewObjective, Integer.parseInt(APP_EDIT));
    }

    public void getTimeBlocksAndObjectives() {
        Log.d("***Debug***", "inside getTimeBlocksAndObjectives");

        //populates timeblocksmap
        dbh.getTimeBlocksForMainActivity(this);



    }

    public void drawTimeBlockObjectives(Map<String, List<Objective>> timeblockObjsMap) {
        Log.d("***Debug***", "inside drawTimeBlockObjectives");

        if(!llGroupList.isEmpty()) {
            for(LinearLayout ll : llGroupList) {
                ll.removeAllViews();
            }
        }

        View loadingPanel = findViewById(R.id.loadingPanel);
        if (loadingPanel != null) {
            loadingPanel.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout ll_group_wrapper = findViewById(R.id.ll_group_wrapper);
        ll_group_wrapper.removeAllViews();
        LinearLayout newGroup = new LinearLayout(this);
        newGroup.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        newGroup.setOrientation(LinearLayout.VERTICAL);
        ll_group_wrapper.addView(newGroup);

        try {
            // for each timeblock
            for(String tbId : timeblockObjsMap.keySet()) {
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

                        //convert military time to regular time based on custom setting

                        String timeRange = currentTB.startTime + " - " + currentTB.endTime;
                        tv_timeblock_timerange.setText(timeRange);

                        newGroup.addView(clTimeblockHeader);
                    }

                    // for each objective in a timeblock
                    if(currentTB != null && timeblockObjsMap.containsKey(currentTB.id)) {
                        for (Objective obj : timeblockObjsMap.get(currentTB.id)) {
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

                            if (obj.isComplete) {
                                objectiveCard.findViewById(R.id.iv_completed).setVisibility(View.VISIBLE);
                                objectiveCard.findViewById(R.id.cv_card_action).setVisibility(View.GONE);
                            }

                            llGroupList.add(newGroup);
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


            if(timer != null && timer.isRunning) {
                Log.d("***Debug***", "Timer: " + timer.elapsedTime + " : " + timer.desiredTimeInMilliSeconds );
                if(timer.elapsedTime > timer.desiredTimeInMilliSeconds) {
                    finishObjective(objective, objectiveCard);
                }
                else if(timer.elapsedTime > 0) {
                    timer.pauseTimer();
                }
            } else {
                if(timer.elapsedTime == 0) {
                    //startService(new Intent(this, BroadcastService.class));
                    timer.startTimer();

                } else {
                    timer.resumeTimer();
                }
            }
        }
        catch(Exception e) {
            Log.d("***ERROR***", "objectiveActionClicked: " + e.getMessage());
            Log.d("***ERROR***", "objectiveActionClicked: " + e);
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent); // or whatever method used to update your GUI fields
        }
    };



    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            long millisUntilFinished = intent.getLongExtra("countdown", 0);
            Log.i(LOG_DEBUG, "Countdown seconds remaining: " +  millisUntilFinished / 1000);
        }
    }

    private void finishObjective(Objective objective, View objCard) {
        Log.d("***Debug***", "inside finishObjective");

        //get the view card and set as completed visually
        markFinished(objCard);

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

        timeblockObjectivesMap = new HashMap<>();

        for(TimeBlock tb : tbm.values()) {

            Date endTime = TimeConverter.convertDisplayToDate(tb.endTime);
            Date currentTime = TimeConverter.getCurrentTime();

            Log.d("***Debug***", "endTime: " + endTime);
            Log.d("***Debug***", "currentTime: " + currentTime);

            //anything where the endtime has passed should be excluded
            if (!timeblockObjectivesMap.containsKey(tb.id) && endTime.after(currentTime)) {
                timeblockObjectivesMap.put(tb.id, new ArrayList<Objective>());
            }

            for(Objective obj : objm.values()) {
                if (obj.timeblockId != null && obj.timeblock != null && obj.timeblock.id.equals(tb.id) && timeblockObjectivesMap.containsKey(tb.id)) {
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

        //populates , when finished, drawTimeBlockObjs will be called
        dbh.getObjectivesForMainActivity(this);
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

    public static void printStackTrace(StackTraceElement[] ste) {
        for(StackTraceElement s : ste) {
            Log.d("***ERROR***", "drawTimeBlockObjectives" + s.toString());
        }
    }

    private void showObjectiveOptionsDialog(final String tbId) {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.fragment_add_objective_options, null);
        TextView tvNew = dialoglayout.findViewById(R.id.tv_from_new);
        TextView tvExisting = dialoglayout.findViewById(R.id.tv_from_existing);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);
        final AlertDialog ad = builder.show();

        tvNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickNewObjectiveForTimeblock(tbId);
                ad.dismiss();
            }
        });
        tvExisting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAddObjectiveForTimeblock(tbId);
                ad.dismiss();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String reqCode = requestCode + "";
        // Check which request we're responding to
        if (reqCode.equals(APP_CREATE) || reqCode.equals(APP_EDIT)) {

            //run queries again and refresh the display to show new / edited / deleted data
            getTimeBlocksAndObjectives();

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

    private void markFinished(View objCard) {

        if (objCard != null) {
            //objCard.setBackgroundColor(Color.GREEN);

            //objCard.refreshDrawableState();

        }

    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}
