package com.AudioRecorder;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

public class RecordActivity extends Activity implements SensorEventListener {
    /**
     * Called when the activity is first created.
     */

    private static final String TAG = "RecordActivity";

    private SensorManager mSensorManager;

    private final String ALL_SENSORS = "All Sensors";
    private final String ACCELEROMETER = "Accelerometer";
    private final String GYROSCOPE = "Gyroscope";
    private final String BAROMETER = "Barometer";
    private final String COMPASS = "Compass";

    private Sensor mAccelerometer;
    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private final float NOISE = (float) 2.0;

    private Sensor mBarometer;

    private Sensor mCompass;

    private Sensor mGyroscope;

    private String mSensorSelected;

    private TextView mLabel_1;
    private TextView mLabel_2;
    private TextView mLabel_3;
    private TextView mLabelReading_1;
    private TextView mLabelReading_2;
    private TextView mLabelReading_3;

    private static final int NUM_SENSORS = 4;
    private BufferedWriter[] mSensorDataWriters = new BufferedWriter[NUM_SENSORS];
    private static final int ACCELEROMETER_IDX = 0;
    private static final int GYROSCOPE_IDX = 1;
    private static final int BAROMETER_IDX = 2;
    private static final int COMPASS_IDX = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mInitialized = false;

        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        Log.d(TAG, "Gyroscope range = " + mGyroscope.getMaximumRange());
        Log.d(TAG, "Accelerometer range = " + mAccelerometer.getMaximumRange());
        Log.d(TAG, "Barometer range = " + mBarometer.getMaximumRange());
        Log.d(TAG, "Compass range = " + mCompass.getMaximumRange());

        // Sample code for getting all the sensors present on the device
//        mLabel_1 = (TextView) findViewById(R.id.label_1);
//        List<Sensor> mList
//                = mSensorManager.getSensorList(Sensor.TYPE_ALL);
//        for (int i = 1; i < mList.size(); i++) {
//            mLabel_1.append("\n" + mList.get(i).getName());
//        }

        mLabel_1 = (TextView) findViewById(R.id.label_1);
        mLabel_2 = (TextView) findViewById(R.id.label_2);
        mLabel_3 = (TextView) findViewById(R.id.label_3);
        mLabelReading_1 = (TextView) findViewById(R.id.label_1_reading);
        mLabelReading_2 = (TextView) findViewById(R.id.label_2_reading);
        mLabelReading_3 = (TextView) findViewById(R.id.label_3_reading);

        Spinner sensorSelector = (Spinner) findViewById(R.id.sensor_selector);
        sensorSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSensorSelected = parent.getItemAtPosition(position).toString();
                mLabelReading_1.setVisibility(View.VISIBLE);
                mLabelReading_2.setVisibility(View.VISIBLE);
                mLabelReading_3.setVisibility(View.VISIBLE);
                if (mSensorSelected.equals(ALL_SENSORS)) {
                    mLabel_1.setVisibility(View.GONE);
                    mLabel_3.setVisibility(View.GONE);
                    mLabelReading_1.setVisibility(View.GONE);
                    mLabelReading_2.setVisibility(View.GONE);
                    mLabelReading_3.setVisibility(View.GONE);
                } else if (mSensorSelected.equals(ACCELEROMETER) || mSensorSelected.equals(GYROSCOPE)) {
                    mLabel_1.setVisibility(View.VISIBLE);
                    mLabel_2.setVisibility(View.VISIBLE);
                    mLabel_3.setVisibility(View.VISIBLE);
                    mLabel_1.setText("X-Axis");
                    mLabel_2.setText("Y-Axis");
                    mLabel_3.setText("Z-Axis");
                } else if (mSensorSelected.equals(BAROMETER)) {
                    mLabel_1.setVisibility(View.GONE);
                    mLabel_2.setVisibility(View.VISIBLE);
                    mLabel_3.setVisibility(View.GONE);
                    mLabel_2.setText("Pressure change");
                } else if (mSensorSelected.equals(COMPASS)) {
                    mLabel_1.setVisibility(View.VISIBLE);
                    mLabel_2.setVisibility(View.VISIBLE);
                    mLabel_3.setVisibility(View.VISIBLE);
                    mLabel_1.setText("Azimuth");
                    mLabel_2.setText("Pitch");
                    mLabel_3.setText("Roll");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button startRecordingButton = (Button) findViewById(R.id.start_recording_button);
        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAudioRecording();
            }
        });

        Button stopRecordingButton = (Button) findViewById(R.id.stop_recording_button);
        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudioRecording();
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored for this app
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            onAccelerometerChanged(event);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            onGyroscopeChanged(event);
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            onBarometerChanged(event);
        } else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            onCompassChanged(event);
        }
    }

    public void onAccelerometerChanged(SensorEvent event) {

        long timeStamp = event.timestamp;

        // Acceleration in the three coordinates
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        onSensorValuesChanged(timeStamp, x, y, z, ACCELEROMETER_IDX);
    }

    public void onGyroscopeChanged(SensorEvent event) {

        long timeStamp = event.timestamp;

        // Angular speeds in the three coordinates
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        onSensorValuesChanged(timeStamp, x, y, z, GYROSCOPE_IDX);
    }

    public void onBarometerChanged(SensorEvent event) {

        long timeStamp = event.timestamp;
        float pressure = event.values[0];

        onSensorValuesChanged(timeStamp, pressure, BAROMETER_IDX);
    }

    public void onCompassChanged(SensorEvent event) {

        long timeStamp = event.timestamp;

        float azimuth = event.values[0];
        float pitch = event.values[1];
        float roll = event.values[2];

        onSensorValuesChanged(timeStamp, azimuth, pitch, roll, COMPASS_IDX);
    }

    private void onSensorValuesChanged(long timeStamp, float x, float y, float z, int sensorIdx) {

        String toWrite = getStringToWrite(timeStamp, x, y, z);
        writeSensorReadingsToFile(sensorIdx, toWrite);

        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mLabelReading_1.setText("0.0");
            mLabelReading_2.setText("0.0");
            mLabelReading_3.setText("0.0");
            mInitialized = true;
        } else {
            float deltaX = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);
//            if (deltaX < NOISE) deltaX = (float)0.0;
//            if (deltaY < NOISE) deltaY = (float)0.0;
//            if (deltaZ < NOISE) deltaZ = (float)0.0;
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mLabelReading_1.setText(Float.toString(round(deltaX, 6)));
            mLabelReading_2.setText(Float.toString(round(deltaY, 6)));
            mLabelReading_3.setText(Float.toString(round(deltaZ, 6)));
        }
    }

    private void onSensorValuesChanged(long timeStamp, float value, int sensorIdx) {

        String toWrite = getStringToWrite(timeStamp, value);
        writeSensorReadingsToFile(sensorIdx, toWrite);

        if (!mInitialized) {
            mLastX = value;
            mLabelReading_2.setText("0.0");
            mInitialized = true;
        } else {
            float deltaX = Math.abs(mLastX - value);
//            if (deltaX < NOISE) deltaX = (float)0.0;
            mLastX = value;
            mLabelReading_2.setText(Float.toString(round(deltaX, 6)));
        }
    }

    public BufferedWriter getSensorDataWriter(String sensorName) {

        // Check if external storage is available for writing
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "External storage not available for writing.");
            return null;
        }

        // Get the directory in external storage to write sensor readings to.
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        // Internal storage
        //File root = this.getFilesDir();
        String fileName = sensorName + ".txt";
        File file = new File(root.getAbsolutePath() + File.separator + fileName);

        // Create a BufferedWriter stream for writing to the file
        BufferedWriter out;
        try {
            FileWriter filewriter = new FileWriter(file);
            out = new BufferedWriter(filewriter);
        } catch (IOException e) {
            Log.e("TAG", "Could not create now file writer " + e.getMessage());
            System.out.println("Failed here");
            return null;
        }

        return out;
    }

    public void writeSensorReadingsToFile(int sensorIdx, String valuesLine) {

        BufferedWriter out = mSensorDataWriters[sensorIdx];
        try {
            out.write(valuesLine);
        } catch (IOException e) {
            Log.e(TAG, "Could not write file: " + e.getMessage());
            Log.e(TAG, "Failed here");
        } catch (NullPointerException e) {
            Log.e(TAG, "Buffered writer is null: " + e.getMessage());
        }
        //out.close();
    }

    private String getStringToWrite(long timeStamp, float x, float y, float z) {
        return timeStamp + ", " + new DecimalFormat("#.######").format(x) + ", "
                + new DecimalFormat("#.######").format(y) + ", "
                + new DecimalFormat("#.######").format(z) + "\n";
    }

    private String getStringToWrite(long timeStamp, float value) {
        return timeStamp + ", " + new DecimalFormat("#.######").format(value) + "\n";
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public float round(float d, int decimalPlace) {

        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public void startAudioRecording() {


        if (mSensorSelected.equals(ALL_SENSORS)) {
            mLabel_2.setText("Recording from all sensors...");
            registerListenerToSensor(mAccelerometer, ACCELEROMETER);
            registerListenerToSensor(mGyroscope, GYROSCOPE);
            registerListenerToSensor(mBarometer, BAROMETER);
            registerListenerToSensor(mCompass, COMPASS);
            mSensorDataWriters[ACCELEROMETER_IDX] = getSensorDataWriter(ACCELEROMETER);
            mSensorDataWriters[GYROSCOPE_IDX] = getSensorDataWriter(GYROSCOPE);
            mSensorDataWriters[BAROMETER_IDX] = getSensorDataWriter(BAROMETER);
            mSensorDataWriters[COMPASS_IDX] = getSensorDataWriter(COMPASS);
        } else if (mSensorSelected.equals(ACCELEROMETER)) {
            registerListenerToSensor(mAccelerometer, ACCELEROMETER);
            mSensorDataWriters[ACCELEROMETER_IDX] = getSensorDataWriter(mSensorSelected);
        } else if (mSensorSelected.equals(GYROSCOPE)) {
            registerListenerToSensor(mGyroscope, GYROSCOPE);
            mSensorDataWriters[GYROSCOPE_IDX] = getSensorDataWriter(mSensorSelected);
        } else if (mSensorSelected.equals(BAROMETER)) {
            registerListenerToSensor(mBarometer, BAROMETER);
            mSensorDataWriters[BAROMETER_IDX] = getSensorDataWriter(mSensorSelected);
        } else if (mSensorSelected.equals(COMPASS)) {
            registerListenerToSensor(mCompass, COMPASS);
            mSensorDataWriters[COMPASS_IDX] = getSensorDataWriter(mSensorSelected);
        }

    }

    private void registerListenerToSensor(Sensor sensor, String sensorName) {
        if (sensor != null) {
            // We can also specify sensor delay in microseconds as of API 11 (Android 3.0)
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            String errorMessage = sensorName + " not found on device.";
            Log.e(TAG, errorMessage);
            Toast.makeText(RecordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void stopAudioRecording() {

        try {
            if (mSensorSelected.equals(ALL_SENSORS)) {
                mLabel_2.setText("");
                unRegisterListenerFromSensor(mAccelerometer);
                unRegisterListenerFromSensor(mGyroscope);
                unRegisterListenerFromSensor(mBarometer);
                unRegisterListenerFromSensor(mCompass);
                for (int i = 0; i < NUM_SENSORS; i++) {
                    mSensorDataWriters[i].close();
                }
            } else if (mSensorSelected.equals(ACCELEROMETER)) {
                unRegisterListenerFromSensor(mAccelerometer);
                mSensorDataWriters[ACCELEROMETER_IDX].close();
            } else if (mSensorSelected.equals(GYROSCOPE)) {
                unRegisterListenerFromSensor(mGyroscope);
                mSensorDataWriters[GYROSCOPE_IDX].close();
            } else if (mSensorSelected.equals(BAROMETER)) {
                unRegisterListenerFromSensor(mBarometer);
                mSensorDataWriters[BAROMETER_IDX].close();
            } else if (mSensorSelected.equals(COMPASS)) {
                unRegisterListenerFromSensor(mCompass);
                mSensorDataWriters[COMPASS_IDX].close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not close Buffered writer: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Could not close Buffered writer: " + e.getMessage());
        }

        mLabelReading_1.setText("");
        mLabelReading_2.setText("");
        mLabelReading_3.setText("");
    }

    private void unRegisterListenerFromSensor(Sensor sensor) {
        if (sensor != null) {
            mSensorManager.unregisterListener(this);
            mInitialized = false;
        }
    }
}
