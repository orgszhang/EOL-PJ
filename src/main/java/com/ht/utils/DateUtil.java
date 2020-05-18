package com.ht.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @program: eol
 * @description: ${description}
 * @author: Zhangzhe
 * @create: 2020-03-20 15:33
 **/
public class DateUtil {
    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return format.format(date);
    }

    public static String formatInfo(String s) {
        return (getDate() + " - " + s + "\r\n");
    }

}
