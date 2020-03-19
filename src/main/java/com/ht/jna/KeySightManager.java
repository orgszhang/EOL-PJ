package com.ht.jna;


import com.ht.entity.ProRecords;
import com.ht.entity.TestResults;

import com.ht.swing.TestConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;
import java.util.Date;

public class KeySightManager {
    private static final Log logger = LogFactory.getLog(KeySightManager.class);

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

        if ((Math.abs(r25 - 75)/ 75) <= 0.03) {
            result.setResistorOK(true);
        } else {
            result.setResistorOK(false);
        }

        result.setNtcR(NTC);
        result.setNtcT(QCalTemp(NTC));
        if (Math.abs(QCalTemp(NTC) - cirTemp) <= 5) {
            result.setNTC_OK(true);
        } else {
            result.setNTC_OK(false);
        }

        result.setTestTime(Calendar.getInstance().getTime());


        return result;
    }

    public ProRecords testThePart(String visualPartNumber, double cirTemp, String resistorID) {
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
        for (int i = 0; i < 10; i++) {
            TestResults oneTest = pseudoDriveDevices(visualPartNumber, cirTemp);
            // TestResults oneTest = driveDevices(visualPartNumber, cirTemp);
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
        thePart.setTntc(QCalTemp(avgRntc));

        if (judgeResistor && judgeNTC) {
            thePart.setProCode(getQRcode());
        } else {
            thePart.setProCode(null);
        }
        thePart.setProDate(new Date());

        thePart.setComments(resistorID);

        return thePart;
    }

    private double QCalTemp(double d) {
        return (25 * (1 - Math.random() / 100));
    }

    private String getQRcode() {
        String[] QRCodes = new String[] {
                "#11D222333  000###*AAA 999 000001D*#",
                "#11D222333  000###*AAA 999 000509T*#",
                "#11D222333  000###*AAA 999 078903X*#",
                "#11D222333  000###*AAA 999 030678Y*#",
                "#11D222333  000###*AAA 999 129753V*#",

                "#11G222333  000###*AAA 999 892437M*#",
                "#11G222333  000###*AAA 999 125894W*#",
                "#11G222333  000###*AAA 999 751369O*#",
                "#11G222333  000###*AAA 999 882319C*#",
                "#11G222333  000###*AAA 999 236578P*#",
        };

        int i = (int)(Math.random() * 10);        ;
        return QRCodes[i];
    }
}
