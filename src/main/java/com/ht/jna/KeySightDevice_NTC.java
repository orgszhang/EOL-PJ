package com.ht.jna;

import com.ht.base.SpringContext;
import com.ht.entity.Devices;
import com.ht.entity.EolStatus;
import com.ht.repository.DevicesRepo;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.DataOutputStream;
import java.util.Optional;

public class KeySightDevice_NTC {
    private static final Log logger = LogFactory.getLog(KeySightDevice_NTC.class);
    private KeySightVic_NTC KEYSIGHTINSTANCE;

    private boolean isOpened = false;
    private boolean isSetVol = false;
    private boolean isSetEle = false;
    LongByReference defaultSession;
    LongByReference vipSession;
    private LongByReference VI_ATTR_SUPPRESS_END_EN;
    private LongByReference VI_ATTR_TERMCHAR_EN;

    String rIp = "169.254.210.4";

    public KeySightDevice_NTC() {
        KEYSIGHTINSTANCE = KeySightVic_NTC.KEYSIGHTNTC;
    }


    public boolean open(JTextArea mDataView) {
        if (isOpened) {
            return true;
        }

        try {
            defaultSession = new LongByReference(59005407);
            int result = KEYSIGHTINSTANCE.viOpenDefaultRM(defaultSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                mDataView.append("KeySightDevice " + rIp + "open  error ...");
                return false;
            }
            vipSession = new LongByReference(0);
            String cmd = "TCPIP0::" + rIp + "::inst0::INSTR";
            // String cmd = "TCPIP0::192.168.0.120::5024::SOCKET";
            // String cmd = "USB0::0x2A8D::0x1301::MY59000220::0::INSTR";
            NativeLong a = new NativeLong(defaultSession.getValue());
            NativeLong b = new NativeLong(0);
            result = KEYSIGHTINSTANCE.viOpen(a, cmd, b, b, vipSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                return false;
            }
            logger.info("连接ip=" + rIp + "的设备成功");
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
            logger.debug(result);
            return false;
        }
        NativeLong b = new NativeLong(defaultSession.getValue());
        result = KEYSIGHTINSTANCE.viClose(b);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.warn("KeySight" + rIp + "退出远程模式失败");
            return false;
        }
        logger.info("KeySight" + rIp + "退出远程模式成功");
        return true;
    }


    public Boolean VI_ATTR_SUPPRESS_END_EN() {
        VI_ATTR_SUPPRESS_END_EN = new LongByReference(0x3fff0036);
        NativeLong a = new NativeLong(vipSession.getValue());
        NativeLong end = new NativeLong(VI_ATTR_SUPPRESS_END_EN.getValue());
        int result = KEYSIGHTINSTANCE.viSetAttribute(a, end, "VI_TRUE");
        return result == KEYSIGHTINSTANCE.STATUS_OK;
    }

    public Boolean VI_ATTR_TERMCHAR_EN() {
        VI_ATTR_TERMCHAR_EN = new LongByReference(0x3fff0038);
        NativeLong a = new NativeLong(vipSession.getValue());
        NativeLong end = new NativeLong(VI_ATTR_TERMCHAR_EN.getValue());
        int result = KEYSIGHTINSTANCE.viSetAttribute(a, end, "VI_TRUE");
        return result == KEYSIGHTINSTANCE.STATUS_OK;
    }

    public Boolean writeCmd(String cmdStr) {
        // VI_ATTR_SUPPRESS_END_EN();
        // VI_ATTR_TERMCHAR_EN();
        NativeLong a = new NativeLong(vipSession.getValue());
        int result = KEYSIGHTINSTANCE.viPrintf(a, "%s\n", cmdStr);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.warn(rIp + "- 执行命令" + cmdStr + "失败，result = " + result);
        }
        return true;
    }

    public String readResult() {
        NativeLong a = new NativeLong(vipSession.getValue());
        Memory mem = new Memory(200);
        int result = KEYSIGHTINSTANCE.viScanf(a, "%t", mem);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.debug(result);
            return null;
        }
        return mem.getString(0);
    }


    public boolean setRCONF() {
        if (writeCmd("CONF:RES 1000000") && writeCmd("CONF:RESistance 1000000")) {
            logger.info("设置" + rIp + "为电阻模式");
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }
}