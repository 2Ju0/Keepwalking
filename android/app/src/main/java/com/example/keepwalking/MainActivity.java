package com.example.keepwalking;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
//import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.SocialObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.kakao.util.helper.Utility.getKeyHash;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    public static Context context_main1;

    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager;

    //Using the Gyroscope
    private SensorEventListener mGyroLis;

    //    private Sensor mGgyroSensor = null;
    private Sensor mGgyroSensor, mAccelometerSensor, mLinearAcceleration, sensor_step_counter;

    //Using the Accelometer
    private SensorEventListener mAccLis;
    private SensorEventListener mLinLis;

    //Roll and Pitch
    private double pitch;
    private double roll;
    private double yaw;

    //timestamp and dt
    private double timestamp;
    private double dt;

    // for radian -> dgree
    private double RAD2DGR = 180 / Math.PI;
    private static final float NS2S = 1.0f / 1000000000.0f;

    //*************************
    private float[] results;
    private ActivityClassifier classifier;

    private static final int TIME_STAMP = 100;
    private static final String TAG = "MainActivity";
    private static List<Float> accX, accY, accZ;
    private static List<Float> gyroX, gyroY, gyroZ;
    private static List<Float> lx, ly, lz;

    public TextView walkingTextView;
    private TextView step_sensor;
    private int mSteps;
    private int mCounterSteps = 0;
    //리스너가 등록되고 난 후의 step count
    private ImageView iv;

    private AnimationDrawable drawable;
    //*************************

    // 권한
    String[] permission_list = {
            Manifest.permission.INTERNET,
//            Manifest.permission.GET_ACCOUNTS
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    private long timeCountInMilliSeconds = 600000; // 60000 : 1분

    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;

    //    private ProgressBar progressBarCircle;
    private EditText editTextMinute;
    private TextView textViewTime;
    private ImageView imageViewReset;
    private ImageView imageViewStartStop;
    private CountDownTimer countDownTimer;
    public ImageView kakaoLinkBtn;
    private ImageView frontwalking;
    private ConstraintLayout backImage;

    // 속도
    //****************
    private LocationManager lm;
    private LocationListener ll;
    double mySpeed, maxSpeed;
    private TextView wspeed;
    //****************

    String kakaoid;

    // Firebase
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    // 날짜
    Date c;
    SimpleDateFormat df;
    String formattedDate;

    // 음성
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        // 시간별 배경이미지 선택
        long now = System.currentTimeMillis();
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH");
        int getHour = Integer.parseInt(dateFormat2.format(now));


        // 계정확인
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.GET_ACCOUNTS}, 1);

        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccounts();
        System.out.println(accounts.length);

        for (Account ac : accounts) {
            String acname = ac.name;
            String actype = ac.type;
            // Take your time to look at all available accounts
            System.out.println("Accounts : " + acname + ", " + actype);
            Log.e("Tag", "계정:" + acname);
        }
        // Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Using the Gyroscope
        mGgyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        mGyroLis = new GyroscopeListener();

        // Using the Accelometer
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mAccLis = new AccelometerListener();

        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // Step count
        sensor_step_counter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (sensor_step_counter == null) {
            Log.e("걸음수 센서", "No Step Detect Sensor");
        } else {
            Log.e("걸음수 센서", "있음!!!!!!!!!");
        }

        // method call to initialize the views
        initViews();
        // method call to initialize the listeners
        initListeners();

        //********************

        accX = new ArrayList<>();
        accY = new ArrayList<>();
        accZ = new ArrayList<>();
        gyroX = new ArrayList<>();
        gyroY = new ArrayList<>();
        gyroZ = new ArrayList<>();

        lx = new ArrayList<>();
        ly = new ArrayList<>();
        lz = new ArrayList<>();

        Log.e("getKeyHash", "" + getKeyHash(this));

        classifier = new ActivityClassifier(getApplicationContext());
        context_main1 = this;

        // 누적 총거리
        //***************
//        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
////        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        // GPS 사용 가능 여부 확인
//        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        tvGpsEnable.setText("GPS Enable: " + isGPSEnable);  //GPS Enable
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
        //***************

        /************* 속력 *************/
        maxSpeed = mySpeed = 0;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ll = new SpeedoActionListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        if (ll == null) {
            Log.e("속력센서 없음", "없음");
        } else {
            Log.e("속력센서 ", "있음");
        }
        wspeed = findViewById(R.id.speed_t);
        /************* 속력 *************/

        /************* 하단바 *************/
        BottomNavigationView bottomNav = findViewById(R.id.bottom_menu);

        // item selection part
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.calendar:
                    final Intent intent1 = new Intent(MainActivity.this, CalendarActivity.class);
                    startActivity(intent1);
                    finish();
                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                    return true;

                case R.id.step:
                    final Intent intent2 = new Intent(MainActivity.this, StepCountChart.class);
                    startActivity(intent2);
                    finish();
                    overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                    return true;

                case R.id.user:
                    final Intent intent3 = new Intent(MainActivity.this, UserInfo.class);
                    startActivity(intent3);
                    finish();
                    overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                    return true;

            }
            return false;
        });
        /************* 하단바 *************/

        // 걸음수 불러오기 & 저장
        Log.e("메인 걸음수:", "" + ((GlobalApplication) getApplication()).getSteps());
        mSteps = ((GlobalApplication) getApplication()).getSteps();
        step_sensor.setText("" + mSteps);

        // 음성
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.KOREA);
                }
            }
        });

        Log.e("오늘의 시간:",""+getHour);
        // 배경화면 바꾸기
        if (5 <= getHour && getHour <= 8) {
            backImage.setBackgroundResource(R.drawable.bg_001);
        } else if (9 <= getHour && getHour <= 12) {
            backImage.setBackgroundResource(R.drawable.bg_002);
        } else if (13 <= getHour && getHour <= 15) {
            backImage.setBackgroundResource(R.drawable.bg_003);
        } else if (16 <= getHour && getHour <= 18) {
            backImage.setBackgroundResource(R.drawable.bg_004);
        } else if (19 <= getHour && getHour <= 23 || getHour == 0) {
            backImage.setBackgroundResource(R.drawable.bg_005);
            textViewTime.setTextColor(Color.parseColor("#FFFF99"));
        } else if (1 <= getHour && getHour <= 4) {
            backImage.setBackgroundResource(R.drawable.bg_006);
            textViewTime.setTextColor(Color.parseColor("#FFFF99"));
        }
    }

    private class SpeedoActionListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                mySpeed = Double.parseDouble(String.format("%.2f", location.getSpeed()));
                if (mySpeed > maxSpeed) {
                    maxSpeed = mySpeed;
                }
                wspeed.setText(maxSpeed + " km/h");
                Log.e("속력:소수점변경", "" + mySpeed);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    }

    // 계정 권한 허용
    public void checkPermission() {
        //현재 안드로이드 버전이 6.0미만이면 메서드를 종료한다.
        //안드로이드6.0 (마시멜로) 이후 버전부터 유저 권한설정 필요
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        for (String permission : permission_list) {
            //권한 허용 여부를 확인한다.
            int chk = checkCallingOrSelfPermission(permission);
            if (chk == PackageManager.PERMISSION_DENIED) {
                //권한 허용을여부를 확인하는 창을 띄운다
                requestPermissions(permission_list, 0);
            }
        }
    }

    // 누적 총거리 계산
//    public double getGPSLocation() {
//        double deltaTime = 0.0;
//        double deltaDist = 0.0;
//        //GPS Start
//        if(isGPSEnable) {
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return 0.0;
//            }
////            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if(lastKnownLocation == null ) {
//                lastKnownLocation = nowLastlocation;
//            }
//
//            if (lastKnownLocation != null && nowLastlocation != null) {
//                double lat1 = lastKnownLocation.getLatitude();
//                double lng1 = lastKnownLocation.getLongitude();
//                double lat2 = nowLastlocation.getLatitude();
//                double lng2 = nowLastlocation.getLongitude();
//
//                deltaTime = (nowLastlocation.getTime() - lastKnownLocation.getTime()) / 1000.0;  //시간 간격
//
//
//                //double distanceMeter = distance(37.52135327,  126.93035147,  37.52135057,  126.93036593);
//                deltaDist = distance(lat1,  lng1,  lat2,  lng2);
//                if(deltaDist > 0.05) {
//                    tvGpsLatitude.setText("Start Latitude : " + lat1);
//                    tvGpsLongitude.setText("Start Longitude : " + lng1);
//                    tvEndLatitude.setText("End Latitude : " + lat2);
//                    tvEndLongitude.setText("End Longitude : " + lng2);
//                    tvDistDif.setText("거리 간격 : " +  Double.parseDouble(String.format("%.3f",deltaDist)) + " m");  // Dist Difference
//                    lastKnownLocation = nowLastlocation;
//                    return deltaDist;
//                }
////                lastKnownLocation = nowLastlocation;
//            }
//        }
//        return 0.0;
//    }

    private void initViews() {
        textViewTime = findViewById(R.id.textViewTime);
        textViewTime.setTypeface(null, Typeface.BOLD);
        imageViewReset = findViewById(R.id.retryButton);
        imageViewStartStop = findViewById(R.id.imageViewStartStop);
        kakaoLinkBtn = findViewById(R.id.imageViewShare);
        walkingTextView = findViewById(R.id.tv_output);
        frontwalking = findViewById(R.id.imageView3);
        step_sensor = findViewById(R.id.step_sensor);
        iv = findViewById(R.id.imageView3);
        backImage = (ConstraintLayout)findViewById(R.id.activity_main);
    }

    /**
     * method to initialize the click listeners
     */
    private void initListeners() {
        imageViewReset.setOnClickListener(this);
        imageViewStartStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.retryButton:
                reset();
                break;
            case R.id.imageViewStartStop:
                startStop();
                break;
        }
    }

    /**
     * method to reset count down timer
     */
    private void reset() {
        stopCountDownTimer();
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 걸음수 센서
        mSensorManager.registerListener(this, sensor_step_counter, SensorManager.SENSOR_DELAY_NORMAL);
        mSteps = ((GlobalApplication) getApplication()).getSteps();
        Log.e("메인 걸음수:", "" + mSteps);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("LOG", "onPause()");
        mSensorManager.unregisterListener(mGyroLis);
        mSensorManager.unregisterListener(mAccLis);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("LOG", "onDestroy()");
        mSensorManager.unregisterListener(mGyroLis);
        mSensorManager.unregisterListener(mAccLis);

        c = Calendar.getInstance().getTime();
        df = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = df.format(c);

        // 걸음수 Firebase 저장
        if(((GlobalApplication) getApplication()).getKakaoID() == null | ((GlobalApplication) getApplication()).getKakaoID() == ""){ // 이메일
            databaseReference.child("EMAIL").child(((GlobalApplication) getApplication()).getBasicName()).child(((GlobalApplication) getApplication()).getBasicEmail()).child("STEPS").child(formattedDate).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    databaseReference.child("EMAIL").child(((GlobalApplication) getApplication()).getBasicName()).child(((GlobalApplication) getApplication()).getBasicEmail()).child("STEPS").child(formattedDate).setValue(mSteps);

                    Log.e("걸음수DB: ", "" + ((GlobalApplication) getApplication()).getSteps());
                    // addGroup(Gname_edit.getText().toString(),Gintro_edit.getText().toString(),Gcate_tv.getText().toString(), goaltime, gmp);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 디비를 가져오던중 에러 발생 시
                    //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
                }
            });
        }
        else{
            databaseReference.child("KAKAOID").child(((GlobalApplication) getApplication()).getKakaoID()).child("STEPS").child(formattedDate).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    databaseReference.child("KAKAOID").child(((GlobalApplication) getApplication()).getKakaoID()).child("STEPS").child(formattedDate).setValue(mSteps);

                    Log.e("걸음수DB: ", "" + ((GlobalApplication) getApplication()).getSteps());
                    // addGroup(Gname_edit.getText().toString(),Gintro_edit.getText().toString(),Gcate_tv.getText().toString(), goaltime, gmp);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 디비를 가져오던중 에러 발생 시
                    //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
                }
            });
        }
    }

    /**
     * method to start and stop count down timer
     */
    private void startStop() {
        if (timerStatus == TimerStatus.STOPPED) {
            // 다시측정 버튼 보이기
            imageViewReset.setVisibility(View.VISIBLE);
//            app:layout_constraintHorizontal_bias="0.081"
            imageViewStartStop.setX(10);
            // changing play icon to stop icon
            imageViewStartStop.setImageResource(R.drawable.stop_btn);
            // changing the timer status to started
            timerStatus = TimerStatus.STARTED;
            // call to start the count down timer
            startCountDownTimer();

            mSensorManager.registerListener(this, mGgyroSensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, mAccelometerSensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_GAME);
//            mSensorManager.registerListener(this, sensor_step_counter, SensorManager.SENSOR_DELAY_GAME);
        } else {
//            textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
            // call to initialize the progress bar values
//                setProgressBarValues();
            // hiding the reset icon
//            imageViewReset.setVisibility(View.GONE);
            // changing stop icon to start icon
//            imageViewStartStop.setImageResource(R.drawable.retry_btn);
            // changing the timer status to stopped
            timerStatus = TimerStatus.STOPPED;
            predictActivity(); // 모델 학습
            mSensorManager.unregisterListener(MainActivity.this);
            stopCountDownTimer();

        }
    }

    /**
     * method to start count down timer
     */
    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
//                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
                // call to initialize the progress bar values
//                setProgressBarValues();
                // hiding the reset icon
//                imageViewReset.setVisibility(View.GONE);
//                // changing stop icon to start icon
//                imageViewStartStop.setImageResource(R.drawable.start_btn);
                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED;

                predictActivity(); // 모델 학습
                mSensorManager.unregisterListener(MainActivity.this);
            }

        }.start();
    }

    /**
     * method to stop count down timer
     */
    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }

    /**
     * method to convert millisecond to time format
     *
     * @param milliSeconds
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accX.add(event.values[0]);
            accY.add(event.values[1]);
            accZ.add(event.values[2]);

//            Log.e("LOG", "ACCELOMETER           [X]:" + String.format("%.4f", event.values[0])
//                    + "           [Y]:" + String.format("%.4f", event.values[1])
//                    + "           [Z]:" + String.format("%.4f", event.values[2]));

        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroX.add(event.values[0]);
            gyroY.add(event.values[1]);
            gyroZ.add(event.values[2]);

//            Log.e("LOG", "GYROSCOPE           [X]:" + String.format("%.4f", event.values[0])
//                    + "           [Y]:" + String.format("%.4f", event.values[1])
//                    + "           [Z]:" + String.format("%.4f", event.values[2])
//                    + "           [Pitch]: " + String.format("%.1f", pitch * RAD2DGR)
//                    + "           [Roll]: " + String.format("%.1f", roll * RAD2DGR)
//                    + "           [Yaw]: " + String.format("%.1f", yaw * RAD2DGR)
//                    + "           [dt]: " + String.format("%.4f", dt));

        } else if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            lx.add(event.values[0]);
            ly.add(event.values[1]);
            lz.add(event.values[2]);

        }
        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
//            step_sensor.setText("걸음수: " + event.values[0]);
            step_sensor.setText("" + (++mSteps));
            Log.e("걸음수: ", "" + mSteps);
            ((GlobalApplication) getApplication()).setSteps(mSteps);

            Log.e("Log", "acc크기: " + accX.size());
            Log.e("Log", "gyro크기: " + gyroX.size());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void predictActivity() {
        tts.setPitch(1f);
        tts.setSpeechRate(0.8f);
        tts.speak("측정이 완료되었습니다", TextToSpeech.QUEUE_FLUSH, null);

        List<Float> data = new ArrayList<>();
        if (accX.size() >= TIME_STAMP && accY.size() >= TIME_STAMP && accZ.size() >= TIME_STAMP
                && gyroX.size() >= TIME_STAMP && gyroY.size() >= TIME_STAMP && gyroZ.size() >= TIME_STAMP
                && lx.size() >= TIME_STAMP && ly.size() >= TIME_STAMP && lz.size() >= TIME_STAMP
        ) {
            Log.e("들어왔니", "어");
            data.addAll(accX.subList(0, TIME_STAMP));
            data.addAll(accY.subList(0, TIME_STAMP));
            data.addAll(accZ.subList(0, TIME_STAMP));

            data.addAll(gyroX.subList(0, TIME_STAMP));
            data.addAll(gyroY.subList(0, TIME_STAMP));
            data.addAll(gyroZ.subList(0, TIME_STAMP));

            data.addAll(lx.subList(0, TIME_STAMP));
            data.addAll(ly.subList(0, TIME_STAMP));
            data.addAll(lz.subList(0, TIME_STAMP));

            results = classifier.predictProbabilities(toFloatArray(data));
            Log.e("Log", "predictActivity: " + Arrays.toString(results));

            // MainActivity2로 전환
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);

            intent.putExtra("KAKAOID", kakaoid);

            intent.putExtra("accX", (Serializable) accX);
            intent.putExtra("accY", (Serializable) accY);
            intent.putExtra("accZ", (Serializable) accZ);

            intent.putExtra("gyroX", (Serializable) gyroX);
            intent.putExtra("gyroY", (Serializable) gyroY);
            intent.putExtra("gyroZ", (Serializable) gyroZ);

            intent.putExtra("lx", (Serializable) lx);
            intent.putExtra("ly", (Serializable) ly);
            intent.putExtra("lz", (Serializable) lz);

            String result = judgement(results[0]);
            intent.putExtra("result", result);

            String result2 = judgement2(results[0]);
            intent.putExtra("result2", result2);

            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
            // MainActivity2 judgement() 호출

            data.clear();
            accX.clear();
            accY.clear();
            accZ.clear();

            gyroX.clear();
            gyroY.clear();
            gyroZ.clear();

            lx.clear();
            ly.clear();
            lz.clear();
        }

    }

    // Normal, abnormal judgment
    private String judgement(float result1) {
        if (result1 > 0.5) {
            return "양호합니다\t";
        } else {
            return "개선이 필요합니다\t";
        }
    }

    private String judgement2(float result1) {
        if (result1 > 0.5) {
            return "양호";
        } else {
            return "개선필요";
        }
    }

    private float[] toFloatArray(List<Float> data) {
        int i = 0;
        float[] array = new float[data.size()];
        for (Float f : data) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }

}