package com.nexr.newsfeed.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Utility
 */
public class Utils {

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static long parseTime(String timeString) throws ParseException {
        String pattern = "yyyy-MM-dd HH:mm:ss,SSS";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.parse(timeString).getTime();
    }

    public static long parseTime(String timeString, String pattern) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.parse(timeString).getTime();
    }

    public static String formatTime(long timestamp) {
        String pattern = "yyyy-MM-dd HH:mm:ss,SSS";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return format.format(calendar.getTime());
    }

    public static String formatTime(long timestamp, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return format.format(calendar.getTime());
    }

    public static String formatTime(long timestamp, String pattern, String timezone) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone(timezone));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return format.format(calendar.getTime());
    }

    public static String formatDateString(long time) {
        return formatTime(time, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Parse a given dateString(yyyy-MM-dd HH:mm:ss) to millis
     * @param dateString
     * @return
     * @throws ParseException
     */
    public static long parseTimeInMillis(String dateString) throws ParseException {
        return parseTime(dateString, "yyyy-MM-dd HH:mm:ss");
    }

    public static String convertErrorObjectToJson(int status, String message) {
        ErrorObject errorObject = new ErrorObject(status, message);
        try {
            return errorObject.toJson();
        } catch (Exception e) {
            return "{\"status\":" + status + ",\"message\":\"" + message + "\"}";
        }
    }

    /**
     * Adds the amount of {@code day} to {@code basetime}.
     * @param basetime
     * @param day
     * @return
     */
    public static long add(long basetime, int day) {
        // TODO yoda time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(basetime);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + day);
        return calendar.getTimeInMillis();
    }

    public static boolean validEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        return pattern.matcher(email).matches();
    }
}
