package com.chigurupatiparthiv.views;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class SeventhDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seventh_display);
        findViewById(R.id.topMiddle2).setBackgroundColor(Color.GRAY);
    }

    public void goBack(View view) {
        finish();
    }
}
