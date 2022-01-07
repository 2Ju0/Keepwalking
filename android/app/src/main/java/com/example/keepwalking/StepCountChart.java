package com.example.keepwalking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.sleep;

public class StepCountChart extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    ArrayList<Integer> Fsteps = new ArrayList<>();
    ArrayList<String> days = new ArrayList<>();

    BarChart barChart;
    ArrayList<BarEntry> barEntries = new ArrayList<>();
    TextView date,KakaoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count_chart);

        barChart = findViewById(R.id.barchart);
        date = findViewById(R.id.date);
        KakaoName = findViewById(R.id.KakaoName);

        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dfDate2 = new SimpleDateFormat("MM월 dd일");
        String monday, sunday;

        // ***** 카카오 닉네임 *****
        KakaoName.setText(((GlobalApplication) getApplication()).getKakaoName()+" 님의");

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        days.add(dfDate.format(c.getTime()));
        monday = dfDate2.format(c.getTime());

        c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        days.add(dfDate.format(c.getTime()));

        c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        days.add(dfDate.format(c.getTime()));

        c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        days.add(dfDate.format(c.getTime()));

        c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        days.add(dfDate.format(c.getTime()));

        c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        days.add(dfDate.format(c.getTime()));

        c.add(Calendar.DATE, 1);
        days.add(dfDate.format(c.getTime()));
        sunday = dfDate2.format(c.getTime());

        date.setText(monday + " ~ " + sunday);

        readData(new FirebaseCallback() {
            @Override
            public void onCallback(ArrayList<Integer> list) {
                Log.e("리리스트: ", "" + list.toString());
//                barEntries.add(new BarEntry(0f, list.get(0)));
                barEntries.add(new BarEntry(list.size() - 1, (Integer) list.get(list.size() - 1)));

                BarDataSet barDataSet = new BarDataSet(barEntries, "");
                ArrayList<String> theDates = new ArrayList<>();
                theDates.add("월");
                theDates.add("화");
                theDates.add("수");
                theDates.add("목");
                theDates.add("금");
                theDates.add("토");
                theDates.add("일");
                barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(theDates));
                BarData theData = new BarData(barDataSet);//----Line of error
                barChart.setData(theData);

                // label font
                Typeface tf = Typeface.createFromAsset(getAssets(), "font/jalnan.ttf");
                YAxis leftAxis = barChart.getAxisLeft();
                YAxis yRAxis = barChart.getAxisRight();
                yRAxis.setDrawLabels(false);
                barDataSet.setValueTypeface(tf);

                barChart.getAxisRight();
                leftAxis.setTypeface(tf);

                XAxis xAxist = barChart.getXAxis();
                xAxist.setTypeface(tf);

                Legend l = barChart.getLegend();
                l.setTypeface(tf);

                // 라벨 제거
                barChart.getLegend().setEnabled(false);

                barChart.setScaleEnabled(true);
                barChart.setTouchEnabled(false);

                // 격자 없애기
                barChart.getAxisLeft().setDrawGridLines(false);
                barChart.getAxisRight().setDrawGridLines(false);
                barChart.getXAxis().setDrawGridLines(false);


                barDataSet.setValueFormatter(new MyValueFormatter());
                XAxis xAxis = barChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                barChart.getAxisLeft().setAxisMinimum(0);
                barChart.getAxisRight().setAxisMinimum(0);

                barChart.animateY(1000);

                // description 삭제
                barChart.getDescription().setEnabled(false);
                barDataSet.setValueTextSize(15f);
            }
        });

        /************* 하단바 *************/
        BottomNavigationView bottomNav = findViewById(R.id.bottom_menu2);

        // item selection part
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        final Intent intent = new Intent(StepCountChart.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        return true;

                    case R.id.calendar:
                        final Intent intent2 = new Intent(StepCountChart.this, CalendarActivity.class);
                        startActivity(intent2);

                        finish();
                        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                        return true;

                    case R.id.user:
                        final Intent intent3 = new Intent(StepCountChart.this, UserInfo.class);
                        startActivity(intent3);
                        finish();
                        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                        return true;
                }
                return false;
            }
        });
        /************* 하단바 *************/
    }

    private void readData(FirebaseCallback firebaseCallback) {
        for (int i = 0; i < 7; i++) {
            final int index;
            index = i;
            // 걸음수 Firebase 저장
            if(((GlobalApplication) getApplication()).getKakaoID() == null | ((GlobalApplication) getApplication()).getKakaoID() == "") { // 이메일
                databaseReference.child("EMAIL").child(((GlobalApplication) getApplication()).getBasicName()).child(((GlobalApplication) getApplication()).getBasicEmail()).child("STEPS").child(days.get(index)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

//                    snapshot.child(days.get(index));
                        if (snapshot.getValue() != null) {
                            Log.e("날짜:", "" + snapshot.getValue(Integer.class));
                            Fsteps.add(index, snapshot.getValue(Integer.class));
//                        Log.e("인덱스", "" + Fsteps[index]);
//                        barEntries.add(new BarEntry(index, Fsteps[index]));

                            Log.e("타입", "" + snapshot.getValue(Integer.class).getClass().getName());
                        } else {
//                        barEntries.add(new BarEntry(index, 0));
                            Fsteps.add(index, 0);
                        }
                        firebaseCallback.onCallback(Fsteps);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // 디비를 가져오던중 에러 발생 시
                        //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
                    }
                });
            }
            else{
                databaseReference.child("KAKAOID").child(((GlobalApplication) getApplication()).getKakaoID()).child("STEPS").child(days.get(index)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

//                    snapshot.child(days.get(index));
                        if (snapshot.getValue() != null) {
                            Log.e("날짜:", "" + snapshot.getValue(Integer.class));
                            Fsteps.add(index, snapshot.getValue(Integer.class));
//                        Log.e("인덱스", "" + Fsteps[index]);
//                        barEntries.add(new BarEntry(index, Fsteps[index]));

                            Log.e("타입", "" + snapshot.getValue(Integer.class).getClass().getName());
                        } else {
//                        barEntries.add(new BarEntry(index, 0));
                            Fsteps.add(index, 0);
                        }
                        firebaseCallback.onCallback(Fsteps);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // 디비를 가져오던중 에러 발생 시
                        //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
                    }
                });
            }

        }
    }

    private interface FirebaseCallback {
        void onCallback(ArrayList<Integer> list);
    }

    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value);
        }
    }
}