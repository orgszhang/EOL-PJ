package com.ht.utils;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @program: eol
 * @description: ${description}
 * @author: Zhangzhe
 * @create: 2020-03-20 15:33
 **/
public class DateUtil {
    private static String getDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static String createVitualPartNumber(int flag) {
        Calendar cal = GregorianCalendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
        switch (flag) {
            case 1:
                return "D" + format.format(cal.getTime());
            case 2:
                return "G" + format.format(cal.getTime());
            default:
                return "O" + format.format(cal.getTime());
        }
    }

    public static String formatInfo(String s) {
        return (getDate() + " - " + s + "\r\n");
    }

}
