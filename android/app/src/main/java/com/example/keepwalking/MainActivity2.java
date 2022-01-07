package com.example.keepwalking;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class MainActivity2 extends AppCompatActivity {

    public static Context context_main2;
    private LineChart chart;

    // Graph 그리기
    private LineGraphSeries<DataPoint> mSeriesAccelX, mSeriesAccelY, mSeriesAccelZ;
    private GraphView mGraphAccel;
    private double graphLastAccelXValue = 10d;
    private GraphView line_graph;

    // TextView
    private TextView walkingTextView;
    private TextView KakaoName;

    // 음성
    private TextToSpeech tts;

    // 그래프 저장
    private String[] permissionList = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private FirebaseStorage storage;
    private LineChart chartView;
    private Button btUpload, btDownload;

    private String result2;

    InputStream filePath1;
    InputStream filePath2;
    InputStream filePath3;

    ArrayList<Double> dataX;
    ArrayList<Double> dataY;
    ArrayList<Double> dataZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        walkingTextView = findViewById(R.id.tv_output);
        KakaoName = findViewById(R.id.KakaoName);

        context_main2 = this;
        storage = FirebaseStorage.getInstance();
        chartView = findViewById(R.id.chart);

        filePath1 = getResources().openRawResource(R.raw.datax);
        filePath2 = getResources().openRawResource(R.raw.datay);
        filePath3 = getResources().openRawResource(R.raw.dataz);

        dataX = new ArrayList<>();
        dataY = new ArrayList<>();
        dataZ = new ArrayList<>();

        ReadTextFile(filePath1, dataX);
        ReadTextFile(filePath2, dataY);
        ReadTextFile(filePath3, dataZ);

        // 데이터 수신
        Intent intent = getIntent();
//        ArrayList<Float> data = (ArrayList<Float>) intent.getSerializableExtra("data");

        ArrayList<Float> accX = (ArrayList<Float>) intent.getSerializableExtra("accX");
        ArrayList<Float> accY = (ArrayList<Float>) intent.getSerializableExtra("accY");
        ArrayList<Float> accZ = (ArrayList<Float>) intent.getSerializableExtra("accZ");

        ArrayList<Float> gyroX = (ArrayList<Float>) intent.getSerializableExtra("gyroX");
        ArrayList<Float> gyroY = (ArrayList<Float>) intent.getSerializableExtra("gyroY");
        ArrayList<Float> gyroZ = (ArrayList<Float>) intent.getSerializableExtra("gyroZ");

        ArrayList<Float> lx = (ArrayList<Float>) intent.getSerializableExtra("lx");
        ArrayList<Float> ly = (ArrayList<Float>) intent.getSerializableExtra("ly");
        ArrayList<Float> lz = (ArrayList<Float>) intent.getSerializableExtra("lz");

        ArrayList<Float> n_accX = new ArrayList<>();
        String result = intent.getStringExtra("result");
        result2 = intent.getStringExtra("result2");
        Log.e("정상/비정상 결과:", result);
        walkingTextView.setText(result);

        chart = findViewById(R.id.chart);
        ArrayList<Entry> entry1 = new ArrayList<>();
        ArrayList<Entry> entry2 = new ArrayList<>();


        for (int i = 200; i < 400; i++) {
            if(i == 200){
                tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            tts.setLanguage(Locale.KOREA);
                            tts.setPitch(1f);         // 음성 톤을 0.5배 내려준다.
                            tts.setSpeechRate(0.8f);    // 읽는 속도는 기본 설정
                            // editText에 있는 문장을 읽는다.
                            tts.speak(result, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                });
            }

            float res = (float) Math.sqrt(Math.pow(accX.get(i), 2) + Math.pow(accY.get(i), 2) + Math.pow(accZ.get(i), 2));
            double a = dataX.get(i);
            double b = dataY.get(i);
            double c = dataZ.get(i);
            double res2 = (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2) + Math.pow(c, 2));
//            float res2 = (double)Math
            entry1.add(new Entry(i, res));
            entry2.add(new Entry(i, (float) res2));
        }
        // ***** 카카오 닉네임 *****
        KakaoName.setText(((GlobalApplication) getApplication()).getKakaoName()+" 님의");

        // ******그래프*********
        LineDataSet set1, set2;
        set1 = new LineDataSet(entry1, "사용자");
        set2 = new LineDataSet(entry2, "정상인");

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        dataSets.add(set2);

        LineData dat = new LineData(dataSets);

        // 격자 없애기
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);

        // 사용자 측정 Graph
        set1.setColor(Color.rgb(153, 204, 255));
        set1.setDrawCircles(false);
        set1.setLineWidth(2);
        set1.setDrawFilled(true); // 차트 아래 fill(채우기) 설정
        set1.setFillColor(Color.rgb(212, 248, 253));
        set1.setValueTextSize(10f);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // 정상 Graph
        set2.setColor(Color.rgb(255, 51, 153));
        set2.setCircleColor(Color.rgb(255, 51, 153));
        set2.setCircleRadius(3f);
        set2.setLineWidth(2);
        set2.setValueTextSize(10f);
        set2.setDrawCircles(false);
        set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // y축 오른쪽 Label remove
        YAxis yAxisRight = chart.getAxisRight(); //Y축의 오른쪽면 설정
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);
        //*************************

        // label font
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "font/jalnan.ttf");
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTypeface(tf);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTypeface(tf);

        Legend l = chart.getLegend();
        l.setTypeface(tf);

        chart.getDescription().setEnabled(false); // 하단 regend remove
        chart.setData(dat);

        /************* 하단바 *************/
        BottomNavigationView bottomNav = findViewById(R.id.bottom_menu2);

        // item selection part
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    final Intent intent1 = new Intent(MainActivity2.this, MainActivity.class);
                    startActivity(intent1);

                    finish();
                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                    return true;

                case R.id.calendar:
                    final Intent intent2 = new Intent(MainActivity2.this, CalendarActivity.class);
                    startActivity(intent2);

                    finish();
                    overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                    return true;

                case R.id.step:
                    final Intent intent3 = new Intent(MainActivity2.this, StepCountChart.class);
                    startActivity(intent3);
                    finish();
                    overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                    return true;

            }
            return false;
        });
        /************* 하단바 *************/
        Toast.makeText(this.getApplicationContext(), "측정 기록이 자동 저장되었습니다.", Toast.LENGTH_SHORT).show();

    }

    public void onPause(){
        super.onPause();
        upLoadFromMemory();
    }

    //경로의 텍스트 파일읽기
    public void ReadTextFile(InputStream filePath, ArrayList<Double> arr) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(filePath, "EUC_KR"));
            while (true) {
                String str = bufferedReader.readLine();
                if (str != null) {
                    arr.add(Double.valueOf(str));
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // 메모리 데이터, 비트맵을 바이트코드로 compress 하여 추가하기
    //  Get the data from an ImageView as bytes
    private void upLoadFromMemory() {
        String kakaoid = ((GlobalApplication) getApplication()).getKakaoID();

        chartView.setDrawingCacheEnabled(true);
        Bitmap bitmap = chartView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH시mm분");
        Date time = new Date();
        String current_time = sdf.format(time);

        String[] file_name = current_time.split("_");

        StorageReference mountainsRef = storage.getReference().child(kakaoid + "/" + file_name[0] + "/" + file_name[1] + "_" + result2);
        UploadTask uploadTask = mountainsRef.putBytes(data);

        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            Log.d(TAG, "Upload is " + progress + "% done");

        }).addOnPausedListener(taskSnapshot -> Log.d(TAG, "Upload is paused")).addOnFailureListener(exception -> {
//            Toast.makeText(this.getApplicationContext(), "그래프가 정상적으로 저장되지 않았습니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "업로드 실패");

        }).addOnSuccessListener(taskSnapshot -> {
//            Toast.makeText(this.getApplicationContext(), "측정 기록이 자동 저장되었습니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "업로드 성공");
        });
    }
}
