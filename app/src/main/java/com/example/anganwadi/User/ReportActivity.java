package com.example.anganwadi.User;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.example.anganwadi.R;
import com.example.anganwadi.BaseActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.example.anganwadi.Pojo.StudentData;

import java.util.ArrayList;

public class ReportActivity extends BaseActivity {

    private BarChart chart;
    private ArrayList<StudentData> studentList = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_report);
        setTitle(getString(R.string.reports));

        progressBar = findViewById(R.id.progressBar);
        chart = findViewById(R.id.chart);

        setupChart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchData();
    }

    private void setupChart() {
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.setDragEnabled(true);
        chart.setFitBars(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);
    }

    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);
        // Firebase data fetching removed.
        populateChart();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void populateChart() {
        if (studentList.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            chart.clear();
            chart.invalidate();
            return;
        }

        final ArrayList<BarEntry> entries = new ArrayList<>();
        final ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < studentList.size(); i++) {
            StudentData student = studentList.get(i);
            entries.add(new BarEntry(i, 0)); // Placeholder value
            labels.add(student.getName());
        }
        displayChart(entries, labels);
    }

    private void displayChart(ArrayList<BarEntry> entries, ArrayList<String> labels) {
        if (entries.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            chart.clear();
            chart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.food_items));
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        
        chart.invalidate(); // Refresh the chart
        progressBar.setVisibility(View.GONE);
    }

    private boolean isAdminLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isAdminLoggedIn", false);
    }
}
