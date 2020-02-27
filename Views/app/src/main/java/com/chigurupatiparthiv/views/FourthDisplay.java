package com.chigurupatiparthiv.views;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class FourthDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth_display);
        findViewById(R.id.smallMiddle).setBackgroundColor(Color.GRAY);
        findViewById(R.id.bigMiddle).setBackgroundColor(Color.WHITE);
        findViewById(R.id.rightMiddle).setBackgroundColor(Color.BLUE);
        findViewById(R.id.leftMiddle).setBackgroundColor(Color.RED);
    }

    public void goBack(View view) {
        finish();
    }
}
