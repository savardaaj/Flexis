package com.asav.flexi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
}
