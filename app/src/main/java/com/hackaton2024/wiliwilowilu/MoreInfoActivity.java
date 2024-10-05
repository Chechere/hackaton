package com.hackaton2024.wiliwilowilu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MoreInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_more_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();

        String title_str = intent.getStringExtra("TITLE");

        TextView title = findViewById(R.id.DataTitle);
        TextView avg_day = findViewById(R.id.tv_avg_day);
        TextView avg_week = findViewById(R.id.tv_avg_week);
        TextView avg_month = findViewById(R.id.tv_avg_month);
        TextView max_day = findViewById(R.id.tv_max_day);
        TextView min_day = findViewById(R.id.tv_min_day);

        if(title_str != null) {
            title.setText(title_str);
        }



    }
}