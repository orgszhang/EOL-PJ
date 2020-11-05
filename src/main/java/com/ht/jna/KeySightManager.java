package com.ht.jna;

import com.ht.base.SpringContext;
import com.ht.dc.TcpClient;
import com.ht.entity.*;
import com.ht.repository.LatestQRCodeRepo;
import com.ht.repository.ProRecordsRepo;
import com.ht.repository.ShuntResistorsRepo;
import com.ht.repository.TestResultsRepo;
import com.ht.utils.DateUtil;
import com.ht.utils.QRCodeGenerator;
import com.ht.utils.TempCalculator;
import com.ht.utils.TestConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public class KeySightManager {
    private static final Log logger = LogFactory.getLog(KeySightManager.class);

    KeySightDevice_Voltage keySightDeviceVoltage;
    KeySightDevice_Voltage16 keySightDeviceVoltage16;
    KeySightDevice_Electricity keySightDeviceElectricity;
    KeySightDevice_NTC keySightDeviceNtc;

/*    public static void main(String[] args) {
        double cirTemp = 15; // 环境温度
        String resistorID = "20203010282";
        String key = "11D915743";

        KeySightManager manager = new KeySightManager();
        manager.initDevices();
        *//*       TestResults result = manager.driveDevices(resistorID, cirTemp, mDataView, eolStatus);*//*
        manager.closeDivices();
    }*/

    public void initDevices(JTextArea mDataView) {
        //每一个六位半对应一个class
        keySightDeviceElectricity = new KeySightDevice_Electricity();
        keySightDeviceVoltage = new KeySightDevice_Voltage();
        keySightDeviceVoltage16 = new KeySightDevice_Voltage16();
        keySightDeviceNtc = new KeySightDevice_NTC();

        //分别打开三台设备
        keySightDeviceElectricity.open(mDataView);
        keySightDeviceVoltage.open(mDataView);
        keySightDeviceVoltage16.open(mDataView);
        keySightDeviceNtc.open(mDataView);

        //分别设置设备的量程
        keySightDeviceElectricity.setEleCONF();
        keySightDeviceVoltage.setVolCONF();
        keySightDeviceVoltage16.setVolCONF();
        keySightDeviceNtc.setRCONF();
    }

    public void closeDivices() {
        //读取完成后关闭设备
        keySightDeviceElectricity.close();
        keySightDeviceVoltage.close();
        keySightDeviceVoltage16.close();
        keySightDeviceNtc.close();
    }

    public TestResults driveDevices(String visualPartNumber, double cirTemp, JTextArea mDataView, boolean production) {
        double Electricity; //电流
        double Voltage; //电压
        double voltagev16;  //电压
        double NTC; //NTC电阻

        logger.debug("Before reading " + visualPartNumber + "'s data, let's sleep 1 second...");
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //分别发送指令通知六位半需要读取数据
        keySightDeviceElectricity.writeCmd("READ?");
        keySightDeviceVoltage.writeCmd("READ?");
        keySightDeviceVoltage16.writeCmd("READ?");
        keySightDeviceNtc.writeCmd("READ?");

        Electricity = Double.valueOf(keySightDeviceElectricity.readResult());
        Voltage = Double.valueOf(keySightDeviceVoltage.readResult());
        voltagev16 = Double.valueOf(keySightDeviceVoltage16.readResult());
        NTC = Double.valueOf(keySightDeviceNtc.readResult());

        return saveTestResult(visualPartNumber, Electricity, Voltage, voltagev16, NTC, cirTemp, mDataView, production);
    }

    public TestResults pseudoDriveDevices(String visualPartNumber, double cirTemp) {
        double Electricity = Math.random() / 10 + 4.3; //电流
        double Voltage = Math.random() / 100 + 0.32; //电压
        double voltagev16 = Math.random() / 100 + 0.32;  //电压
        double NTC = 10000 * (1 + Math.random() / 100); //NTC电阻
        return new TestResults();
        /*return saveTestResult(visualPartNumber, Electricity, Voltage, voltagev16, NTC, cirTemp);*/
    }

    private TestResults saveTestResult(String visualPartNumber, double Electricity, double Voltage, double voltagev16, double NTC, double cirTemp, JTextArea mDataView, boolean production) {
        TestResults result = new TestResults();

        result.setVisualPartNumber(visualPartNumber);
        result.setMainCurr(Electricity);
        result.setV25(Voltage);
        result.setV16(voltagev16);
        if (Math.abs(Electricity) > 1) {
            result.setR25(Voltage / Electricity * 1000000);  // 25 rt
            result.setR16(voltagev16 / Electricity * 1000000);  // 16 rw
        } else {
            result.setR25(2000);  // 25 rt
            result.setR16(2000);  // 16 rw
        }

        /* 2020-11-02 修改内控参数，与界面一致 */
        double up = TestConstant.RESISTOR_EXP * (1 + TestConstant.RESISTOR_TOLERANCE);
        double low = TestConstant.RESISTOR_EXP * (1 - TestConstant.RESISTOR_TOLERANCE);
        if ((result.getR25() < (up - 0.5) && result.getR25() > (low + 0.5))
                && (result.getR16() < up && result.getR16() > low))
        /*if (((Math.abs(result.getR25() - TestConstant.RESISTOR_EXP) / TestConstant.RESISTOR_EXP) <= TestConstant.RESISTOR_TOLERANCE)
                && ((Math.abs(result.getR16() - TestConstant.RESISTOR_EXP) / TestConstant.RESISTOR_EXP) <= TestConstant.RESISTOR_TOLERANCE))*/ {
            result.setResistorOK(true);
        } else {
            result.setResistorOK(false);
        }

        if (NTC > TempCalculator.AbHigh) {
            result.setNtcR(-2000);
        } else {
            result.setNtcR(NTC);
        }
        result.setNtcT(TempCalculator.QCalTemp(NTC));
        if (Math.abs(TempCalculator.QCalTemp(NTC) - cirTemp) <= TestConstant.TEMP_GAP) {
            result.setNTC_OK(true);
        } else {
            result.setNTC_OK(false);
        }

        result.setTestTime(Calendar.getInstance().getTime());

        logger.debug("ProRecords save: " + result.toString());
        try {
            TestResultsRepo rRepo = SpringContext.getBean(TestResultsRepo.class);
            rRepo.save(result);
        } catch (Exception e) {
            mDataView.append("数据库异常，请检查连接！");
            logger.error("Save ProRecordsRepo to DB error. ", e);
            if (production) {
                EolStatus.getInstance().setEolStatus("Error");
            }
        }

        return result;
    }

    public double[] testZeroV() {
        double[] result = new double[2];

        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //分别发送指令通知六位半需要读取数据
        keySightDeviceVoltage.writeCmd("READ?");
        keySightDeviceVoltage16.writeCmd("READ?");

        result[0] = Double.valueOf(keySightDeviceVoltage.readResult());
        result[1] = Double.valueOf(keySightDeviceVoltage16.readResult());

        return result;
    }

    public ProRecords testThePart(String visualPartNumber, String resistorID, double cirTemp, JTextArea mDataView, boolean production) {
        double zerov[] = new double[2];

        // 获得TestResultsRepo以保存每次测试结果
        double r25 = 0;
        double r16 = 0;
        double rntc = 0;
        boolean judgeResistor = true;
        boolean judgeNTC = true;

        ProRecords thePart = new ProRecords();
        thePart.setVisualPartNumber(visualPartNumber);
        thePart.setResistorID(resistorID);
        thePart.setProDate(new Date());

        int times = 0;

        /* 2020-11-04 在开电源之前，测零漂 */
        logger.debug("开始测试零漂");
        while (times < TestConstant.ZEROV_TIMES) {
            zerov = testZeroV();
            if (Math.abs(zerov[0]) < TestConstant.ZEROV_TIMES || Math.abs(zerov[1]) < TestConstant.ZEROV_TIMES) {
                break;
            } else {
                times++;
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        logger.debug("零漂测试结束");
        thePart.setZerov25(zerov[0]);
        thePart.setZerov16(zerov[1]);

        NumberFormat ddf = NumberFormat.getNumberInstance();
        ddf.setMaximumFractionDigits(2);
        logger.debug("零漂25 = " + ddf.format(zerov[0] * 1000000) + "μV & 零漂16 = " + ddf.format(zerov[1] * 1000000) + "μV");

        /* 2020-11-05 判断条件：
         * 1. 2-5脚小于3μV 且 1-6脚小于9μV
         * 或者
         * 2. 2-5脚小于9μV 且 1-6脚小于3μV
         */
        if ((Math.abs(zerov[0]) < TestConstant.ZEROV_TIMES && Math.abs(zerov[1]) < 3 * TestConstant.ZEROV_TIMES)
                || (Math.abs(zerov[0]) < 3 * TestConstant.ZEROV_TIMES && Math.abs(zerov[1]) < TestConstant.ZEROV_TIMES)) {
            /* 2020-11-05 控制显示 */
            mDataView.append(DateUtil.formatInfo("零漂25 = " + ddf.format(zerov[0] * 1000000) + "μV & 零漂16 = "
                    + ddf.format(zerov[1] * 1000000) + "μV"));
            /* 2020-11-05 控制屏幕滚动 */
            mDataView.setCaretPosition(mDataView.getText().length());

            // 开电源
            TcpClient client = new TcpClient();
            client.open();

            // 循环Test_Time次
            // 按ResistorID和maskID，获得一次Run
            for (int i = 0; i < TestConstant.TEST_TIMES; i++) {
                TestResults oneTest = driveDevices(visualPartNumber, cirTemp, mDataView, production);
                r25 = r25 + oneTest.getR25();
                r16 = r16 + oneTest.getR16();
                rntc = rntc + oneTest.getNtcR();
                judgeResistor = judgeResistor && oneTest.getResistorOK();
                judgeNTC = judgeNTC && oneTest.getNTC_OK();
            }

            // 关电源
            client.close();

            // 获得25和R16、Rntc的平均值
            double avgR25 = r25 / TestConstant.TEST_TIMES;
            double avgR16 = r16 / TestConstant.TEST_TIMES;
            double avgRntc = rntc / TestConstant.TEST_TIMES;

            thePart.setR25(avgR25);
            thePart.setR16(avgR16);
            thePart.setRntc(avgRntc);
            thePart.setTntc(TempCalculator.QCalTemp(avgRntc));


            /* 2020-10-30 查原厂阻值 */
            double t = 100.0d;
            try {
                ShuntResistorsRepo repo = SpringContext.getBean(ShuntResistorsRepo.class);
                Optional<ShuntResistors> oneRow = repo.findById(resistorID);
                double facrv = oneRow.get().getRValue();
                t = Math.abs(avgR25 - facrv) / facrv;
                ddf.setMaximumFractionDigits(4);
                thePart.setComments(ddf.format(t * 100) + "%");
                mDataView.append(DateUtil.formatInfo("电阻偏差：" + ddf.format(t * 100) + "%"));
                /* 2020-11-02 控制屏幕滚动 */
                mDataView.setCaretPosition(mDataView.getText().length());
            } catch (Exception e) {
                String aa = "Cannot find factory resistor id " + resistorID + " in database.";
                thePart.setComments(aa);
                logger.warn(aa);
                logger.warn(e);
            }

            if (judgeResistor && judgeNTC && t <= 0.01) {
                thePart.setProCode(maintainQRCode(visualPartNumber, production));
            } else {
                thePart.setProCode(null);
            }
        } else {
            if (Math.abs(zerov[0]) < 3 * TestConstant.ZEROV_TIMES) {
                mDataView.append(DateUtil.formatInfo("零漂25 = " + ddf.format(zerov[0] * 1000000)));
            } else {
                mDataView.append(DateUtil.formatInfo("零漂25错误"));
            }
            if (Math.abs(zerov[1]) < 3 * TestConstant.ZEROV_TIMES) {
                mDataView.append(DateUtil.formatInfo("零漂16 = " + ddf.format(zerov[0] * 1000000)));
            } else {
                mDataView.append(DateUtil.formatInfo("零漂16错误"));
            }
            /* 2020-11-05 控制屏幕滚动 */
            mDataView.setCaretPosition(mDataView.getText().length());
        }

        try {
            ProRecordsRepo repo = SpringContext.getBean(ProRecordsRepo.class);
            repo.save(thePart);
        } catch (Exception e) {
            logger.error(e.getMessage());
            mDataView.append("数据库异常，请检查连接！");
            if (production) {
                EolStatus.getInstance().setEolStatus("Error");
            }
        }
        return thePart;
    }

    private String maintainQRCode(String visualPartNumber, boolean production) {
        String factoryID = null;
        if (visualPartNumber.startsWith("D")) {
            factoryID = TestConstant.SVW;
        } else if (visualPartNumber.startsWith("G")) {
            factoryID = TestConstant.FAW;
        } else {
            return null;
        }

        logger.debug("getLatestQRCodeByFactoryID start : " + factoryID);
        String nextBarCode = null;
        try {
            LatestQRCodeRepo repo = SpringContext.getBean(LatestQRCodeRepo.class);
            Optional<LatestQRCodes> oneRow = repo.findById(factoryID);
            String lastQR = oneRow.get().getLatestQRCode();

            String s = QRCodeGenerator.getSeqNumber(lastQR);
            nextBarCode = QRCodeGenerator.calQRCode(factoryID, s);

            LatestQRCodes lqrc = new LatestQRCodes();
            lqrc.setLatestQRCode(nextBarCode);
            lqrc.setCustomerPartNo(factoryID);
            repo.save(lqrc);
        } catch (Exception e) {
            logger.error("数据库无最新的QR Code --> " + factoryID, e);
            if (production) {
                EolStatus.getInstance().setEolStatus("Error");
            }
        }

        return nextBarCode;
    }
}
