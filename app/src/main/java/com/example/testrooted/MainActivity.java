package com.example.testrooted;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView txtResult = findViewById(R.id.txtResult);
        findViewById(R.id.btnTestRoot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestRoot testRoot = new TestRoot(MainActivity.this);
                if (testRoot.isRoot()) {
                    txtResult.setText("IS ROOT");
                    txtResult.setTextColor(Color.RED);
                } else {
                    txtResult.setText("NOT ROOT");
                    txtResult.setTextColor(Color.BLACK);
                }
            }
        });
    }
}