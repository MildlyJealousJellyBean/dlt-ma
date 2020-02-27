package com.chigurupatiparthiv.views;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class ThirdDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_display);
        findViewById(R.id.bottomMiddle).setBackgroundColor(Color.GRAY);
        findViewById(R.id.middleMiddle).setBackgroundColor(Color.GRAY);
        findViewById(R.id.topMiddle).setBackgroundColor(Color.GRAY);
    }

    public void goBack(View view) {
        finish();
    }
}
