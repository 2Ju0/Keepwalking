package com.example.gait_health_prediction_androidphone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    public static Context context_main2;

    // Graph 그리기
    private LineGraphSeries<DataPoint> mSeriesAccelX, mSeriesAccelY, mSeriesAccelZ;
    private GraphView mGraphAccel;
    private double graphLastAccelXValue = 10d;
    private GraphView line_graph;
    TextView xValue, yValue, zValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        context_main2 = this;

        // ******
        mSeriesAccelX = initSeries(Color.BLUE, "X"); //라인 그래프를 그림
        mSeriesAccelY = initSeries(Color.RED, "Y");
        mSeriesAccelZ = initSeries(Color.GREEN, "Z");
        mGraphAccel = initGraph(R.id.graph, "X, Y, Z direction Acceleration");

        //그래프에 x,y,z 추가
        mGraphAccel.addSeries(mSeriesAccelX);
        mGraphAccel.addSeries(mSeriesAccelY);
        mGraphAccel.addSeries(mSeriesAccelZ);

        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);

        // 데이터 수신
        Intent intent = getIntent();
        ArrayList<Float> data = (ArrayList<Float>) intent.getSerializableExtra("data");

        // here
        float x, y, z;
        x = data.get(120);
        y = data.get(250);
        z = data.get(140);

        xValue.setText("xValue: " + x);
        yValue.setText("yValue: " + y);
        zValue.setText("zValue: " + z);

        graphLastAccelXValue += 0.05d;

        mSeriesAccelX.appendData(new DataPoint(graphLastAccelXValue,x),true,100);
        mSeriesAccelY.appendData(new DataPoint(graphLastAccelXValue,y),true,100);
        mSeriesAccelZ.appendData(new DataPoint(graphLastAccelXValue,z),true,100);
        Log.e("Log", String.valueOf(data));
    }

    // Normal, abnormal judgment
    public void judgement(float result1, float result2) {
        if (result1 >= result2) {
            ((MainActivity) MainActivity.context_main1).walkingTextView.setText("정상입니다🤓 \t" + result1);
        } else {
            ((MainActivity) MainActivity.context_main1).walkingTextView.setText("비정상입니다😂 \t" + result2);
        }
    }

    //**********************
    //그래프 초기화
    public GraphView initGraph(int id, String title) {
        GraphView graph = findViewById(id);
        //데이터가 늘어날때 x축 scroll이 생기도록
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(5);
        graph.getGridLabelRenderer().setLabelVerticalWidth(100);
        graph.setTitle(title);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        return graph;
    }

    // x,y,z 데이터 그래프 추가
    public LineGraphSeries<DataPoint> initSeries(int color, String title) {
        LineGraphSeries<DataPoint> series;
        series = new LineGraphSeries<>();
        series.setDrawDataPoints(true);
        series.setDrawBackground(true);
        series.setColor(color);
        series.setTitle(title);
        return series;
    }

    // *******************
}