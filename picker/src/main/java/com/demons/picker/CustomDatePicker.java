package com.demons.picker;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomDatePicker implements View.OnClickListener, PickerView.OnSelectListener {

    private Context mContext;
    private Callback mCallback;
    private Calendar mBeginTime, mEndTime, mSelectedTime;
    private boolean mCanDialogShow;

    private Dialog mPickerDialog;
    private PickerView mDpvYear, mDpvMonth, mDpvDay, mDpvWeek, mDpvHour, mDpvMinute;
    private TextView mTvHourUnit, mTvMinuteUnit;

    private int mBeginYear, mBeginMonth, mBeginDay, mBeginWeek, mBeginHour, mBeginMinute,
            mEndYear, mEndMonth, mEndDay, mEndWeek, mEndHour, mEndMinute;
    private List<String> mYearUnits = new ArrayList<>(), mMonthUnits = new ArrayList<>(), mDayUnits = new ArrayList<>(),
            mWeekUnits = new ArrayList<>(), mHourUnits = new ArrayList<>(), mMinuteUnits = new ArrayList<>();
    private DecimalFormat mDecimalFormat = new DecimalFormat("00");

    private boolean mCanShowPreciseTime;
    private int mScrollUnits = SCROLL_UNIT_HOUR + SCROLL_UNIT_MINUTE;

    /**
     * 时间单位：时、分
     */
    private static final int SCROLL_UNIT_HOUR = 0b1;
    private static final int SCROLL_UNIT_MINUTE = 0b10;

    /**
     * 时间单位的最大显示值
     */
    private static final int MAX_MINUTE_UNIT = 59;
    private static final int MAX_HOUR_UNIT = 23;
    private static final int MAX_MONTH_UNIT = 12;

    /**
     * 级联滚动延迟时间
     */
    private static final long LINKAGE_DELAY_DEFAULT = 100L;

    /**
     * 时间选择结果回调接口
     */
    public interface Callback {
        void onTimeSelected(long timestamp);
    }

    /**
     * 通过日期字符串初始换时间选择器
     *
     * @param context      Activity Context
     * @param callback     选择结果回调
     * @param beginDateStr 日期字符串，格式为 yyyy-MM-dd HH:mm
     * @param endDateStr   日期字符串，格式为 yyyy-MM-dd HH:mm
     */
    public CustomDatePicker(Context context, Callback callback, String beginDateStr, String endDateStr) {
        this(context, callback, DateFormatUtils.str2Long(beginDateStr, true),
                DateFormatUtils.str2Long(endDateStr, true));
    }

    /**
     * 通过时间戳初始换时间选择器，毫秒级别
     *
     * @param context        Activity Context
     * @param callback       选择结果回调
     * @param beginTimestamp 毫秒级时间戳
     * @param endTimestamp   毫秒级时间戳
     */
    public CustomDatePicker(Context context, Callback callback, long beginTimestamp, long endTimestamp) {
        if (context == null || callback == null || beginTimestamp <= 0 || beginTimestamp >= endTimestamp) {
            mCanDialogShow = false;
            return;
        }

        mContext = context;
        mCallback = callback;
        mBeginTime = Calendar.getInstance();
        mBeginTime.setTimeInMillis(beginTimestamp);
        mEndTime = Calendar.getInstance();
        mEndTime.setTimeInMillis(endTimestamp);
        mSelectedTime = Calendar.getInstance();

        initView();
        initData();
        mCanDialogShow = true;
    }

    private void initView() {
        mPickerDialog = new Dialog(mContext, R.style.date_picker_dialog);
        mPickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPickerDialog.setContentView(R.layout.dialog_date_picker);

        Window window = mPickerDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

        mPickerDialog.findViewById(R.id.tv_cancel).setOnClickListener(this);
        mPickerDialog.findViewById(R.id.tv_confirm).setOnClickListener(this);
        mTvHourUnit = mPickerDialog.findViewById(R.id.tv_hour_unit);
        mTvMinuteUnit = mPickerDialog.findViewById(R.id.tv_minute_unit);

        mDpvYear = mPickerDialog.findViewById(R.id.dpv_year);
        mDpvYear.setOnSelectListener(this);
        mDpvMonth = mPickerDialog.findViewById(R.id.dpv_month);
        mDpvMonth.setOnSelectListener(this);
        mDpvDay = mPickerDialog.findViewById(R.id.dpv_day);
        mDpvDay.setOnSelectListener(this);
        mDpvWeek = mPickerDialog.findViewById(R.id.dpv_week);
        mDpvWeek.setCanScroll(false);
        mDpvHour = mPickerDialog.findViewById(R.id.dpv_hour);
        mDpvHour.setOnSelectListener(this);
        mDpvMinute = mPickerDialog.findViewById(R.id.dpv_minute);
        mDpvMinute.setOnSelectListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {

        } else if (v.getId() == R.id.tv_confirm) {
            if (mCallback != null) {
                mCallback.onTimeSelected(mSelectedTime.getTimeInMillis());
            }
        }
        if (mPickerDialog != null && mPickerDialog.isShowing()) {
            mPickerDialog.dismiss();
        }
    }

    @Override
    public void onSelect(View view, String selected) {
        if (view == null || TextUtils.isEmpty(selected)) return;
        int timeUnit;
        try {
            timeUnit = Integer.parseInt(selected);
        } catch (Throwable ignored) {
            return;
        }
        if (view.getId() == R.id.dpv_year) {
            mSelectedTime.set(Calendar.YEAR, timeUnit);
            linkageMonthUnit(true, LINKAGE_DELAY_DEFAULT);
        } else if (view.getId() == R.id.dpv_month) {
            // 防止类似 2018/12/31 滚动到11月时因溢出变成 2018/12/01
            int lastSelectedMonth = mSelectedTime.get(Calendar.MONTH) + 1;
            mSelectedTime.add(Calendar.MONTH, timeUnit - lastSelectedMonth);
            linkageDayUnit(true, LINKAGE_DELAY_DEFAULT);
        } else if (view.getId() == R.id.dpv_day) {
            mSelectedTime.set(Calendar.DAY_OF_MONTH, timeUnit);
            linkageWeekUnit(true, LINKAGE_DELAY_DEFAULT);
        } else if (view.getId() == R.id.dpv_hour) {
            mSelectedTime.set(Calendar.HOUR_OF_DAY, timeUnit);
        } else if (view.getId() == R.id.dpv_minute) {
            mSelectedTime.set(Calendar.MINUTE, timeUnit);
        }
    }

    private void initData() {
        mSelectedTime.setTimeInMillis(mBeginTime.getTimeInMillis());
        mBeginYear = mBeginTime.get(Calendar.YEAR);
        // Calendar.MONTH 值为 0-11
        mBeginMonth = mBeginTime.get(Calendar.MONTH) + 1;
        mBeginDay = mBeginTime.get(Calendar.DAY_OF_MONTH);
        mBeginWeek = Calendar.getInstance().getFirstDayOfWeek();
        mBeginHour = mBeginTime.get(Calendar.HOUR_OF_DAY);
        mBeginMinute = mBeginTime.get(Calendar.MINUTE);

        mEndYear = mEndTime.get(Calendar.YEAR);
        mEndMonth = mEndTime.get(Calendar.MONTH) + 3;
        mEndDay = mEndTime.get(Calendar.DAY_OF_MONTH);
        mEndWeek = mEndTime.get(Calendar.DAY_OF_WEEK);
        mEndHour = mEndTime.get(Calendar.HOUR_OF_DAY);
        mEndMinute = mEndTime.get(Calendar.MINUTE);

        boolean canSpanYear = mBeginYear != mEndYear;
        boolean canSpanMon = !canSpanYear && mBeginMonth != mEndMonth;
        boolean canSpanDay = !canSpanMon && mBeginDay != mEndDay;
        boolean canSpanWeek = !canSpanMon && mBeginWeek != mEndWeek;
        boolean canSpanHour = !canSpanDay && mBeginHour != mEndHour;
        boolean canSpanMinute = !canSpanHour && mBeginMinute != mEndMinute;
        if (canSpanYear) {
            initDateUnits(MAX_MONTH_UNIT, mBeginTime.getActualMaximum(Calendar.DAY_OF_MONTH), 7, MAX_HOUR_UNIT, MAX_MINUTE_UNIT);
        } else if (canSpanMon) {
            initDateUnits(mEndMonth, mBeginTime.getActualMaximum(Calendar.DAY_OF_MONTH), 7, MAX_HOUR_UNIT, MAX_MINUTE_UNIT);
        } else if (canSpanDay) {
            initDateUnits(mEndMonth, mEndDay, 7, MAX_HOUR_UNIT, MAX_MINUTE_UNIT);
        } else if (canSpanWeek) {
            initDateUnits(mEndMonth, mEndDay, 7, MAX_HOUR_UNIT, MAX_MINUTE_UNIT);
        } else if (canSpanHour) {
            initDateUnits(mEndMonth, mEndDay, 7, mEndHour, MAX_MINUTE_UNIT);
        } else if (canSpanMinute) {
            initDateUnits(mEndMonth, mEndDay, 7, mEndHour, mEndMinute);
        }
    }

    private void initDateUnits(int endMonth, int endDay, int endWeek, int endHour, int endMinute) {
        for (int i = mBeginYear; i <= mEndYear; i++) {
            mYearUnits.add(String.valueOf(i));
        }

        for (int i = mBeginMonth; i <= endMonth; i++) {
            mMonthUnits.add(mDecimalFormat.format(i));
        }

        for (int i = mBeginDay; i <= endDay; i++) {
            mDayUnits.add(mDecimalFormat.format(i));
        }

        for (int i = mBeginWeek; i <= endWeek; i++) {
            mWeekUnits.add(formatWeekInfo(i));
        }

        if ((mScrollUnits & SCROLL_UNIT_HOUR) != SCROLL_UNIT_HOUR) {
            mHourUnits.add(mDecimalFormat.format(mBeginHour));
        } else {
            for (int i = mBeginHour; i <= endHour; i++) {
                mHourUnits.add(mDecimalFormat.format(i));
            }
        }

        if ((mScrollUnits & SCROLL_UNIT_MINUTE) != SCROLL_UNIT_MINUTE) {
            mMinuteUnits.add(mDecimalFormat.format(mBeginMinute));
        } else {
            for (int i = mBeginMinute; i <= endMinute; i++) {
                mMinuteUnits.add(mDecimalFormat.format(i));
            }
        }

        mDpvYear.setDataList(mYearUnits);
        mDpvYear.setSelected(0);
        mDpvMonth.setDataList(mMonthUnits);
        mDpvMonth.setSelected(0);
        mDpvDay.setDataList(mDayUnits);
        mDpvDay.setSelected(0);
        mDpvWeek.setDataList(mWeekUnits);
        mDpvWeek.setSelected(0);
        mDpvHour.setDataList(mHourUnits);
        mDpvHour.setSelected(0);
        mDpvMinute.setDataList(mMinuteUnits);
        mDpvMinute.setSelected(0);

        setCanScroll();
    }

    private String formatWeekInfo(int i) {
        String weekInfo = "";
        if (i == 1) {
            weekInfo = "周日";
        } else if (i == 2) {
            weekInfo = "周一";
        } else if (i == 3) {
            weekInfo = "周二";
        } else if (i == 4) {
            weekInfo = "周三";
        } else if (i == 5) {
            weekInfo = "周四";
        } else if (i == 6) {
            weekInfo = "周五";
        } else if (i == 7) {
            weekInfo = "周六";
        }
        return weekInfo;
    }

    private void setCanScroll() {
        mDpvYear.setCanScroll(mYearUnits.size() > 1);
        mDpvMonth.setCanScroll(mMonthUnits.size() > 1);
        mDpvDay.setCanScroll(mDayUnits.size() > 1);
//        mDpvWeek.setCanScroll(mWeekUnits.size() > 1);
        mDpvHour.setCanScroll(mHourUnits.size() > 1 && (mScrollUnits & SCROLL_UNIT_HOUR) == SCROLL_UNIT_HOUR);
        mDpvMinute.setCanScroll(mMinuteUnits.size() > 1 && (mScrollUnits & SCROLL_UNIT_MINUTE) == SCROLL_UNIT_MINUTE);
    }

    /**
     * 联动“月”变化
     *
     * @param showAnim 是否展示滚动动画
     * @param delay    联动下一级延迟时间
     */
    private void linkageMonthUnit(final boolean showAnim, final long delay) {
        int minMonth;
        int maxMonth;
        int selectedYear = mSelectedTime.get(Calendar.YEAR);
        if (mBeginYear == mEndYear) {
            minMonth = mBeginMonth;
            maxMonth = mEndMonth;
        } else if (selectedYear == mBeginYear) {
            minMonth = mBeginMonth;
            maxMonth = MAX_MONTH_UNIT;
        } else if (selectedYear == mEndYear) {
            minMonth = 1;
            maxMonth = mEndMonth;
        } else {
            minMonth = 1;
            maxMonth = MAX_MONTH_UNIT;
        }

        // 重新初始化时间单元容器
        mMonthUnits.clear();
        for (int i = minMonth; i <= maxMonth; i++) {
            mMonthUnits.add(mDecimalFormat.format(i));
        }
        mDpvMonth.setDataList(mMonthUnits);

        // 确保联动时不会溢出或改变关联选中值
        int selectedMonth = getValueInRange(mSelectedTime.get(Calendar.MONTH) + 1, minMonth, maxMonth);
        mSelectedTime.set(Calendar.MONTH, selectedMonth - 1);
        mDpvMonth.setSelected(selectedMonth - minMonth);
        if (showAnim) {
            mDpvMonth.startAnim();
        }

        // 联动“日”变化
        mDpvMonth.postDelayed(new Runnable() {
            @Override
            public void run() {
                linkageDayUnit(showAnim, delay);
            }
        }, delay);
    }

    /**
     * 联动“日”变化
     *
     * @param showAnim 是否展示滚动动画
     * @param delay    联动下一级延迟时间
     */
    private void linkageDayUnit(final boolean showAnim, final long delay) {
        int minDay;
        int maxDay;
        int selectedYear = mSelectedTime.get(Calendar.YEAR);
        int selectedMonth = mSelectedTime.get(Calendar.MONTH) + 1;
        if (mBeginYear == mEndYear && mBeginMonth == mEndMonth) {
            minDay = mBeginDay;
            maxDay = mEndDay;
        } else if (selectedYear == mBeginYear && selectedMonth == mBeginMonth) {
            minDay = mBeginDay;
            maxDay = mSelectedTime.getActualMaximum(Calendar.DAY_OF_MONTH);
        } else if (selectedYear == mEndYear && selectedMonth == mEndMonth) {
            minDay = 1;
            maxDay = mEndDay;
        } else {
            minDay = 1;
            maxDay = mSelectedTime.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        mDayUnits.clear();
        for (int i = minDay; i <= maxDay; i++) {
            mDayUnits.add(mDecimalFormat.format(i));
        }
        mDpvDay.setDataList(mDayUnits);

        final int selectedDay = getValueInRange(mSelectedTime.get(Calendar.DAY_OF_MONTH), minDay, maxDay);
        mSelectedTime.set(Calendar.DAY_OF_MONTH, selectedDay);
        mDpvDay.setSelected(selectedDay - minDay);
        if (showAnim) {
            mDpvDay.startAnim();
        }
        // 联动“日”变化
        mDpvMonth.postDelayed(new Runnable() {
            @Override
            public void run() {
                linkageWeekUnit(showAnim, delay);
            }
        }, delay);
    }

    /**
     * 联动“周”变化
     *
     * @param showAnim 是否展示滚动动画
     * @param delay    联动下一级延迟时间
     */
    private void linkageWeekUnit(boolean showAnim, long delay) {
        mWeekUnits.clear();
        for (int i = 1; i <= 7; i++) {
            mWeekUnits.add(formatWeekInfo(i));
        }
        mDpvWeek.setDataList(mWeekUnits);
        mDpvWeek.setSelected(mSelectedTime.get(Calendar.DAY_OF_WEEK) - 1);
        String s = formatWeekInfo(mSelectedTime.get(Calendar.DAY_OF_WEEK));
        Log.e("当前是：", mSelectedTime.get(Calendar.DAY_OF_MONTH) + "日，" + s);
    }

    private int getValueInRange(int value, int minValue, int maxValue) {
        if (value < minValue) {
            return minValue;
        } else if (value > maxValue) {
            return maxValue;
        } else {
            return value;
        }
    }

    /**
     * 展示时间选择器
     *
     * @param dateStr 日期字符串，格式为 yyyy-MM-dd 或 yyyy-MM-dd HH:mm
     */
    public void show(String dateStr) {
        if (!canShow() || TextUtils.isEmpty(dateStr)) return;

        // 弹窗时，考虑用户体验，不展示滚动动画
        if (setSelectedTime(dateStr, false)) {
            mPickerDialog.show();
        }
    }

    private boolean canShow() {
        return mCanDialogShow && mPickerDialog != null;
    }

    /**
     * 设置日期选择器的选中时间
     *
     * @param dateStr  日期字符串
     * @param showAnim 是否展示动画
     * @return 是否设置成功
     */
    public boolean setSelectedTime(String dateStr, boolean showAnim) {
        return canShow() && !TextUtils.isEmpty(dateStr)
                && setSelectedTime(DateFormatUtils.str2Long(dateStr, mCanShowPreciseTime), showAnim);
    }

    /**
     * 展示时间选择器
     *
     * @param timestamp 时间戳，毫秒级别
     */
    public void show(long timestamp) {
        if (!canShow()) return;

        if (setSelectedTime(timestamp, false)) {
            mPickerDialog.show();
        }
    }

    /**
     * 设置日期选择器的选中时间
     *
     * @param timestamp 毫秒级时间戳
     * @param showAnim  是否展示动画
     * @return 是否设置成功
     */
    public boolean setSelectedTime(long timestamp, boolean showAnim) {
        if (!canShow()) return false;

        if (timestamp < mBeginTime.getTimeInMillis()) {
            timestamp = mBeginTime.getTimeInMillis();
        } else if (timestamp > mEndTime.getTimeInMillis()) {
            timestamp = mEndTime.getTimeInMillis();
        }
        mSelectedTime.setTimeInMillis(timestamp);

        mYearUnits.clear();
        for (int i = mBeginYear; i <= mEndYear; i++) {
            mYearUnits.add(String.valueOf(i));
        }
        mDpvYear.setDataList(mYearUnits);
        mDpvYear.setSelected(mSelectedTime.get(Calendar.YEAR) - mBeginYear);
        linkageMonthUnit(showAnim, showAnim ? LINKAGE_DELAY_DEFAULT : 0);
        return true;
    }

    /**
     * 设置是否允许点击屏幕或物理返回键关闭
     */
    public void setCancelable(boolean cancelable) {
        if (!canShow()) return;

        mPickerDialog.setCancelable(cancelable);
    }

    /**
     * 设置日期控件是否显示时和分
     */
    public void setCanShowPreciseTime(boolean canShowPreciseTime) {
        if (!canShow()) return;

        if (canShowPreciseTime) {
            initScrollUnit();
            mDpvHour.setVisibility(View.VISIBLE);
            mTvHourUnit.setVisibility(View.VISIBLE);
            mDpvMinute.setVisibility(View.VISIBLE);
            mTvMinuteUnit.setVisibility(View.VISIBLE);
        } else {
            initScrollUnit(SCROLL_UNIT_HOUR, SCROLL_UNIT_MINUTE);
            mDpvHour.setVisibility(View.GONE);
            mTvHourUnit.setVisibility(View.GONE);
            mDpvMinute.setVisibility(View.GONE);
            mTvMinuteUnit.setVisibility(View.GONE);
        }
        mCanShowPreciseTime = canShowPreciseTime;
    }

    private void initScrollUnit(Integer... units) {
        if (units == null || units.length == 0) {
            mScrollUnits = SCROLL_UNIT_HOUR + SCROLL_UNIT_MINUTE;
        } else {
            for (int unit : units) {
                mScrollUnits ^= unit;
            }
        }
    }

    /**
     * 设置日期控件是否可以循环滚动
     */
    public void setScrollLoop(boolean canLoop) {
        if (!canShow()) return;

        mDpvYear.setCanScrollLoop(canLoop);
        mDpvMonth.setCanScrollLoop(canLoop);
        mDpvDay.setCanScrollLoop(canLoop);
//        mDpvWeek.setCanScrollLoop(canLoop);
        mDpvHour.setCanScrollLoop(canLoop);
        mDpvMinute.setCanScrollLoop(canLoop);
    }

    /**
     * 设置日期控件是否展示滚动动画
     */
    public void setCanShowAnim(boolean canShowAnim) {
        if (!canShow()) return;

        mDpvYear.setCanShowAnim(canShowAnim);
        mDpvMonth.setCanShowAnim(canShowAnim);
        mDpvDay.setCanShowAnim(canShowAnim);
//        mDpvWeek.setCanShowAnim(canShowAnim);
        mDpvHour.setCanShowAnim(canShowAnim);
        mDpvMinute.setCanShowAnim(canShowAnim);
    }

    /**
     * 销毁弹窗
     */
    public void onDestroy() {
        if (mPickerDialog != null) {
            mPickerDialog.dismiss();
            mPickerDialog = null;

            mDpvYear.onDestroy();
            mDpvMonth.onDestroy();
            mDpvDay.onDestroy();
            mDpvWeek.onDestroy();
            mDpvHour.onDestroy();
            mDpvMinute.onDestroy();
        }
    }

}
