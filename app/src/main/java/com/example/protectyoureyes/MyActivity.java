package com.example.protectyoureyes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public abstract class MyActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    protected void initToolbar(int id){
        toolbar = (Toolbar) findViewById(id);
        setSupportActionBar(toolbar);
    }
}
