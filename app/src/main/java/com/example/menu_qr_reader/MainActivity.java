package com.example.menu_qr_reader;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.menu_qr_reader.activities.ScanBySchoolActivity;
import com.example.menu_qr_reader.activities.ScanBySelfActivity;



public class MainActivity extends AppCompatActivity {
    private Button schoolBtn, selfBtn;
    private boolean doubleCheckToExit = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.schoolBtn = (Button) findViewById(R.id.by_school_btn);
        this.selfBtn = (Button) findViewById(R.id.by_self_btn);

        schoolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goScannerIntent = new Intent(MainActivity.this, ScanBySchoolActivity.class);
                startActivity(goScannerIntent);
            }
        });

        selfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goScannerIntent = new Intent(MainActivity.this, ScanBySelfActivity.class);
                startActivity(goScannerIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleCheckToExit) {  // if it's true, it means user already clicked once
            super.onBackPressed();  // quit app
            return;
        }

        this.doubleCheckToExit = true;
        Toast.makeText(this, "앱을 종료하려면 다시 한 번 눌러주세요", Toast.LENGTH_LONG).show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {  // wait 2sec and make it as false again
            @Override
            public void run() {
                doubleCheckToExit = false;
            }
        }, 2000);
    }
}
