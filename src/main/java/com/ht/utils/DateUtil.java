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
    public static String getdate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String result = format.format(date);
        return result;
    }
}
