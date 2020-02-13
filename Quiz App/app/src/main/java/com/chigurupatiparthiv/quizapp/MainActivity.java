package com.chigurupatiparthiv.quizapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    EditText responseText;
    EditText gradeInt;
    TextView displayText;
    int incrementValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        responseText = findViewById(R.id.responseEditText);
        gradeInt = findViewById(R.id.response2EditText);
        displayText = findViewById(R.id.textBox);
        responseText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    if(responseText.getText().toString().equals(getString(R.string.name_match))) {
                        displayText.setText(R.string.name_match_message);
                    }
                }
                else {
                    displayText.setText(R.string.center_text);
                    responseText.setText("");
                    responseText.setHint(R.string.hint1_text);
                }
            }
        });
    }
}
