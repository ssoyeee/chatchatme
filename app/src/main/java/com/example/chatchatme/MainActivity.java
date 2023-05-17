package com.example.chatchatme;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatchatme.fragment.ChatFragment;
import com.example.chatchatme.fragment.PeopleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.mainactivity2_bottomnavigationview);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_people:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity2_framelayout, new PeopleFragment()).commit();
                        return true;
                    case R.id.action_chat:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity2_framelayout, new ChatFragment()).commit();
                        return true;
                }
                return false;
            }
        });
    }
}
