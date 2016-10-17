package com.na.camera.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.na.base.NaBaseActivity;
import com.na.camera.app.R;
import com.na.camera.app.ui.CameraActivity;

public class LauncherActivity extends NaBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Button btCamera = (Button) findViewById(R.id.btCamera);
        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goCameraActivity();
            }
        });

        Button btMain = (Button) findViewById(R.id.btMain);
        btMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goMainActivity();
            }
        });
    }

    private void goMainActivity() {

    }

    private void goCameraActivity() {
        startActivity(new Intent(this, CameraActivity.class));
    }
}
