package com.example.keepwalking;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

public class MainActivity2 extends AppCompatActivity {

    public static Context context_main2;
    private LineChart chart;

    // Graph 그리기
    private LineGraphSeries<DataPoint> mSeriesAccelX, mSeriesAccelY, mSeriesAccelZ;
    private GraphView mGraphAccel;
    private double graphLastAccelXValue = 10d;
    private GraphView line_graph;
    TextView xValue, yValue, zValue;
    private TextView walkingTextView;

    private TextToSpeech tts;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        walkingTextView = findViewById(R.id.tv_output);
        btn = findViewById(R.id.button3);

        context_main2 = this;


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

        String result = intent.getStringExtra("result");
        Log.e("정상/비정상 결과:", result);
        walkingTextView.setText(result);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    tts.setLanguage(Locale.KOREA);
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.setPitch(0.5f);         // 음성 톤을 0.5배 내려준다.
                tts.setSpeechRate(1.0f);    // 읽는 속도는 기본 설정
                // editText에 있는 문장을 읽는다.
                tts.speak(result, TextToSpeech.QUEUE_FLUSH, null);
            }
        });



        // 음성 텍스트
//        tts.speak(result,TextToSpeech.QUEUE_FLUSH, null);
//        Log.e("LOG", accX.size() + "," + accY.size() + "," + accZ.size());

        // here

        chart = findViewById(R.id.chart);
        ArrayList<Entry> entry1 = new ArrayList<>();
        ArrayList<Entry> entry2 = new ArrayList<>();

        for (int i = 0; i < accX.size(); i++) {
            float res = (float) Math.sqrt(Math.pow(accX.get(i), 2) + Math.pow(accY.get(i), 2) + Math.pow(accZ.get(i), 2));

            entry1.add(new Entry(i, res));
            entry2.add(new Entry(i, res + 10));
        }

        LineDataSet set1, set2;
        set1 = new LineDataSet(entry1, "사용자");
        set2 = new LineDataSet(entry2, "정상인");

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        dataSets.add(set2);

        LineData dat = new LineData(dataSets);

        // ******그래프 디자인*********
        // 사용자 측정 Graph
        set1.setColor(Color.rgb(153, 204, 255));
        set1.setDrawCircles(false);
//        set1.setCircleColor(Color.rgb(153, 204, 255));
//        set1.setCircleRadius(3f);
        set1.setLineWidth(2);
        set1.setDrawFilled(true); // 차트 아래 fill(채우기) 설정
        set1.setFillColor(Color.rgb(212, 248, 253));
        set1.setValueTextSize(10f);

        // 정상 Graph
        set2.setColor(Color.rgb(255, 51, 153));
        set2.setCircleColor(Color.rgb(255, 51, 153));
        set2.setCircleRadius(3f);
        set2.setLineWidth(2);
        set2.setValueTextSize(10f);

        // y축 오른쪽 Label remove
        YAxis yAxisRight = chart.getAxisRight(); //Y축의 오른쪽면 설정
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);
        //*************************

        chart.getDescription().setEnabled(false); // 하단 regend remove
        chart.setData(dat);


//        Log.e("Log", String.valueOf(data));
    }


//    // Normal, abnormal judgment
//    public void judgement(float result1, float result2) {
//        TextView walkingTextView = findViewById(R.id.tv_output);
//        if (result1 >= result2) {
//            walkingTextView.setText("정상입니다🤓 \t" + result1);
//        } else {
//            walkingTextView.setText("비정상입니다😂 \t" + result2);
//        }
//    }
}