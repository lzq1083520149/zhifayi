package com.hkcect.z12.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.hkcect.z12.R;
import com.hkcect.z12.utils.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class SpaceManagementActivity extends BaseActivity implements OnChartValueSelectedListener {

    private static final String TAG = "SpaceManagementActivity";
    private PieChart mChart;

    protected String[] mParties;

    long freeSpace;
    long allSpace;
    long photoLength;
    long videoLength;
    long remaining;

    private TextView all_space;
    private TextView tv_local_patch;
    private EditText et_space;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_management);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initData();

        initView();
    }

    private void initData() {
        mParties = new String[]{
                getString(R.string.part_a), getString(R.string.part_b),
                getString(R.string.part_c), getString(R.string.part_d)
        };


        File file = new File(StringUtils.local_media_path);


        if (file.exists() && file.isDirectory()) {

            freeSpace = file.getFreeSpace();
            allSpace = file.getTotalSpace();
            Log.e(TAG, "initData: allSpace "+allSpace );
            String[] photo = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".JPG");
                }
            });

            String[] video = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".MOV");
                }
            });
            photoLength = 0L;
            if (photo != null && photo.length > 0) {
                for (String aPhoto : photo) {
                    photoLength = photoLength + new File(StringUtils.local_media_path + aPhoto).length();
                    Log.e(TAG, "initData: photoLength "+photoLength );
                }
            }

            for (String aVideo : video) {
                videoLength = videoLength + new File(StringUtils.local_media_path + aVideo).length();
                Log.e(TAG, "initData: videoLength "+videoLength );
            }

        }else {
            photoLength = 0L;
            videoLength = 0L;
            freeSpace = new File(StringUtils.root_path).getFreeSpace();
            allSpace = new File(StringUtils.root_path).getTotalSpace();
        }
            remaining = allSpace - freeSpace - photoLength - videoLength;

        Log.e(TAG, "initData: remaining "+remaining );
    }

    private void initView() {

        all_space = (TextView) findViewById(R.id.all_space);
        all_space.setText(StringUtils.byte2FitMemorySize(allSpace));

        tv_local_patch = (TextView) findViewById(R.id.tv_local_patch);
        tv_local_patch.setText(StringUtils.local_media_path);

        et_space = (EditText) findViewById(R.id.et_space);
        et_space.setText(getSharedPreferences(StringUtils.setting, Context.MODE_PRIVATE).getInt(StringUtils.space, 300) + "");

        mChart = (PieChart) findViewById(R.id.chart1);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        mChart.setDrawHoleEnabled(false);

        mChart.setTransparentCircleColor(Color.BLACK);
        mChart.setTransparentCircleAlpha(50);


        mChart.setEntryLabelColor(Color.BLACK);
//        mChart.setHoleRadius(30f);
//        mChart.setTransparentCircleRadius(33f);
        mChart.setDrawCenterText(false);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        setData();

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setFormToTextSpace(10f);
        l.setXEntrySpace(20f);//设置图例实体之间延X轴的间距（setOrientation = HORIZONTAL有效）
        l.setYEntrySpace(10f);//设置图例实体之间延Y轴的间距（setOrientation = VERTICAL 有效）
        l.setDrawInside(false);
        l.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_config: //保存剩余空间大小

                String space = et_space.getText().toString();

                if (!StringUtils.isNumber(space)) {
                    Toast.makeText(this, getResources().getString(R.string.please_enter_the_numbers), Toast.LENGTH_SHORT).show();
                    return true;
                }
                int s = Integer.valueOf(space);
                if (s * 1024 > freeSpace) {
                    Toast.makeText(this, String.format(getString(R.string.iput_value),
                            StringUtils.byte2FitMemorySize(freeSpace)), Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPreferences = getSharedPreferences(StringUtils.setting, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("space", s);
                    editor.apply();
                    Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }


    private void setData() {


        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        float a1 = (float)remaining / allSpace;
        float b1 = (float)freeSpace / allSpace;
        float c1 = (float)photoLength / allSpace;
        float d1 = (float)videoLength / allSpace;
        Log.e(TAG, "setData: a1 "+ a1);
        Log.e(TAG, "setData: b1 "+ b1);
        Log.e(TAG, "setData: c1 "+ c1);
        Log.e(TAG, "setData: d1 "+ d1);
        if (a1>0.0004){
            entries.add(new PieEntry( a1, mParties[0]));//剩余
        }
        if (b1>0.0004){
            entries.add(new PieEntry( b1, mParties[1]));//其他
        }
        if (c1>0.0004){
            entries.add(new PieEntry( c1, mParties[2]));//图片
        }else {
            entries.add(new PieEntry( 0.001f, mParties[2]));
        }
        if (d1>0.0004){
            entries.add(new PieEntry( d1, mParties[3]));//视频
        }

        PieDataSet dataSet = new PieDataSet(entries, "Election Results");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);
        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        //dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", xIndex: " + e.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }

}
