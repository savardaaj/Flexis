package com.asav.flexis;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.asav.flexis.MainActivity.printStackTrace;

public class TimeConverter {

    public TimeConverter() {

    }

    //converts from 5:06 pm to GMT time, 05:06:00Z-5:00
    public static String convertToGMTFromDisplay(String timeParam) {

        String gmt = "";

        try {
            String time = timeParam.substring(0, 5);
            String AMPM = timeParam.substring(6, 8);

            SimpleDateFormat inputDateFormat = new SimpleDateFormat("HH:mm");
            inputDateFormat.setTimeZone(TimeZone.getDefault());

            Date inputDate = inputDateFormat.parse(timeParam);

            Calendar cal = Calendar.getInstance();
            cal.setTime(inputDate);
            //cal.add(Calendar.HOUR_OF_DAY, 12);

            SimpleDateFormat outputDateFormat = new SimpleDateFormat("HH:mm:ss 'GMT'");
            outputDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            Date militaryTime = new Date();

            if(AMPM.equals("am")) {
                militaryTime = cal.getTime();
            }
            else {
                cal.add(Calendar.HOUR_OF_DAY, 12);
                militaryTime = cal.getTime();
            }

            //convert military to GMT
            gmt = outputDateFormat.format(militaryTime);

        }
        catch(Exception e) {
            Log.d("***ERROR***", "convertToGMTFromDisplay: " + e.getMessage());
            Log.d("***ERROR***", "convertToGMTFromDisplay: " + e);
            printStackTrace(e.getStackTrace());
        }
        return gmt;
    }

    public static void convertFromGMTDisplay(String timeParam) {

    }
    public static Date convertDisplayToDate(String timeParam) {

        Date inputDate = new Date();
        String AMPM = timeParam.substring(6, 8);

        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("HH:mm");
            //inputDateFormat.setTimeZone(TimeZone.getDefault());

            inputDate = inputDateFormat.parse(timeParam);

            Calendar cal = Calendar.getInstance();
            cal.setTime(inputDate);

            Date militaryTime = new Date();

            if(AMPM.equals("am")) {
                militaryTime = cal.getTime();
            }
            else {
                cal.add(Calendar.HOUR_OF_DAY, 12);
                militaryTime = cal.getTime();
            }

            DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            String time = dateFormat.format(militaryTime);
            inputDate = inputDateFormat.parse(time);
        }
        catch (Exception e) {
            Log.d("***ERROR***", "convertDisplayToDate: " + e.getMessage());
            Log.d("***ERROR***", "convertDisplayToDate: " + e);
            printStackTrace(e.getStackTrace());
        }

        return inputDate;
    }

    public static Date getCurrentTime() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("HH:mm");
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String time = dateFormat.format(date);
        try {
            date = inputDateFormat.parse(time);
        }
        catch (Exception e) {
            Log.d("***ERROR***", "getCurrentTime: " + e.getMessage());
            Log.d("***ERROR***", "getCurrentTime: " + e);
            printStackTrace(e.getStackTrace());
        }

        return date;
    }

}
