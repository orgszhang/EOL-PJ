package com.ht.jna;

import com.ht.base.SpringContext;
import com.ht.entity.Devices;
import com.ht.entity.EolStatus;
import com.ht.repository.DevicesRepo;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.DataOutputStream;
import java.util.Optional;

@Slf4j
public class KeySightDevice_Voltage {
    private static final Log logger = LogFactory.getLog(KeySightDevice_Voltage.class);
    private KeySightVci_Voltage KEYSIGHTINSTANCE;

    private boolean isOpened = false;
    private boolean isSetVol = false;
    private boolean isSetEle = false;
    LongByReference defaultSession;
    LongByReference vipSession;
    private LongByReference VI_ATTR_SUPPRESS_END_EN;
    private LongByReference VI_ATTR_TERMCHAR_EN;

    String curIp = "169.254.210.2";

    public KeySightDevice_Voltage() {
        KEYSIGHTINSTANCE = KeySightVci_Voltage.KEYSIGHTINSTANCE;
    }


    public boolean open(JTextArea mDataView) {
        logger.debug("KeySightDevice " + curIp + " open ...");
        if (isOpened) {
            return true;
        }

        try {
            defaultSession = new LongByReference(59005407);
            int result = KEYSIGHTINSTANCE.viOpenDefaultRM(defaultSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                mDataView.append("KeySightDevice " + curIp + " open  error ...");
                return false;
            }
            vipSession = new LongByReference(0);
            String cmd = "TCPIP0::" + curIp + "::inst0::INSTR";
            // String cmd = "TCPIP0::192.168.0.120::5024::SOCKET";
            // String cmd = "USB0::0x2A8D::0x1301::MY59000220::0::INSTR";
            NativeLong a = new NativeLong(defaultSession.getValue());
            NativeLong b = new NativeLong(0);
            result = KEYSIGHTINSTANCE.viOpen(a, cmd, b, b, vipSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                return false;
            }
            logger.info("连接ip=" + curIp + "的设备成功");
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
            logger.warn("KeySight" + curIp + "退出远程模式失败");
            return false;
        }
        logger.info("KeySight" + curIp + "退出远程模式成功");
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
            logger.warn(curIp + " - 执行命令" + cmdStr + "失败，result = " + result);
        }
        return true;
    }


    public boolean setVolCONF() {
        if (isSetVol) {
            return true;
        }
        // if (writeCmd("FUNC \"VOLT:DC\"") && writeCmd("VOLT:RANG 10")) {
        if (writeCmd("CONF:VOLT:DC 0.1") && writeCmd("CONF:VOLT 0.1")) {
            logger.info("设置" + curIp + "为DC电压模式");

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                logger.error(e);
            }

            isSetVol = true;
            return true;
        }
        return false;
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

}