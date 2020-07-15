package com.example.audiobartest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private TextView textView;
    private TextView [] textAccel = new TextView[3];
    private TextView [] textGiro = new TextView[3];
    private Handler handler = new Handler();
    private Button  bStart;
    private Button  bSend;

    private SensorManager sensorManager;
    private List list;
    private List list2;

    private static final int RecordTime = 30; // Remember starts from 0

    private Thread t;

    private StringBuffer sbAccel    = new StringBuffer();
    private StringBuffer sbGiro     = new StringBuffer();

    ////////////////////////// For Audio Part
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    private static String pathName = null;
    private static String FilenoNamae = null;

    private RecordButton recordButton = null;
    private MediaRecorder recorder = null;

    private PlayButton   playButton = null;
    private MediaPlayer player = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textAccel [0] = (TextView)findViewById(R.id.sAccelX);
        textAccel [1] = (TextView)findViewById(R.id.sAccelY);
        textAccel [2] = (TextView)findViewById(R.id.sAccelZ);

        textGiro [0]  = (TextView)findViewById(R.id.sGiroX);
        textGiro [1]  = (TextView)findViewById(R.id.sGiroY);
        textGiro [2]  = (TextView)findViewById(R.id.sGiroZ);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(RecordTime);
        //progressBar.setMin(0);
        textView    = (TextView) findViewById(R.id.textView);
        bStart      = (Button) findViewById(R.id.bStart);

        bSend      = (Button) findViewById(R.id.bSend);
        bSend.setEnabled(false);


        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
        pathName = fileName;
        FilenoNamae = "audiorecordtest.mp4";

        Log.d("State","PATH = " + fileName);
        fileName += "/audiorecordtest.mp4";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        //sbAccel.append("Esto es de la Cadenda");
        //sbAccel.append("Esto mas");

        ///////////////////////////////////////////////////////////
        // To Make visible the buttons for Record and Play
        /*LinearLayout ll = new LinearLayout(this);
        recordButton = new RecordButton(this);
        ll.addView(recordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        playButton = new PlayButton(this);
        ll.addView(playButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        //setContentView(ll);
        addContentView(ll,new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));*/
        ///////////////////////////////////////////////////////////
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    class RecordButton extends androidx.appcompat.widget.AppCompatButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends androidx.appcompat.widget.AppCompatButton {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }


    //////////////////////////////////////////////////////////


    SensorEventListener sel = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        @SuppressLint("SetTextI18n")
        public void onSensorChanged(SensorEvent event) {


            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                float[] values = event.values;

                textAccel[0].setText(Float.toString(values[0]));
                textAccel[1].setText(Float.toString(values[1]));
                textAccel[2].setText(Float.toString(values[2]));

                sbAccel.append(values[0]+","+values[1]+","+values[2]+","+"\n");

            }

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

                float[] values = event.values;

                textGiro[0].setText(Float.toString(values[0]));
                textGiro[1].setText(Float.toString(values[1]));
                textGiro[2].setText(Float.toString(values[2]));

                sbGiro.append(values[0]+","+values[1]+","+values[2]+","+"\n");

            }

        }
    };



    /** Called when the user touches the button */
    public void sendMessage(View view) {
        Log.d("State","START Button pushed");

        // Sensor STUFF
        sensorManager   = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        list  = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        // TO DO this needs to be fixed... Without 2 list.. only using 1
        list2 = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        if(list.size()>0){
            sensorManager.registerListener(sel, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(sel, (Sensor) list2.get(0), SensorManager.SENSOR_DELAY_FASTEST);
        }else{
            Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
        }
        // End of Sensor STUFF

        bStart.setEnabled(false);

        // Simulate click to Record

        //recordButton.performClick();

        // Start long running operation in a background thread

        t = new Thread(new Runnable() {

            public void run() {

                // For the Record
                onRecord(true);

                ///////////////////////////////////////

                while (progressStatus < RecordTime) {
                    try {
                        // Sleep for 200 milliseconds.
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    progressStatus += 1;
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                            textView.setText(progressStatus + "\tSeconds");
                        }
                    });
                }

                sensorManager.unregisterListener(sel, (Sensor) list.get(0));
                sensorManager.unregisterListener(sel, (Sensor) list2.get(0));

                onRecord(false);
                //bSend.setEnabled(true);
                return;
            }
        });


        t.start();

        bSend.setEnabled(true);
         // Do something in response to button click
    }

    public void SendToEmail(View view){
        // Write all the values from the sensor
        // For the Files
        // To Write something

        File fileDir = new File(getExternalCacheDir().getAbsolutePath() + File.separator +"Data");
        if(!fileDir.exists()){
            try{
                fileDir.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File file = new File(getExternalCacheDir().getAbsolutePath() + File.separator +"Data"+File.separator+"Accel.txt");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if(file.exists()){
            try {
                FileWriter fileWriter  = new FileWriter(file);
                BufferedWriter bfWriter = new BufferedWriter(fileWriter);
                bfWriter.write(String.valueOf(sbAccel));
                bfWriter.write("\nText from THREAD");

                bfWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File file2 = new File(getExternalCacheDir().getAbsolutePath() + File.separator +"Data"+File.separator+"Giro.txt");
        if(!file2.exists()){
            try {
                file2.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if(file2.exists()){
            try {
                FileWriter fileWriter  = new FileWriter(file2);
                BufferedWriter bfWriter = new BufferedWriter(fileWriter);
                bfWriter.write(String.valueOf(sbGiro));
                bfWriter.append("\nText from THREAD");
                bfWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ShareViaEmail(pathName,FilenoNamae);

    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void ShareViaEmail(String folder_name, String file_name) {
        try {
            //File root= Environment.getExternalStorageDirectory();
            String filelocation     = folder_name + "/" + file_name;
            String filelocation2    = getExternalCacheDir().getAbsolutePath() + File.separator +"Data"+File.separator+"Accel.txt";
            String filelocation3    = getExternalCacheDir().getAbsolutePath() + File.separator +"Data"+File.separator+"Giro.txt";


            // Original
           /* Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            String message="File to be shared is " + file_name + ".";
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse( "file://"+filelocation2));
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse( "file://"+filelocation));
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setData(Uri.parse("mailto:daweek@icloud.com"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);*/

            String formattedDate = getDateTime();

            Intent emailIntent=new Intent(Intent.ACTION_SEND_MULTIPLE, Uri.parse("mailto:daweek@icloud.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Test from ANDROID: " + formattedDate);
            emailIntent.setData(Uri.parse("mailto:daweek@icloud.com"));
            emailIntent.setType("text/plain");
            Uri uri1 = Uri.parse("file://" +  filelocation);
            Uri uri2 = Uri.parse("file://" +  filelocation2);
            Uri uri3 = Uri.parse("file://" +  filelocation3);

            ArrayList<Uri> arrayList=new ArrayList<Uri>();
            arrayList.add(uri1);
            arrayList.add(uri2);
            arrayList.add(uri3);
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,arrayList);

            emailIntent.putExtra(Intent.EXTRA_TEXT,"Test #6283" );
            startActivity(Intent.createChooser(emailIntent,"Send Via..."));
            //emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(emailIntent);




        } catch(Exception e)  {
            System.out.println("is exception raises during sending mail"+e);
        }

        bSend.setEnabled(false);
    }

}