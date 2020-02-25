package com.chigurupatiparthiv.views;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class SecondDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_display);
        findViewById(R.id.topMiddle).setBackgroundColor(Color.GRAY);
        findViewById(R.id.middleRight).setBackgroundColor(Color.GRAY);
        findViewById(R.id.middleLeft).setBackgroundColor(Color.GRAY);
    }

    public void goBack(View view) {
        finish();
    }
}
