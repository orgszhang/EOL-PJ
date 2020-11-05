package com.ht.jna;

import com.ht.utils.TestConstant;

import javax.swing.*;
import java.text.NumberFormat;

public class TestZerov {
    KeySightDevice_Voltage keySightDeviceVoltage;
    KeySightDevice_Voltage16 keySightDeviceVoltage16;

    public static void main(String[] args) {
        TestZerov zp = new TestZerov();
        zp.initDevices(null);
        zp.testThePart();
        zp.closeDivices();
    }

    public void initDevices(JTextArea mDataView) {
        //每一个六位半对应一个class
        keySightDeviceVoltage = new KeySightDevice_Voltage();
        keySightDeviceVoltage16 = new KeySightDevice_Voltage16();

        //分别打开三台设备
        keySightDeviceVoltage.open(mDataView);
        keySightDeviceVoltage16.open(mDataView);

        //分别设置设备的量程
        keySightDeviceVoltage.setVolCONF();
        keySightDeviceVoltage16.setVolCONF();
    }

    public void closeDivices() {
        //读取完成后关闭设备
        keySightDeviceVoltage.close();
        keySightDeviceVoltage16.close();
    }
    public void testThePart() {
        // 循环Test_Time次
        // 按ResistorID和maskID，获得一次Run
        for (int i = 0; i < TestConstant.TEST_TIMES; i++) {
            driveDevices();
        }
    }

    public void driveDevices() {
        double Electricity; //电流
        double Voltage; //电压
        double voltagev16;  //电压
        double NTC; //NTC电阻

        // System.out.println("Before reading " + visualPartNumber + "'s data, let's sleep 1 second...");
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //分别发送指令通知六位半需要读取数据
        // keySightDeviceElectricity.writeCmd("READ?");
        keySightDeviceVoltage.writeCmd("READ?");
        keySightDeviceVoltage16.writeCmd("READ?");
        // keySightDeviceNtc.writeCmd("READ?");

        // Electricity = Double.valueOf(keySightDeviceElectricity.readResult());
        Voltage = Double.valueOf(keySightDeviceVoltage.readResult());
        voltagev16 = Double.valueOf(keySightDeviceVoltage16.readResult());
        // NTC = Double.valueOf(keySightDeviceNtc.readResult());

        NumberFormat ddf = NumberFormat.getNumberInstance();
        ddf.setMaximumFractionDigits(10);


        System.out.println("voltage25 = " + ddf.format(Voltage) + "; voltagev16 = " + ddf.format(voltagev16));
        // return saveTestResult(visualPartNumber, Electricity, Voltage, voltagev16, NTC, cirTemp, mDataView, production);
    }
}
