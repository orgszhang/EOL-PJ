package com.ht.jna;

import com.ht.entity.EolStatus;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.DataOutputStream;


public class KeySightDevice_Electricity {
    private static final Log logger = LogFactory.getLog(KeySightDevice_Electricity.class);
    private KeySightVci_Electricity KEYSIGHTINSTANCE;

    private boolean isOpened = false;
    private boolean isSetVol = false;
    private boolean isSetEle = false;
    LongByReference defaultSession;
    LongByReference vipSession;
    private LongByReference VI_ATTR_SUPPRESS_END_EN;
    private LongByReference VI_ATTR_TERMCHAR_EN;

    public KeySightDevice_Electricity() {
        KEYSIGHTINSTANCE = KeySightVci_Electricity.KEYSIGHTINSTANCE;
    }


    public boolean open(JTextArea mDataView) {
        if (isOpened) {
            return true;
        }
//        try {
//            Socket s = new Socket("192.168.1.110",5024);
//        } catch (IOException e) {
//            log.error("连接设备失败");
//            return false;
//        }
//        defaultSession = new LongByReference(getStartInf());
        try {
            /*String path = "D:\\台架\\keysight.xlsx";
            InputStream inputStream = null;
            inputStream = new FileInputStream(path);
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheet("IP");
            String eleIp = sheet.getRow(0).getCell(1).getStringCellValue();*/
            String eleIp = "169.254.210.1";
            defaultSession = new LongByReference(59005407);
            int result = KEYSIGHTINSTANCE.viOpenDefaultRM(defaultSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                mDataView.append("KeySightDevice_Electricity open  error ...");
                return false;
            }
            vipSession = new LongByReference(0);
            String cmd = "TCPIP0::" + eleIp + "::inst0::INSTR";
//        String cmd = "TCPIP0::192.168.0.120::5024::SOCKET";
//        String cmd = "USB0::0x2A8D::0x1301::MY59000220::0::INSTR";
            NativeLong a = new NativeLong(defaultSession.getValue());
            NativeLong b = new NativeLong(0);
            result = KEYSIGHTINSTANCE.viOpen(a, cmd, b, b, vipSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                return false;
            }
            System.out.println("连接ip=" + eleIp + "的设备成功");
        } catch (Exception e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }
        isOpened = true;
        return true;
    }

    public boolean close() {
        NativeLong a = new NativeLong(vipSession.getValue());
        int result = KEYSIGHTINSTANCE.viClose(a);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            System.out.println(result);
            return false;
        }
        NativeLong b = new NativeLong(defaultSession.getValue());
        result = KEYSIGHTINSTANCE.viClose(b);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            System.out.println("KeySight退出远程模式失败");
            return false;
        }
        System.out.println("KeySight退出远程模式成功");
        return true;
    }


    public Boolean VI_ATTR_SUPPRESS_END_EN() {
        VI_ATTR_SUPPRESS_END_EN = new LongByReference(0x3fff0036);
        NativeLong a = new NativeLong(vipSession.getValue());
        NativeLong end = new NativeLong(VI_ATTR_SUPPRESS_END_EN.getValue());
        int result = KEYSIGHTINSTANCE.viSetAttribute(a, end, "VI_TRUE");
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            return false;
        }
        return true;
    }

    public Boolean VI_ATTR_TERMCHAR_EN() {
        VI_ATTR_TERMCHAR_EN = new LongByReference(0x3fff0038);
        NativeLong a = new NativeLong(vipSession.getValue());
        NativeLong end = new NativeLong(VI_ATTR_TERMCHAR_EN.getValue());
        int result = KEYSIGHTINSTANCE.viSetAttribute(a, end, "VI_TRUE");
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            return false;
        }
        return true;
    }

    public Boolean writeCmd(String cmdStr, JTextArea mDataView, EolStatus eolStatus, DataOutputStream dos) {
//        VI_ATTR_SUPPRESS_END_EN();
//        VI_ATTR_TERMCHAR_EN();
        NativeLong a = new NativeLong(vipSession.getValue());
        int result = KEYSIGHTINSTANCE.viPrintf(a, "%s\n", cmdStr);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            System.out.println("I - 执行命令" + cmdStr + "失败，result=" + result);
            mDataView.append("I - 执行命令" + cmdStr + "失败，result=" + result);
        }
        return true;
    }

    public String readResult() {
        NativeLong a = new NativeLong(vipSession.getValue());
        Memory mem = new Memory(200);
        int result = KEYSIGHTINSTANCE.viScanf(a, "%t", mem);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            System.out.println(result);
            return null;
        }
        return mem.getString(0);
    }

    public boolean setEleCONF() {
        if (isSetEle) {
            return true;
        }
        // writeCmd("*RST");
        // writeCmd("*IDN?");
        String s = readResult();
        System.out.println(s);

        if (writeCmd("CONF:CURR:DC", null, null, null) && writeCmd("SENS:CURR:DC:TERM 10", null, null, null)) {
            System.out.println("设置为DC电流模式");
            isSetEle = true;
            return true;
        }
        return false;
    }

}