package com.chigurupatiparthiv.activitylifecycle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LifecycleObserver {
    int[] textViewCounters = new int[4];
    int[] lifecycleCounters = new int[14];
    String[] counters;
    String[] toastMessages = new String[]{"Top Left TextView Clicked!\nTotal Clicks: %d",
            "Top Right TextView Clicked!\nTotal Clicks: %d",
            "Bottom Left TextView Clicked!\nTotal Clicks: %d",
            "Bottom Right TextView Clicked!\nTotal Clicks: %d"};
    String[] intID = new String[]{"topLeft", "topRight", "bottomLeft", "bottomRight"};
    String[] eventTrackers = new String[]{"onCreate", "onCreate2", "onStart", "onStart2", "onResume", "onResume2", "onPause", "onPause2", "onStop", "onStop2", "onRestart", "onRestart2", "onDestroy", "onDestroy2"};
    // Regular is the current counter, 2 is the lifecycle counter
    SharedPreferences mPreferences;
    TextView onCreate;
    TextView onStart;
    TextView onResume;
    TextView onPause;
    TextView onStop;
    TextView onRestart;
    TextView onDestroy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = getSharedPreferences("com.chigurupatiparthiv.activitylifecycle.sharedprefs", MODE_PRIVATE);
        for (int index = 0; index < 4; index++) {
            textViewCounters[index] = mPreferences.getInt(intID[index], 0);
        }
        for (int index = 0; index < 14; index++) {
            lifecycleCounters[index] = mPreferences.getInt(eventTrackers[index], 0);
        }
        lifecycleCounters[0] += 1; // Updates the current counter
        lifecycleCounters[1] += 1; // Update the lifecycle counter
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(eventTrackers[1], lifecycleCounters[1]);  // Updates lifecycle count
        preferencesEditor.apply();
        counters = getResources().getStringArray(R.array.counters);
        onCreate = findViewById(R.id.onCreate);
        onStart = findViewById(R.id.onStart);
        onResume = findViewById(R.id.onResume);
        onPause = findViewById(R.id.onPause);
        onStop = findViewById(R.id.onStop);
        onRestart = findViewById(R.id.onRestart);
        onDestroy = findViewById(R.id.onDestroy);
        updateText();
    }

    public void onStart() {
        super.onStart();
        lifecycleCounters[2] += 1; // Updates the current counter
        lifecycleCounters[3] += 1; // Update the lifecycle counter
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(eventTrackers[3], lifecycleCounters[3]);  // Updates lifecycle count
        preferencesEditor.apply();
        updateText();
    }

    public void onResume() {
        super.onResume();
        lifecycleCounters[4] += 1; // Updates the current counter
        lifecycleCounters[5] += 1; // Update the lifecycle counter
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(eventTrackers[5], lifecycleCounters[5]);  // Updates lifecycle count
        preferencesEditor.apply();
        updateText();
    }

    public void onPause() {
        super.onPause();
        lifecycleCounters[6] += 1; // Updates the current counter
        lifecycleCounters[7] += 1; // Update the lifecycle counter
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(eventTrackers[7], lifecycleCounters[7]);  // Updates lifecycle count
        preferencesEditor.apply();
        updateText();
    }

    public void onStop() {
        super.onStop();
        lifecycleCounters[8] += 1; // Updates the current counter
        lifecycleCounters[9] += 1; // Update the lifecycle counter
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(eventTrackers[9], lifecycleCounters[9]);  // Updates lifecycle count
        preferencesEditor.apply();
        updateText();
    }

    public void onRestart() {
        super.onRestart();
        lifecycleCounters[10] += 1; // Updates the current counter
        lifecycleCounters[11] += 1; // Update the lifecycle counter
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(eventTrackers[11], lifecycleCounters[11]);  // Updates lifecycle count
        preferencesEditor.apply();
        updateText();
    }

    public void onDestroy() {
        super.onDestroy();
        lifecycleCounters[12] += 1; // Updates the current counter
        lifecycleCounters[13] += 1; // Update the lifecycle counter
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(eventTrackers[13], lifecycleCounters[13]);  // Updates lifecycle count
        preferencesEditor.apply();
        updateText();
    }

    public void updateText() {
        onCreate.setText(String.format(counters[0], lifecycleCounters[0], lifecycleCounters[1]));
        onStart.setText(String.format(counters[1], lifecycleCounters[2], lifecycleCounters[3]));
        onResume.setText(String.format(counters[2], lifecycleCounters[4], lifecycleCounters[5]));
        onPause.setText(String.format(counters[3], lifecycleCounters[6], lifecycleCounters[7]));
        onStop.setText(String.format(counters[4], lifecycleCounters[8], lifecycleCounters[9]));
        onRestart.setText(String.format(counters[5], lifecycleCounters[10], lifecycleCounters[11]));
        onDestroy.setText(String.format(counters[6], lifecycleCounters[12], lifecycleCounters[13]));
    }

    public void showToast(View view) {
        int index = -1;
        switch (view.getId()) {
            case (R.id.topLeft):
                index = 0;
                break;
            case (R.id.topRight):
                index = 1;
                break;
            case (R.id.bottomLeft):
                index = 2;
                break;
            case (R.id.bottomRight):
                index = 3;
                break;
        }
        textViewCounters[index] += 1;
        String toastMessage = String.format(toastMessages[index], textViewCounters[index]);
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, toastMessage, duration);
        toast.show();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(intID[index], textViewCounters[index]);
        preferencesEditor.apply();
    }

    public void resetCounters(View view) {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        for (int index = 0; index < 14; index++) {
            lifecycleCounters[index] = 0;
            preferencesEditor.putInt(eventTrackers[index], 0);
        }
        preferencesEditor.apply();
        updateText();
    }
}
