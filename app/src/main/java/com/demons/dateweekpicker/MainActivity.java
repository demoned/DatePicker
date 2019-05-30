package com.demons.dateweekpicker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.demons.picker.CustomDatePicker;
import com.demons.picker.DateFormatUtils;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvSelectedDate, mTvSelectedTime;
    private CustomDatePicker mDatePicker, mTimerPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.ll_date).setOnClickListener(this);
        mTvSelectedDate = findViewById(R.id.tv_selected_date);
        initDatePicker();

        findViewById(R.id.ll_time).setOnClickListener(this);
        mTvSelectedTime = findViewById(R.id.tv_selected_time);
        initTimerPicker();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_date:
                // 日期格式为yyyy-MM-dd
                mDatePicker.show(DateFormatUtils.str2Long(DateFormatUtils.getTimeByCalendar(mTimestamp), false));
                break;

            case R.id.ll_time:
                // 日期格式为yyyy-MM-dd HH:mm
                mTimerPicker.show(DateFormatUtils.str2Long(DateFormatUtils.getTimeByCalendar(mTimestamp), false));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatePicker.onDestroy();
    }

    private Calendar mTimestamp = Calendar.getInstance();

    private void initDatePicker() {

        long beginTime = DateFormatUtils.str2Long(DateFormatUtils.getYear(-2), false);
        long endTime = DateFormatUtils.str2Long(DateFormatUtils.getYear(2), false);

        mTvSelectedDate.setText(DateFormatUtils.formatDateInfo(mTimestamp));

        // 通过时间戳初始化日期，毫秒级别
        mDatePicker = new CustomDatePicker(this, new CustomDatePicker.Callback() {
            @Override
            public void onTimeSelected(Calendar timestamp) {
                mTimestamp = timestamp;
                mTvSelectedDate.setText(DateFormatUtils.formatDateInfo(timestamp));
            }
        }, beginTime, endTime);
        // 不允许点击屏幕或物理返回键关闭
        mDatePicker.setCancelable(false);
        // 不显示时和分
        mDatePicker.setCanShowPreciseTime(false);
        // 不允许循环滚动
        mDatePicker.setScrollLoop(false);
        // 不允许滚动动画
        mDatePicker.setCanShowAnim(false);
    }

    private Calendar mTvTimestamp = Calendar.getInstance();

    private void initTimerPicker() {


        long beginTime = DateFormatUtils.str2Long(DateFormatUtils.getYear(-2), false);
        long endTime = DateFormatUtils.str2Long(DateFormatUtils.getYear(2), false);

        mTvSelectedTime.setText(DateFormatUtils.formatDateInfo(mTimestamp));
        // 通过日期字符串初始化日期，格式请用：yyyy-MM-dd HH:mm
        mTimerPicker = new CustomDatePicker(this, new CustomDatePicker.Callback() {
            @Override
            public void onTimeSelected(Calendar timestamp) {
                mTvTimestamp = timestamp;
                mTvSelectedTime.setText(DateFormatUtils.formatDateInfo(timestamp));
            }
        }, beginTime, endTime);
        // 允许点击屏幕或物理返回键关闭
        mTimerPicker.setCancelable(true);
        // 显示时和分
        mTimerPicker.setCanShowPreciseTime(true);
        // 允许循环滚动
        mTimerPicker.setScrollLoop(true);
        // 允许滚动动画
        mTimerPicker.setCanShowAnim(true);
    }
}
