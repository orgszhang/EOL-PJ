package com.ht.utils;


import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO:
 * 测试结果计算
 *
 *
 */
public final class ResultCalculator {
    private static final Log logger = LogFactory.getLog(ResultCalculator.class);
    /**
     * Rt = Ut / It
     * @param voltage 电压
     * @param current 电流
     * @return
     */
    public static double calRt(double voltage, double current) {

        double result = voltage / current;
        return result;
    }

    /**
     * Rw = Uw / It
     * @param voltage 电压
     * @param current 电流
     * @return
     */
    public static double calRw(double voltage, double current) {

        double result = voltage / current;
        return result;
    }

    /**
     * ΔR 在±0.5之间，这个条件1合格，显示在界面上，不存入数据库
     *
     *  ΔR = (R1-R0)/R0
     * @param resistanceT
     * @param resistanceW
     * @return
     */
    public static double calDeltaR(double resistanceT, double resistanceW) {
        return (resistanceT - resistanceW)/resistanceT;
    }

    public static boolean isDeltaROk(double deltaR) {
        logger.info("IS isDeltaROk input : " + deltaR + ", result: " + (deltaR > 0.5 && deltaR < 1.5));
        return deltaR > 0.5 && deltaR < 1.5;
    }



    /**
     * TODO: ???
     */
    public static double calTemperature() {
        return RandomUtils.nextDouble();
    }


    public static boolean isTemperatureOk(double temperature) {
        boolean result = temperature <=28 && temperature >=22;
        logger.info("IS isTemperatureOk input : " + temperature + ", result: " + result);
        return result;
    }


    // -----------------------------------------------
    public static String fakeRtStr() {
        return String.format("%.4f", RandomUtils.nextDouble(10, 90));
    }
    public static String fakeRwStr() {
        return String.format("%.4f", RandomUtils.nextDouble(10, 90));
    }

    public static double fakeRt() {
        return RandomUtils.nextDouble(10, 90);
    }
    public static double fakeRw() {
        return RandomUtils.nextDouble(10, 90);
    }
    public static int fakeRntc() {
        return RandomUtils.nextInt(1000, 11000);
    }
    public static double fakeIt() {
        return RandomUtils.nextInt(10, 30);
    }

    public static int fakeTemperature() {
        return RandomUtils.nextInt(20, 30);
    }

    public static double fakeDeltaR() {
        return RandomUtils.nextDouble(0, 1);
    }





    public static void main(String[] args) {
        System.out.println(String.format("%.3f", 1.23456));
    }
}
