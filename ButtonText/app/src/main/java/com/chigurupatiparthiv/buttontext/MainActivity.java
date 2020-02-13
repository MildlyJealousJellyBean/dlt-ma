package com.chigurupatiparthiv.buttontext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    EditText responseEditText;
    TextView nameText;
    TextView messageText;
    String[] messages;
    int messageArrayLength;
    int arrayIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        responseEditText = findViewById(R.id.responseEditText);
        nameText = findViewById(R.id.nameView);
        messageText = findViewById(R.id.messageView);
        Resources res = getResources();
        messages = res.getStringArray(R.array.messages);
        messageArrayLength = messages.length;
    }

    public void changeName(View view) {
        String name = responseEditText.getText().toString().trim();
        Log.i("Response Text", name);
        if(name.equals("")) {
            name = "User";
        }
        nameText.setText(String.format("Hello %s!", name));
    }

    public void changeMessage(View view) {
        String name = responseEditText.getText().toString().trim();
        Log.i("Response Text", name);
        if (name.equals("")) {
            name = "User";
        }
        messageText.setText(String.format(messages[arrayIndex], name));
        updateIndex();
    }

    public void updateIndex() {
        arrayIndex++;
        if (arrayIndex == messageArrayLength) {
            arrayIndex = 0;
        }
    }
}