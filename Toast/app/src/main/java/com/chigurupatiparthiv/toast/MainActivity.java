package com.chigurupatiparthiv.toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        simpleSeekBar = (SeekBar)findViewById()
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
