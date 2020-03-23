package com.ht.swing;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * http://teaching.csse.uwa.edu.au/units/CITS1001/colorinfo.html
 *
 *
 */
public final class UIConstant {
    static final String APP_NAME = "HTSS EOL Tester";
    static final String RESET_BUTTON = "全部重置";
    static final String NETPORT_OPEN = "打开网口";
    static final String NETPORT_CLOSE = "关闭网口";
    // static final String VERIFY_QRCODE_BUTTON = "验证二维码";

    static final String LABEL_TO_GENERATE = "待生成";
    static final String LABEL_TO_TEST = "待检测";

    // static final String LABEL_DEVICE_CONFIG = "查看设备网络配置";

    static final String EMPTY_STRING = "";

    static final double TEST_TEMP = 23d;

    public static final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 28);
    public static final Font AREA_FONT = new Font("微软雅黑", Font.BOLD, 22);
    public static final Font TEXT_FONT = new Font("微软雅黑", Font.BOLD, 16);
    public static final Font COPYRIGHT_FONT = new Font("微软雅黑", Font.BOLD, 10);

    public static final Color DARK_GREEN = new Color(0,153, 0);

    public static final String DELTA_R = "∆R";
    public static final String PERCENT = "%";
    public static final String UNIT_TEMPERATURE = "°C";
    public static final String EQUAL = "=";
    public static final String TEMPERATURE = "T";

    public static final Dimension INPUT_DIMENSION = new Dimension(120, 30);
    public static final Dimension INPUT_LONGDIMENSION = new Dimension(200, 30);
    public static final Dimension BUTTON_DIMENSION = new Dimension(200, 30);
    public static final Color BGCOLOR_BLUE = new Color(0, 173, 232);
    public static final Color BGCOLOR_GRAY = new Color(160, 160, 160);

    public static final int SCREEN_WIDTH = 1000;


    //
    public static String formatDeltaR(double input) {
        return DELTA_R + EQUAL + String.format("%.4f", input / 100) + PERCENT;
    }

    public static String formatTemperature(int input) {
        return TEMPERATURE + EQUAL + input + UNIT_TEMPERATURE;
    }

    public static String formatTemperature(double input) {
        return TEMPERATURE + EQUAL + String.format("%.1f", input) + UNIT_TEMPERATURE;
    }

    private static DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public static String formatInfo(String s) {
        return (DATEFORMAT.format(new Date()) + " - " + s + "\r\n");
    }
}
