package com.asav.flexis

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.navigation.ui.AppBarConfiguration

import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.Objects

import java.time.LocalDateTime.now
import java.util.Map.Entry.comparingByValue
import java.util.stream.Collectors.toMap

class MainActivity : AppCompatActivity(), FragmentAddObjectiveOptions.FragmentAddObjectiveOptionsListener {

    private val mDrawerLayout: DrawerLayout? = null


    internal var dbh = DatabaseHandler()
    internal lateinit var notificationHandler: NotificationHandler
    internal var notifManager: NotificationManagerCompat? = null

    internal var currentTimeBlock: TimeBlock? = null

    internal var timeblockObjectivesMap = mutableMapOf<String, MutableList<Objective>>()
    internal var objectivesMap = mutableMapOf<String, Objective>()
    internal var timeblocksMap = mutableMapOf<String, TimeBlock>()
    internal var objectivesViewMap = mutableMapOf<View, Objective>()
    internal var timerMap = mutableMapOf<Objective, TaskTimer>()

    internal var llGroupList = mutableListOf<LinearLayout>()

    internal var toolbar_title: TextView? = null
    internal var tv_card_begin: TextView? = null

    private var dl: DrawerLayout? = null
    private var t: ActionBarDrawerToggle? = null
    private var nv: NavigationView? = null


    private val onClickActionObjective = View.OnClickListener { v -> objectiveActionClicked(v) }

    private val onClickEditObjective = View.OnClickListener { v -> editObjective(v) }

    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateGUI(intent) // or whatever method used to update your GUI fields
        }
    }

    private val onClickAddObjective = View.OnClickListener { v ->
        val parent = v.parent as ViewGroup
        val tbId = parent.findViewById<View>(R.id.tv_timeblock_name).tag as String

        showObjectiveOptionsDialog(tbId)
    }

    private val onClickEditTimeBlock = View.OnClickListener { v -> onClickEditTimeBlock(v) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(LOG_DEBUG, "inside onCreate MainActivity")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.loadingPanel).visibility = View.VISIBLE

        //getMenuInflater().inflate(R.menu.options, menu);

        initDrawerItems()

        notificationHandler = NotificationHandler(this, MainActivity::class.java)
        notifManager = notificationHandler.notificationManager

        setTodaysDate()

        if (savedInstanceState != null) {
            Log.d(LOG_DEBUG, "saved isntance state not null")
            // Then the application is being reloaded
            timeblockObjectivesMap = Gson().fromJson<MutableMap<String, MutableList<Objective>>>(
                    savedInstanceState.getString("timeblockObjectivesMapJSON"), object : TypeToken<HashMap<String, Any>>() {

            }.type
            )

        } else {
            getTimeBlocksAndObjectives()
        }

        //reset isComplete flag if necessary

        val extras = intent.extras
        if (extras != null) {
            val objJSON = extras.getString("objectiveJSON")
            Log.d(LOG_DEBUG, "objJSON: " + objJSON!!)
            val obj = Gson().fromJson(objJSON, Objective::class.java)

            if (obj != null) {
                val v = getKeyByValue(objectivesViewMap, obj)
                finishObjective(obj, v)
            }
        }
    }

    public override fun onResume() {
        Log.d(LOG_DEBUG, "inside onResume")
        super.onResume()
        //registerReceiver(br, new IntentFilter(BroadcastService.TASKTIMER_BR));

        val extras = intent.extras
        if (extras != null) {
            Log.d(LOG_DEBUG, "get extras on Resume")
            val objJSON = extras.getString("objectiveJSON")
            val uniqueId = extras.getInt("notifUniqueId")
            Log.d(LOG_DEBUG, "objJSON: " + objJSON!!)
            Log.d(LOG_DEBUG, "notifUniqueId: $uniqueId")
            val obj = Gson().fromJson(objJSON, Objective::class.java)

            if (obj != null) {
                notificationHandler.cancelNotification(uniqueId)
                val v = getKeyByValue(objectivesViewMap, obj)
                finishObjective(obj, v)
            }
        }
    }

    public override fun onPause() {
        super.onPause()

        //store saved instance state?

        //unregisterReceiver(br);
        Log.d(LOG_DEBUG, "Unregistered broadcast receiver")
    }

    public override fun onStop() {
        try {
            //unregisterReceiver(br);
        } catch (e: Exception) {
            // Receiver was probably already stopped in onPause()
        }

        super.onStop()
    }

    public override fun onDestroy() {
        stopService(Intent(this, BroadcastService::class.java))
        Log.i(LOG_DEBUG, "Stopped service")
        super.onDestroy()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        val timeblockObjectivesMapJSON = Gson().toJson(timeblockObjectivesMap)
        val objectivesMapJSON = Gson().toJson(objectivesMap)
        val timeblocksMapJSON = Gson().toJson(timeblocksMap)
        val objectivesViewMapJSON = Gson().toJson(objectivesViewMap)
        //String timerMapJSON = new Gson().toJson(timerMap);

        savedInstanceState.putString("timeblockObjectivesMapJSON", timeblockObjectivesMapJSON)
        savedInstanceState.putString("objectivesMapJSON", objectivesMapJSON)
        savedInstanceState.putString("timeblocksMapJSON", timeblocksMapJSON)
        savedInstanceState.putString("objectivesViewMapJSON", objectivesViewMapJSON)
        //savedInstanceState.putString("timerMapJSON", timerMapJSON);

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        // etc.

        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        //val myBoolean = savedInstanceState.getBoolean("MyBoolean")
        //val myDouble = savedInstanceState.getDouble("myDouble")
        //val myInt = savedInstanceState.getInt("MyInt")
        //val myString = savedInstanceState.getString("MyString")
    }

    private fun initDrawerItems() {

        dl = findViewById(R.id.activity_main)
        t = ActionBarDrawerToggle(this, dl, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        dl!!.addDrawerListener(t!!)
        t!!.syncState()

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        nv = findViewById(R.id.nv)
        nv!!.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->
            val id = item.itemId
            when (id) {

                R.id.new_timeblock -> {
                    Toast.makeText(this@MainActivity, "New Timeblock", Toast.LENGTH_SHORT).show()
                    onClickNewTimeBlock(null)
                }

                R.id.objective_list -> Toast.makeText(this@MainActivity, "Objective List", Toast.LENGTH_SHORT).show()


                R.id.settings -> Toast.makeText(this@MainActivity, "Settings", Toast.LENGTH_SHORT).show()


                R.id.timeblock_list -> Toast.makeText(this@MainActivity, "Timeblock List", Toast.LENGTH_SHORT).show()


                else -> return@OnNavigationItemSelectedListener true
            }

            true
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //unreachable?
        if (id == R.id.options_notify) {
            Toast.makeText(this@MainActivity, "Action clicked", Toast.LENGTH_LONG).show()
            notificationHandler.createNotification(null!!)
            return true
        }

        return if (t!!.onOptionsItemSelected(item)) true else super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.options, menu)
        return true
    }


    fun setTodaysDate() {
        Log.d("***DEBUG***", "inside setTodaysDate")

        val cal = Calendar.getInstance()
        val format = SimpleDateFormat("MMMM d, yyyy")
        val todaysDate = format.format(cal.time)
        supportActionBar!!.title = todaysDate
    }

    fun onClickNewTimeBlock(v: View?) {
        Log.d("***DEBUG***", "inside onClickNewTimeBlock")
        val openNewTimeBlock = Intent(this, TimeBlockPage::class.java)
        //startActivity(openNewTimeBlock);
        startActivityForResult(openNewTimeBlock, Integer.parseInt(APP_CREATE))
    }

    fun onClickEditTimeBlock(v: View) {
        Log.d("***Debug***", "inside onClickEditTimeBlock")

        try {
            for (obj in timeblocksMap.values) {
                if (obj.id === v.tag) {

                    val gson = Gson()
                    val timeblockJSON = gson.toJson(obj)
                    val extras = Bundle()
                    val editTimeBlock = Intent(this, TimeBlockPage::class.java)
                    editTimeBlock.addCategory(APP_EDIT)
                    //extras.putParcelable("user", user);
                    extras.putString("details", timeblockJSON)
                    editTimeBlock.putExtras(extras)
                    editTimeBlock.addCategory(APP_EDIT)
                    startActivityForResult(editTimeBlock, Integer.parseInt(APP_EDIT))
                }
            }
        } catch (e: Exception) {
            Log.d("***ERROR***", "objectiveActionClicked: " + e.message)
            Log.d("***ERROR***", "objectiveActionClicked: $e")
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
        }

    }

    fun onClickNewObjective(v: View) {
        Log.d("***DEBUG***", "inside onClickNewObjective")
        val openNewObjective = Intent(this, ObjectivePage::class.java)
        startActivity(openNewObjective)
    }

    fun onClickNewObjectiveForTimeblock(tbId: String) {
        Log.d("***DEBUG***", "inside onClickNewObjectiveForTimeblock")

        val openNewObjective = Intent(this, ObjectivePage::class.java)
        openNewObjective.putExtra("TimeblockId", tbId)
        startActivityForResult(openNewObjective, Integer.parseInt(APP_CREATE))
    }

    fun onClickAddObjectiveForTimeblock(tbId: String) {
        Log.d("***DEBUG***", "inside onClickNewObjectiveForTimeblock")

        val openNewObjective = Intent(this, ObjectiveList::class.java)
        openNewObjective.putExtra("TimeblockId", tbId)
        startActivityForResult(openNewObjective, Integer.parseInt(APP_EDIT))
    }

    fun getTimeBlocksAndObjectives() {
        Log.d("***Debug***", "inside getTimeBlocksAndObjectives")

        //populates timeblocksmap
        dbh.getTimeBlocksForMainActivity(this)


    }

    fun drawTimeBlockObjectives(timeblockObjsMap: Map<String, List<Objective>>) {
        Log.d("***Debug***", "inside drawTimeBlockObjectives")

        if (!llGroupList.isEmpty()) {
            for (ll in llGroupList) {
                ll.removeAllViews()
            }
        }

        val loadingPanel = findViewById<View>(R.id.loadingPanel)
        if (loadingPanel != null) {
            loadingPanel!!.visibility = View.GONE
        }

        val inflater = LayoutInflater.from(this)
        val ll_group_wrapper = findViewById<LinearLayout>(R.id.ll_group_wrapper)
        ll_group_wrapper.removeAllViews()
        val newGroup = LinearLayout(this)
        newGroup.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        newGroup.orientation = LinearLayout.VERTICAL
        ll_group_wrapper.addView(newGroup)

        try {
            // for each timeblock
            for (tbId in timeblockObjsMap.keys) {
                //if (timeblockObjsMap.get(tbId).size() > 0) { //get objectives for time block
                val v = inflater.inflate(R.layout.timeblock_header, null)
                val clTimeblockHeader = v.findViewById<ConstraintLayout>(R.id.cl_timeblock_header)
                val tv_timeblock_name = clTimeblockHeader.findViewById<TextView>(R.id.tv_timeblock_name)

                val tv_timeblock_timerange = clTimeblockHeader.findViewById<TextView>(R.id.tv_timeblock_timerange)
                val iv_addTask = clTimeblockHeader.findViewById<ImageView>(R.id.iv_add_objective)
                iv_addTask.setOnClickListener(onClickAddObjective)
                var currentTB: TimeBlock? = null
                if (timeblocksMap.containsKey(tbId)) {
                    currentTB = timeblocksMap[tbId]
                    tv_timeblock_name.tag = currentTB!!.id
                    tv_timeblock_name.text = currentTB.name
                    tv_timeblock_name.setOnClickListener(onClickEditTimeBlock)

                    //convert military time to regular time based on custom setting

                    val timeRange = currentTB!!.startTime + " - " + currentTB.endTime
                    tv_timeblock_timerange.text = timeRange

                    newGroup.addView(clTimeblockHeader)
                }

                // for each objective in a timeblock
                if (currentTB != null && timeblockObjsMap.containsKey(currentTB!!.id)) {
                    for (obj in timeblockObjsMap[currentTB.id]!!) {
                        //setup layout stuff

                        val objectiveCard = inflater.inflate(R.layout.objective_card_component, null, false)
                        objectiveCard.setPadding(0, 0, 0, 10)
                        objectiveCard.tag = obj

                        val cvActionObjective = objectiveCard.findViewById<CardView>(R.id.cv_card_action)
                        cvActionObjective.setOnClickListener(onClickActionObjective)

                        //initialize the layout fields
                        val objectiveName = objectiveCard.findViewById<TextView>(R.id.tv_objCard_Name)
                        //TextView objectiveDescription =  objectiveCard.findViewById(R.id.);
                        val objectiveDuration = objectiveCard.findViewById<TextView>(R.id.tv_objCard_Duration)
                        val objectiveEffort = objectiveCard.findViewById<TextView>(R.id.tv_objCard_Effort)
                        //TextView objectiveFrequency =  objectiveCard.findViewById(R.id.tv_objCard_Frequency);

                        //set values of imported components
                        objectiveName.setText(obj.name)
                        //objectiveDescription.setText(obj.description);
                        var objDuration = "0"
                        if (obj.duration != null && obj.duration != "") {
                            objDuration = parseDuration(Integer.parseInt(obj.duration!!))
                            val timer = TaskTimer(this, objectiveCard, Integer.parseInt(obj.duration!!))

                            timerMap[obj] = timer
                        }

                        objectiveDuration.text = objDuration
                        objectiveEffort.setText(obj.effort)
                        objectiveCard.setOnClickListener(onClickEditObjective)

                        //objectiveFrequency.setText(obj.frequency);

                        //Add margins to card for spacing, height, width
                        val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        params.setMargins(0, 5, 0, 5)
                        objectiveCard.layoutParams = params
                        newGroup.addView(objectiveCard)

                        objectiveCard.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                        objectivesViewMap[objectiveCard] = obj

                        if (obj.isComplete) {
                            objectiveCard.findViewById<View>(R.id.iv_completed).visibility = View.VISIBLE
                            objectiveCard.findViewById<View>(R.id.cv_card_action).visibility = View.GONE
                        }

                        llGroupList.add(newGroup)
                    }
                }
                //}
            }
        } catch (e: Exception) {
            Log.d("***ERROR***", "drawTimeBlockObjectives: " + e.message)
            Log.d("***ERROR***", "drawTimeBlockObjectives: $e")
            printStackTrace(e.stackTrace)
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
        }

    }

    private fun editObjective(v: View) {
        Log.d("***Debug***", "inside editObjective")

        try {
            //ViewGroup objectiveCard = (ViewGroup) v.getParent();

            for (obj in objectivesMap.values) {
                if (obj == v.tag) {

                    val gson = Gson()
                    val objectiveJSON = gson.toJson(obj)
                    val extras = Bundle()
                    val editObjective = Intent(this, ObjectivePage::class.java)
                    editObjective.addCategory(APP_EDIT)
                    //extras.putParcelable("user", user);
                    extras.putString("details", objectiveJSON)
                    editObjective.putExtras(extras)
                    startActivity(editObjective)
                }
            }
        } catch (e: Exception) {
            Log.d("***ERROR***", "objectiveActionClicked: " + e.message)
            Log.d("***ERROR***", "objectiveActionClicked: $e")
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
        }

    }

    fun objectiveActionClicked(v: View) {
        Log.d("***Debug***", "inside objectiveActionClicked")

        var objective: Objective? = null

        try {
            val objectiveCard = v.parent as ViewGroup
            var timer: TaskTimer? = null

            for (obj in objectivesMap.values) {
                if (obj == objectiveCard.tag) {
                    timer = timerMap[obj]
                    objective = obj
                }
            }


            if (timer != null && timer!!.isRunning) {
                Log.d("***Debug***", "Timer: " + timer.elapsedTime + " : " + timer!!.desiredTimeInMilliSeconds)
                if (timer.elapsedTime > timer!!.desiredTimeInMilliSeconds) {
                    finishObjective(objective!!, objectiveCard)
                } else if (timer.elapsedTime > 0) {
                    timer!!.pauseTimer()
                }
            } else {
                if (timer!!.elapsedTime == 0L) {
                    //startService(new Intent(this, BroadcastService.class));
                    timer.startTimer()

                } else {
                    timer!!.resumeTimer()
                }
            }
        } catch (e: Exception) {
            Log.d("***ERROR***", "objectiveActionClicked: " + e.message)
            Log.d("***ERROR***", "objectiveActionClicked: $e")
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
        }

    }


    private fun updateGUI(intent: Intent) {
        if (intent.extras != null) {
            val millisUntilFinished = intent.getLongExtra("countdown", 0)
            Log.i(LOG_DEBUG, "Countdown seconds remaining: " + millisUntilFinished / 1000)
        }
    }

    private fun finishObjective(objective: Objective, objCard: View?) {
        Log.d("***Debug***", "inside finishObjective")

        //get the view card and set as completed visually
        markFinished(objCard)

        //mark objective to be excluded from list
        objective.isComplete = true

        objective.dateCompleted = Calendar.getInstance().time

        //update database to show complete?
        dbh.updateObjective(objective)

        //prompt user to select next task to begin?
        //redraw list of objectives
        drawTimeBlockObjectives(timeblockObjectivesMap)
    }

    private fun parseDuration(duration: Int): String {
        val minutes = duration % 60
        val hours = duration / 60
        val seconds = 0
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun onClickCreateNotification(v: View) {
        Log.d("***Debug***", "inside onClickCreateNotification")

        notificationHandler.createNotification(v)
    }

    private fun initializeNavigationView() {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            mDrawerLayout!!.closeDrawers()

            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            true
        }
    }

    fun setTimeBlockObjectivesMap(tbm: Map<String, TimeBlock>, objm: Map<String, Objective>) {
        Log.d("***Debug***", "inside setTimeBlockObjectivesMap")

        for (tb in tbm.values) {

            val endTime = TimeConverter.convertDisplayToDate(tb.endTime!!)
            val currentTime = TimeConverter.currentTime

            //anything where the endtime has passed should be excluded
            if (!timeblockObjectivesMap.containsKey(tb.id) && endTime.after(currentTime)) {
                timeblockObjectivesMap[tb.id!!] = ArrayList()
            }

            for (obj in objm.values) {
                if (obj.timeblockId != null && obj.timeblock != null && obj.timeblock!!.id == tb.id && timeblockObjectivesMap.containsKey(tb.id)) {
                    timeblockObjectivesMap[obj.timeblockId!!]?.add(obj)
                }
            }
        }

        drawTimeBlockObjectives(timeblockObjectivesMap)
    }

    fun setCurrentTimeBlock(tb: TimeBlock) {
        Log.d("***DEBUG***", "inside MA.CurrentTimeBlock")
        this.currentTimeBlock = tb

        dbh.getObjectivesForTimeBlock(this, currentTimeBlock!!)
    }

    fun setTimeBlocksMap(timeblocksMap: MutableMap<String, TimeBlock>) {
        Log.d("***DEBUG***", "inside MA.setTimeBlocksMap")
        this.timeblocksMap = timeblocksMap

        //populates , when finished, drawTimeBlockObjs will be called
        dbh.getObjectivesForMainActivity(this)
    }

    fun setObjectivesMap(objectivesMap: MutableMap<String, Objective>) {
        Log.d("***Debug***", "inside setObjectivesMap")

        this.objectivesMap = objectivesMap

        //db call finished, create the final map
        setTimeBlockObjectivesMap(timeblocksMap, objectivesMap)

    }

    private fun showObjectiveOptionsDialog(tbId: String) {
        val inflater = layoutInflater
        val dialoglayout = inflater.inflate(R.layout.fragment_add_objective_options, null)
        val tvNew = dialoglayout.findViewById<TextView>(R.id.tv_from_new)
        val tvExisting = dialoglayout.findViewById<TextView>(R.id.tv_from_existing)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialoglayout)
        val ad = builder.show()

        tvNew.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                onClickNewObjectiveForTimeblock(tbId)
                ad.dismiss()
            }
        })
        tvExisting.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                onClickAddObjectiveForTimeblock(tbId)
                ad.dismiss()
            }
        })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val reqCode = (requestCode).toString() + ""
        // Check which request we're responding to
        if (reqCode == APP_CREATE || reqCode == APP_EDIT) {

            //run queries again and refresh the display to show new / edited / deleted data
            getTimeBlocksAndObjectives()

            // Make sure the request was successful
            /*if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }*/
        }
    }

    override fun onFinishEditDialog(inputText: String, tbId: String) {
        Toast.makeText(this, "Hi, $inputText", Toast.LENGTH_SHORT).show()
        if (inputText == "New") {
            onClickNewObjectiveForTimeblock(tbId)
        } else {
            onClickAddObjectiveForTimeblock(tbId)
        }
    }

    private fun markFinished(objCard: View?) {

        if (objCard != null) {
            //objCard.setBackgroundColor(Color.GREEN);

            //objCard.refreshDrawableState();

        }

    }

    companion object {

        internal val APP_CREATE = "0"
        internal val APP_READ = "1"
        internal val APP_EDIT = "2"
        internal val APP_DELETE = "3"

        var LOG_DEBUG = "***DEBUG***"

        fun printStackTrace(ste: Array<StackTraceElement>) {
            for (s in ste) {
                Log.d("***ERROR***", "drawTimeBlockObjectives$s")
            }
        }

        fun <T, E> getKeyByValue(map: Map<T, E>, value: E): T? {
            for (entry in map.entries) {
                if (value == entry.value) {
                    return entry.key
                }
            }
            return null
        }
    }

}
