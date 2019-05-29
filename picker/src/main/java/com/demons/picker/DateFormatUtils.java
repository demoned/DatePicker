package com.demons.picker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateFormatUtils {

    private static final String DATE_FORMAT_PATTERN_YMD = "yyyy-MM-dd";
    private static final String DATE_FORMAT_PATTERN_YMD_HM = "yyyy-MM-dd HH:mm";

    /**
     * 时间戳转字符串
     *
     * @param timestamp     时间戳
     * @param isPreciseTime 是否包含时分
     * @return 格式化的日期字符串
     */
    public static String long2Str(long timestamp, boolean isPreciseTime) {
        return long2Str(timestamp, getFormatPattern(isPreciseTime));
    }

    private static String long2Str(long timestamp, String pattern) {
        return new SimpleDateFormat(pattern, Locale.CHINA).format(new Date(timestamp));
    }

    /**
     * 字符串转时间戳
     *
     * @param dateStr       日期字符串
     * @param isPreciseTime 是否包含时分
     * @return 时间戳
     */
    public static long str2Long(String dateStr, boolean isPreciseTime) {
        return str2Long(dateStr, getFormatPattern(isPreciseTime));
    }

    private static long str2Long(String dateStr, String pattern) {
        try {
            return new SimpleDateFormat(pattern, Locale.CHINA).parse(dateStr).getTime();
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private static String getFormatPattern(boolean showSpecificTime) {
        if (showSpecificTime) {
            return DATE_FORMAT_PATTERN_YMD_HM;
        } else {
            return DATE_FORMAT_PATTERN_YMD;
        }
    }

    public static String formatWeekInfo(int i) {
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

    public static String formatDateInfo(Calendar timestamp) {
        int year = timestamp.get(Calendar.YEAR);
        int month = timestamp.get(Calendar.MONTH) + 1;
        int date = timestamp.get(Calendar.DAY_OF_MONTH);
        int week = timestamp.get(Calendar.DAY_OF_WEEK);
        int hour = timestamp.get(Calendar.HOUR_OF_DAY);
        int minute = timestamp.get(Calendar.MINUTE);
        return year + "/" + month + "/" + date + " " + DateFormatUtils.formatWeekInfo(week) + " " + hour + ":" + minute;
    }

    public static String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date d1 = new Date();
        return format.format(d1);
    }

    public static String getYear(int i) {
        Date date = new Date();// 取时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, i);// 把日期往后增加一年.整数往后推,负数往前移动
        date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static String getMonth(int i) {
        Date date = new Date();// 取时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, i);// 把日期往后增加一年.整数往后推,负数往前移动
        date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }
}
