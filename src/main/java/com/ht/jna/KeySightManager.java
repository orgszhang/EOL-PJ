package com.ht.jna;

import com.ht.base.SpringContext;
import com.ht.entity.LatestQRCodes;
import com.ht.entity.ProRecords;
import com.ht.entity.TestResults;
import com.ht.repository.LatestQRCodeRepo;
import com.ht.repository.ProRecordsRepo;
import com.ht.repository.TestResultsRepo;
import com.ht.utils.QRCodeGenerator;
import com.ht.utils.TempCalculator;
import com.ht.utils.TestConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.DataOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public class KeySightManager {
    private static final Log logger = LogFactory.getLog(KeySightManager.class);

    KeySightDevice_Voltage keySightDeviceVoltage;
    KeySightDevice_Voltage16 keySightDeviceVoltage16;
    KeySightDevice_Electricity keySightDeviceElectricity;
    KeySightDevice_NTC keySightDeviceNtc;

    public static void main(String[] args) {
        double cirTemp = 15; // 环境温度
        String resistorID = "20203010282";
        String key = "11D915743";

        KeySightManager manager = new KeySightManager();
        manager.initDevices();
 /*       TestResults result = manager.driveDevices(resistorID, cirTemp, mDataView, eolStatus);*/
        manager.closeDivices();
    }

    public void initDevices() {
        //每一个六位半对应一个class
        keySightDeviceVoltage = new KeySightDevice_Voltage();
        keySightDeviceVoltage16 = new KeySightDevice_Voltage16();
        keySightDeviceElectricity = new KeySightDevice_Electricity();
        keySightDeviceNtc = new KeySightDevice_NTC();

        //分别打开三台设备
        keySightDeviceVoltage.open();
        keySightDeviceVoltage16.open();
        keySightDeviceElectricity.open();
        keySightDeviceNtc.open();

        //分别设置设备的量程
        keySightDeviceVoltage.setVolCONF();
        keySightDeviceVoltage16.setVolCONF();
        keySightDeviceElectricity.setEleCONF();
        keySightDeviceNtc.setRCONF();
    }

    public void closeDivices() {
        //读取完成后关闭设备
        keySightDeviceVoltage.close();
        keySightDeviceVoltage16.close();
        keySightDeviceElectricity.close();
        keySightDeviceNtc.close();
    }

    public TestResults driveDevices(String visualPartNumber, double cirTemp, JTextArea mDataView, ThreadLocal<String> eolStatus, DataOutputStream dos) {
        double Electricity; //电流
        double Voltage; //电压
        double voltagev16;  //电压
        double NTC; //NTC电阻

        logger.info("Before reading " + visualPartNumber + "'s data, let's sleep 1 second...");
        try {
            Thread.sleep(1 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //分别发送指令通知六位半需要读取数据
        keySightDeviceVoltage.writeCmd("READ?", mDataView, eolStatus, dos);
        keySightDeviceVoltage16.writeCmd("READ?", mDataView, eolStatus, dos);
        keySightDeviceElectricity.writeCmd("READ?", mDataView, eolStatus, dos);
        keySightDeviceNtc.writeCmd("READ?", mDataView, eolStatus, dos);

        Voltage = Double.valueOf(keySightDeviceVoltage.readResult());
        voltagev16 = Double.valueOf(keySightDeviceVoltage16.readResult());
        Electricity = Double.valueOf(keySightDeviceElectricity.readResult());
        NTC = Double.valueOf(keySightDeviceNtc.readResult());

        return saveTestResult(visualPartNumber, Electricity, Voltage, voltagev16, NTC, cirTemp);
    }

    public TestResults pseudoDriveDevices(String visualPartNumber, double cirTemp) {
        double Electricity = Math.random()/10 + 4.3; //电流
        double Voltage = Math.random() / 100 + 0.32; //电压
        double voltagev16 = Math.random() /100 + 0.32;  //电压
        double NTC = 10000 * (1 + Math.random() / 100); //NTC电阻

        return saveTestResult(visualPartNumber, Electricity, Voltage, voltagev16, NTC, cirTemp);
    }

    private TestResults saveTestResult(String visualPartNumber, double Electricity, double Voltage, double voltagev16, double NTC, double cirTemp) {
        TestResults result = new TestResults();

        result.setVisualPartNumber(visualPartNumber);
        result.setMainCurr(Electricity);
        result.setV25(Voltage);
        double r25 = Voltage / Electricity * 1000;
        result.setR25(r25);  // 25 rt
        result.setV16(voltagev16);
        result.setR16(voltagev16 / Electricity * 1000);  // 16 rw

        if ((Math.abs(r25 - TestConstant.RESISTOR_EXP)/ TestConstant.RESISTOR_EXP) <= TestConstant.RESISTOR_TOLERANCE) {
            result.setResistorOK(true);
        } else {
            result.setResistorOK(false);
        }

        result.setNtcR(NTC);
        result.setNtcT(TempCalculator.QCalTemp(NTC));
        if (Math.abs(TempCalculator.QCalTemp(NTC) - cirTemp) <= TestConstant.TEMP_GAP) {
            result.setNTC_OK(true);
        } else {
            result.setNTC_OK(false);
        }

        result.setTestTime(Calendar.getInstance().getTime());

        logger.info("ProRecords save: " + result.toString());
        try {
            TestResultsRepo rRepo = SpringContext.getBean(TestResultsRepo.class);
            rRepo.save(result);
        } catch (Exception e) {
            logger.error("Save ProRecordsRepo to DB error. ", e);
        }

        return result;
    }

    public ProRecords testThePart(String visualPartNumber, double cirTemp, String resistorID, JTextArea mDataView, ThreadLocal<String> eolStatus, DataOutputStream dos) {
        ProRecords thePart = new ProRecords();
        thePart.setVisualPartNumber(visualPartNumber);

        // 获得TestResultsRepo以保存每次测试结果
        double r25=0;
        double r16 = 0;
        double rntc=0;
        boolean judgeResistor = true;
        boolean judgeNTC = true;

        // 循环Test_Time次
        // 按ResistorID和maskID，获得一次Run
        for (int i = 0; i < TestConstant.TEST_TIMES; i++) {
            TestResults oneTest = pseudoDriveDevices(visualPartNumber, cirTemp);
            // TestResults oneTest=  driveDevices(visualPartNumber, cirTemp,mDataView,eolStatus,dos);
            r25 = r25 + oneTest.getR25();
            r16 = r16 + oneTest.getR16();
            rntc = rntc + oneTest.getNtcR();
            judgeResistor = judgeResistor && oneTest.getResistorOK();
            judgeNTC = judgeNTC && oneTest.getNTC_OK();
        }

        // 获得25和R16、Rntc的平均值
        double avgR25 = r25 / TestConstant.TEST_TIMES;
        double avgR16 = r16 / TestConstant.TEST_TIMES;
        double avgRntc = rntc / TestConstant.TEST_TIMES;

        thePart.setR25(avgR25);
        thePart.setR16(avgR16);
        thePart.setRntc(avgRntc);
        thePart.setTntc(TempCalculator.QCalTemp(avgRntc));

        if (judgeResistor && judgeNTC) {
            thePart.setProCode(maintainQRCode(visualPartNumber));
        } else {
            thePart.setProCode(null);
        }
        thePart.setProDate(new Date());

        thePart.setComments(resistorID);

        ProRecordsRepo repo = SpringContext.getBean(ProRecordsRepo.class);
        repo.save(thePart);

        return thePart;
    }

    private String maintainQRCode(String visualPartNumber) {
        String factoryID = null;
        if (visualPartNumber.startsWith("D")) {
            factoryID = TestConstant.SVW;
        } else if (visualPartNumber.startsWith("G")) {
            factoryID = TestConstant.FAW;
        } else {
            return null;
        }

        logger.info("getLatestQRCodeByFactoryID start : " + factoryID);
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
            logger.error("Cannot find the latest QR code --> " + factoryID, e);
        }

        return nextBarCode;
    }
}
