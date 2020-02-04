package com.chigurupatiparthiv.quizapp;

import androidx.appcompat.app.AppCompatActivity;

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
    private Button submitButton;
    EditText responseText;
    EditText gradeInt;
    TextView displayText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        submitButton = findViewById(R.id.clickButton);
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

    public void submitAction(View view) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String nameText = responseText.getText().toString().trim();
        String gradeText = gradeInt.getText().toString().trim();
        Log.i("responseText", nameText);
        Log.i("gradeInt", gradeText);
        Log.i("clickTime", String.format("%s\n", dateFormat.format(new Date())));
        if(nameText.equals("")) {
            nameText = "User";
        }
        changeActionBarTitle(String.format("Hello %s!", nameText));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                changeActionBarTitle(R.string.app_name.toString());
            }
        }, 3000);

    }

    public void changeActionBarTitle(String title) {
        setTitle(title);
    }
}
