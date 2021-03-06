package com.ht.utils;

public class TestConstant {
    public static float DEFALUT_ERROR_RESISTOR = -2000;
    public static float DEFAULT_ERROR_TEMPERATURE = -2000;

    public static int TEST_TIMES = 5;

    public static double RESISTOR_EXP = 75;
    public static double RESISTOR_TOLERANCE = 0.05;
    public static double up = RESISTOR_EXP * (1 + RESISTOR_TOLERANCE);
    public static double low = RESISTOR_EXP * (1 - RESISTOR_TOLERANCE);
    public static double inup = RESISTOR_EXP * (1 + RESISTOR_TOLERANCE - 0.005);
    public static double inlow = RESISTOR_EXP * (1 - RESISTOR_TOLERANCE + 0.005);
    public static int TEMP_GAP = 5;

    public static String SVW = "11D915743";
    public static String FAW = "11G915743";

    public static int ZEROV_TIMES = 3;
    public static double ZEROV_UPPER = 0.000003d;
}
