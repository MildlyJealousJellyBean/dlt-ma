package com.chigurupatiparthiv.views;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class FirstDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_display);
        findViewById(R.id.topLeft).setBackgroundColor(Color.GRAY);
        findViewById(R.id.topRight).setBackgroundColor(Color.GRAY);
        findViewById(R.id.bottomLeft).setBackgroundColor(Color.GRAY);
        findViewById(R.id.bottomRight).setBackgroundColor(Color.GRAY);
    }

    public void goBack(View view) {
        finish();
    }
}
