package com.g09.levels;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.g09.MainActivity;
import com.g09.R;

import java.util.Timer;
import java.util.TimerTask;


//Żyroskop			LSM330 Gyroscope sensor				Trzeba szybko zakrecić telefonem
//You spin me right round

public class Lvl1 extends Level {
    private SensorManager mSensorManager;
    private Sensor mGyroscopeSensor;
    int gyroscopeValue;
    float max;
    TextView lvl1txt;
    TextView timeTxt;

    double a;
    Timer timer = new Timer();
    Handler handler = new Handler();

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate() {
        setContentView(R.layout.lvl1);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lvl1txt = (TextView)findViewById(R.id.lvl1txt);
        timeTxt = findViewById(R.id.time1);
        ImageButton hint = findViewById(R.id.hint1);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHint("Czynność powinna wzorować się na ruchu wskazówek zegara.");
            }
        });

        a = startTimer();
        start();
    }

    public void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null) {
            noSensorsAlert();
        }
        else {
            mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            max = mGyroscopeSensor.getMaximumRange();
            if(max*0.9 < 30)
                max *= 0.9;
            else
                max = 5;
            mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
        }

        if(getFlagTime()) {
            final int[] i = {0};
            timeTxt.setVisibility(View.VISIBLE);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            timeTxt.setText(String.valueOf(i[0]));
                            i[0]++;
                        }
                    });
                }
            }, 0, 1000);
        }
    }

    public void stop() {
        mSensorManager.unregisterListener(this, mGyroscopeSensor);
        timer.cancel();
        timer.purge();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        gyroscopeValue = (int) event.values[2];
        String f = "";
        if(gyroscopeValue < -max) {
            double b = stopTimer();
            f = "\nudalo sie ";
            lvl1txt.setText(String.valueOf(gyroscopeValue) + f + String.valueOf(max));
            winAlert("Gratulację!", "Udalo Ci się przejść poziom!\nTwój czas: " + calculateElapsedTime(a, b) + " sekund", Lvl2.class);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("stats1", (float)calculateElapsedTime(a, b));
            editor.apply();
            if(getScore("stats1") < getCurrentHighScore("stats1CurrentHS"))
                editor.putFloat("stats1CurrentHS", (float)calculateElapsedTime(a, b));
            editor.apply();

            stop();
        }
    }
}
