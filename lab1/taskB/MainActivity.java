package com.example.lab1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ContentResolver mContentResolver = null;
    private Cursor cursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Button) findViewById(R.id.button1)).setOnClickListener(this);
        ((Button) findViewById(R.id.button2)).setOnClickListener(this);
        ((Button) findViewById(R.id.button3)).setOnClickListener(this);
        ((Button) findViewById(R.id.button4)).setOnClickListener(this);
        ((Button) findViewById(R.id.button5)).setOnClickListener(this);
        BroadcastReceiver br = new MyClass1();
        IntentFilter intentFilter = new IntentFilter("com.android.skill");
        intentFilter.addAction("android.intent.action.MY_BROADCAST");
        registerReceiver(br, intentFilter);
        this.mContentResolver = getContentResolver();
        ((TextView) findViewById(R.id.textView)).setText("Add initial data ");
        for (int i = 0; i < 10; i++) {
            ContentValues values = new ContentValues();
            values.put(Constant.COLUMN_NAME, "haha" + i);
            this.mContentResolver.insert(Constant.CONTENT_URI, values);
        }

    }

    @Override
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.button1:
                    Intent startIntent = new Intent(this, MyClass2.class);
                    startService(startIntent);
                    break;
                case R.id.button2:
                    Intent stopIntent = new Intent(this, MyClass2.class);
                    stopService(stopIntent);
                    break;
                case R.id.button3:
                    Intent custIntent = new Intent();
                    custIntent.setAction("com.exmaple.CUSTOM_INTENT");
                    custIntent.setPackage("com.example.myapplication");
                    sendBroadcast(custIntent);
                case R.id.button4 /* 2131230810 */:
                    sendBroadcast(new Intent("android.intent.action.MY_BROADCAST"));
                    break;
                case R.id.button5 /* 2131230811 */:
                    break;
                default:
                    return;
            }
            TextView tv = (TextView) findViewById(R.id.textView);
            tv.setText("Query Data ");
            Cursor query = this.mContentResolver.query(Constant.CONTENT_URI, new String[]{Constant.COLUMN_ID, Constant.COLUMN_NAME}, null, null, null);
            this.cursor = query;
            if (query.moveToFirst()) {
                Cursor cursor = this.cursor;
                tv.setText("The first data： " + cursor.getString(cursor.getColumnIndex(Constant.COLUMN_NAME)));
            }
        }
    }
}

