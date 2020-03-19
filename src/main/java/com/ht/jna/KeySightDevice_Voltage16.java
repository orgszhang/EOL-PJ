package com.ht.jna;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 16
 */
public class KeySightDevice_Voltage16 {
    private static final Log logger = LogFactory.getLog(KeySightDevice_Voltage16.class);

    private KeySightVci_Voltage KEYSIGHTINSTANCE;

    private boolean isOpened = false;
    private boolean isSetVol = false;
    private boolean isSetEle = false;
    LongByReference defaultSession;
    LongByReference vipSession;
    private LongByReference VI_ATTR_SUPPRESS_END_EN;
    private LongByReference VI_ATTR_TERMCHAR_EN;

    public KeySightDevice_Voltage16() {
        KEYSIGHTINSTANCE = KeySightVci_Voltage.KEYSIGHTINSTANCE;
    }

    public boolean open() {
        if (isOpened) {
            return true;
        }

        try {
            /*String path = "keysight.xlsx";
            InputStream inputStream = null;
            inputStream = new FileInputStream(path);
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheet("IP");
            String curIp = sheet.getRow(3).getCell(1).getStringCellValue();  //*/
            String curIp = "169.254.210.3";
            logger.info("连接 1-6 的设备. IP..." + curIp);
            defaultSession = new LongByReference(59005407);
            int result = KEYSIGHTINSTANCE.viOpenDefaultRM(defaultSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                logger.info("连接 1-6 的设备....");
                return false;
            }
            vipSession = new LongByReference(0);
            String cmd = "TCPIP0::" + curIp + "::inst0::INSTR";
            NativeLong a = new NativeLong(defaultSession.getValue());
            NativeLong b = new NativeLong(0);
            result = KEYSIGHTINSTANCE.viOpen(a, cmd, b, b, vipSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                logger.info("连接 1-6 的设备失败");
                return false;
            }
            System.out.println("连接ip=" + curIp + "的设备成功");
        } catch (Exception e) {
            logger.error(e);
        }
        isOpened = true;
        return true;
    }

    public boolean close() {
        NativeLong a = new NativeLong(vipSession.getValue());
        int result = KEYSIGHTINSTANCE.viClose(a);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.info(result);
            return false;
        }
        NativeLong b = new NativeLong(defaultSession.getValue());
        result = KEYSIGHTINSTANCE.viClose(b);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.info("KeySight退出远程模式失败");
            return false;
        }
        logger.info("KeySight退出远程模式成功");
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

    public Boolean writeCmd(String cmdStr) {
        // VI_ATTR_SUPPRESS_END_EN();
        // VI_ATTR_TERMCHAR_EN();
        NativeLong a = new NativeLong(vipSession.getValue());
        int result = KEYSIGHTINSTANCE.viPrintf(a, "%s\n", cmdStr);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.warn("执行命令失败,result=" + result);
        }
        return true;
    }


    public boolean setVolCONF() {
        if (isSetVol) {
            return true;
        }
        // if (writeCmd("FUNC \"VOLT:DC\"") && writeCmd("VOLT:RANG 10")) {
        if (writeCmd("CONF:VOLT 0.1") && writeCmd("CONF:VOLT 0.1")) {
            System.out.println("设置为DC电压模式");
            isSetVol = true;
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                logger.error(e);
            }
            return true;
        }
        return false;
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

}