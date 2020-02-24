package com.chigurupatiparthiv.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Class[] screenNames = new Class[]{FirstDisplay.class, SecondDisplay.class, ThirdDisplay.class, FourthDisplay.class, FifthDisplay.class, SixthDisplay.class, SeventhDisplay.class};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.switcherText);
    }

    public void changeDisplay(View view) {
        int index = -1;
        switch (view.getId()) {
            case (R.id.goTo1):
                index = 0;
                break;
            case (R.id.goTo2):
                index = 1;
                break;
            case (R.id.goTo3):
                index = 2;
                break;
            case (R.id.goTo4):
                index = 3;
                break;
            case (R.id.goTo5):
                index = 4;
                break;
            case (R.id.goTo6):
                index = 5;
                break;
            case (R.id.goTo7):
                index = 6;
                break;
        }
        Intent nextScreen = new Intent(getApplicationContext(), screenNames[index]);
        Log.i("Index", Integer.toString(index));
        startActivity(nextScreen);
    }
}


/*
package com.chigurupatiparthiv.toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    int[] textViewCounters = new int[4];
    String[] toastMessages = new String[]{"Top Left TextView Clicked!\nTotal Clicks: %d",
            "Top Right TextView Clicked!\nTotal Clicks: %d",
            "Bottom Left TextView Clicked!\nTotal Clicks: %d",
            "Bottom Right TextView Clicked!\nTotal Clicks: %d"};
    String[] intID = new String[]{"topLeft", "topRight", "bottomLeft", "bottomRight"};
    SharedPreferences mPreferences;
    SeekBar simpleSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = getSharedPreferences("com.chigurupatiparthiv.toast.sharedprefs", MODE_PRIVATE);
        for (int index = 0; index < 4; index++) {
            textViewCounters[index] = mPreferences.getInt(intID[index], 0);
        }
        simpleSeekBar = findViewById(R.id.simpleSeekBar);
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            SharedPreferences.Editor preferencesEditor = mPreferences.edit();
            int progressChangedValue = mPreferences.getInt("seekBar", 0);
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferencesEditor.putInt("seekBar", progressChangedValue);
                preferencesEditor.apply();
                TextView changeTextSize = findViewById(R.id.topLeft);
                changeTextSize.setTextSize(TypedValue.COMPLEX_UNIT_PT, progressChangedValue);
                changeTextSize = findViewById(R.id.topRight);
                changeTextSize.setTextSize(TypedValue.COMPLEX_UNIT_PT, progressChangedValue);
                changeTextSize = findViewById(R.id.bottomRight);
                changeTextSize.setTextSize(TypedValue.COMPLEX_UNIT_PT, progressChangedValue);
                changeTextSize = findViewById(R.id.bottomLeft);
                changeTextSize.setTextSize(TypedValue.COMPLEX_UNIT_PT, progressChangedValue);
                Toast.makeText(MainActivity.this, "Font Size: " + progressChangedValue, Toast.LENGTH_SHORT).show();
            }
        });
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
}

 */