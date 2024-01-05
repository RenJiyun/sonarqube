package com.wlzq.activity.base.utils;


import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author wlzq
 */
public class DateUtil {
    public final static String TIME_INTERVAL_YEAR = "year";
    public final static String TIME_INTERVAL_QUARTER = "quarter";
    public final static String TIME_INTERVAL_MONTH = "month";
    public final static String TIME_INTERVAL_WEEK = "week";
    public final static String TIME_INTERVAL_DAY = "day";
    public final static String TIME_INTERVAL_HOUR = "hour";
    public final static String TIME_INTERVAL_MINUTE = "minute";
    public final static String TIME_INTERVAL_SECOND = "second";

    /**
     * 按指定日期单位计算两个日期间的间隔
     *
     * @param timeInterval
     * @param date1
     * @param date2
     * @return
     */
    public static long dateDiff(String timeInterval, Date date1, Date date2) {
        if (TIME_INTERVAL_YEAR.equals(timeInterval)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            int time = calendar.get(Calendar.YEAR);
            calendar.setTime(date2);
            return time - calendar.get(Calendar.YEAR);
        }

        if (TIME_INTERVAL_QUARTER.equals(timeInterval)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            int time = calendar.get(Calendar.YEAR) * 4;
            calendar.setTime(date2);
            time -= calendar.get(Calendar.YEAR) * 4;
            calendar.setTime(date1);
            time += calendar.get(Calendar.MONTH) / 4;
            calendar.setTime(date2);
            return time - calendar.get(Calendar.MONTH) / 4;
        }

        if (TIME_INTERVAL_MONTH.equals(timeInterval)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            int time = calendar.get(Calendar.YEAR) * 12;
            calendar.setTime(date2);
            time -= calendar.get(Calendar.YEAR) * 12;
            calendar.setTime(date1);
            time += calendar.get(Calendar.MONTH);
            calendar.setTime(date2);
            return time - calendar.get(Calendar.MONTH);
        }

        if (TIME_INTERVAL_WEEK.equals(timeInterval)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            int time = calendar.get(Calendar.YEAR) * 52;
            calendar.setTime(date2);
            time -= calendar.get(Calendar.YEAR) * 52;
            calendar.setTime(date1);
            time += calendar.get(Calendar.WEEK_OF_YEAR);
            calendar.setTime(date2);
            return time - calendar.get(Calendar.WEEK_OF_YEAR);
        }

        if (TIME_INTERVAL_DAY.equals(timeInterval)) {
            long time = date1.getTime() / 1000 / 60 / 60 / 24;
            return time - date2.getTime() / 1000 / 60 / 60 / 24;
        }

        if (TIME_INTERVAL_HOUR.equals(timeInterval)) {
            long time = date1.getTime() / 1000 / 60 / 60;
            return time - date2.getTime() / 1000 / 60 / 60;
        }

        if (TIME_INTERVAL_MINUTE.equals(timeInterval)) {
            long time = date1.getTime() / 1000 / 60;
            return time - date2.getTime() / 1000 / 60;
        }

        if (TIME_INTERVAL_SECOND.equals(timeInterval)) {
            long time = date1.getTime() / 1000;
            return time - date2.getTime() / 1000;
        }

        return date1.getTime() - date2.getTime();
    }

    /**
     * 日期格式化，默认格式yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String dateFormat(Date date) {
        return dateFormat(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 日期格式化
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateFormat(Date date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 日期格式化
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateFormat(String date, String format) {
        try {
            Date newDate = stringToDate(date, "");
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(newDate);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 日期格式化
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateFormat(String date, String strToDateFormat, String format) {
        try {
            Date newDate = stringToDate(date, strToDateFormat);
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(newDate);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前格式化年月日
     *
     * @return
     */
    public static String getCurrYmd() {
        return dateFormat(new Date(), "yyyy-MM-dd");
    }

    /**
     * 格式化显示当前日期
     *
     * @param format
     * @return
     */
    public static String dateFormat(String format) {
        if (format != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(getServerDate());
        }
        return null;
    }

    /**
     * 时间格式化， 传入毫秒
     *
     * @param time
     * @return
     */
    public static String dateFormat(long time) {
        if (time <= 0) {
            return null;
        }
        return dateFormat(new Date(time), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 时间格式化， 传入毫秒
     *
     * @param time
     * @return
     */
    public static String dateFormat(long time, String format) {
        try {
            if (time <= 0) {
                return null;
            }
            if (format == null) {
                format = "yyyy-MM-dd HH:mm:ss";
            }
            return dateFormat(new Date(time), format);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 服务器当前日期
     *
     * @return
     */
    public static Date getServerDate() {
        return new Date();
    }

    /**
     * 字符串转换成时间
     *
     * @param date
     * @param format
     * @return
     */
    public static Date stringToDate(String date, String format) {
        String space = " ";
        try {
            if (StringUtils.isBlank(format)) {
                format = "yyyy-MM-dd HH:mm:ss";
            }
            if (date.split(space).length == 1) {
                format = "yyyy-MM-dd";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算两个时间段之间的所耗时间
     *
     * @param date  开始时间
     * @param date2 结束时间
     * @return 所消耗时间段的字符串
     */
    public static String getRunningTime(Date date, Date date2) {
        if (date == null || date2 == null) {
            return null;
        }
        long mss = date2.getTime() - date.getTime();
        return getRunnigtTime(mss);
    }

    public static String getRunnigtTime(Long mss) {
        try {
            long displayFormatLimit = 10;
            long days = mss / (1000 * 60 * 60 * 24);
            long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
            long seconds = (mss % (1000 * 60)) / 1000;
            StringBuffer str = new StringBuffer();
            if (days > 0) {
                str.append(days).append("天,");
            }
            if (hours < displayFormatLimit) {
                str.append("0");
            }
            str.append(hours).append(":");
            if (minutes < displayFormatLimit) {
                str.append("0");
            }
            str.append(minutes).append(":");
            if (seconds < displayFormatLimit) {
                str.append("0");
            }
            return str.append(seconds).toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取时间戳
     *
     * @return
     */
    public static String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

}

