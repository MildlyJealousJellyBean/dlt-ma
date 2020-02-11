package com.chigurupatiparthiv.buttontext;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    EditText responseEditText;
    TextView nameText;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

/*
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

    @SuppressLint("SetTextI18n")
    public void submitAction(View view) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String nameText = responseText.getText().toString().trim();
        String gradeText = gradeInt.getText().toString().trim();
        Log.i("responseText", nameText);
        int gradeInt = Log.i("gradeInt", gradeText);
        Log.i("clickTime", String.format("%s\n", dateFormat.format(new Date())));
        if(nameText.equals("")) {
            nameText = "User";
        }
        changeActionBarTitle(String.format("Hello %s!", nameText));
        displayText.setText(getString(R.string.center_text) + " " + Integer.toString(++incrementValue));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                changeActionBarTitle(getString(R.string.app_name));
            }
        }, 3000);
    }

    public void changeActionBarTitle(String title) {
        setTitle(title);
    }
}

 */