package com.myra.assistant;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Myra AI Assistant is Active!");
        tv.setTextSize(24);
        tv.setGravity(17);
        setContentView(tv);
    }
}
