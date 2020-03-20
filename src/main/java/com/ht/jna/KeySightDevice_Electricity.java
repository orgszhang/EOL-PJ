package com.ht.jna;

import com.ht.base.SpringContext;
import com.ht.entity.Devices;
import com.ht.repository.DevicesRepo;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Optional;

public class KeySightDevice_Electricity {
    private static final Log logger = LogFactory.getLog(KeySightDevice_Electricity.class);
    LongByReference defaultSession;
    LongByReference vipSession;
    private KeySightVci KEYSIGHTINSTANCE;
    private boolean isOpened = false;
    private boolean isSetVol = false;
    private boolean isSetEle = false;
    private LongByReference VI_ATTR_SUPPRESS_END_EN;
    private LongByReference VI_ATTR_TERMCHAR_EN;

    public KeySightDevice_Electricity() {
        // KEYSIGHTINSTANCE = KeySightVci_Electricity.KEYSIGHTINSTANCE;
        KEYSIGHTINSTANCE = KeySightVci.KEYSIGHTINSTANCE;
    }

    public boolean open() {
        if (isOpened) {
            return true;
        }

        // try {
        //     Socket s = new Socket("192.168.1.110",5024);
        // } catch (IOException e) {
        //     log.error("连接设备失败");
        //     return false;
        // }
        // defaultSession = new LongByReference(getStartInf());

        try {
            /*****
             从数据库获得IP地址
             界面上不能设置IP，以后要改，就直接改数据库
             *****/
            String eleIp = DefaultDevicesConfig.eleIP;
            try {
                DevicesRepo repo = SpringContext.getBean(DevicesRepo.class);
                Optional<Devices> device = repo.findById(DefaultDevicesConfig.eleName);
                eleIp = device.get().getIpAddress();
            } catch (Exception e) {
                logger.error(e);
            }

            defaultSession = new LongByReference(59005407);
            int result = KEYSIGHTINSTANCE.viOpenDefaultRM(defaultSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                return false;
            }
            vipSession = new LongByReference(0);
            String cmd = "TCPIP0::" + eleIp + "::inst0::INSTR";
            NativeLong a = new NativeLong(defaultSession.getValue());
            NativeLong b = new NativeLong(0);
            result = KEYSIGHTINSTANCE.viOpen(a, cmd, b, b, vipSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                return false;
            }
            logger.info("连接ip=" + eleIp + "的设备成功");

            // TODO: 界面上，此设备状态设为Green
            // TODO: 界面上，TextArea增加一行"电流万用表连接成功"
        } catch (Exception e) {
            logger.error(e);
            // TODO: 界面上，此设备状态设为Red
            // TODO: 通知主控，设备有错
        }
        isOpened = true;
        return true;
    }

    public boolean close() {
        NativeLong a = new NativeLong(vipSession.getValue());
        int result = KEYSIGHTINSTANCE.viClose(a);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.warn(result);
            return false;
        }
        NativeLong b = new NativeLong(defaultSession.getValue());
        result = KEYSIGHTINSTANCE.viClose(b);

        // TODO: 退出远程模式失败，界面上要显示不？
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.warn("KeySight退出远程模式失败");
            return false;
        }

        logger.info("KeySight退出远程模式成功");
        // TODO: 界面上TEXTAREA上显示"KeySight退出远程模式成功"
        // TODO: 界面上此设备状态显示为"--"
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
            logger.warn("执行命令失败,result=" + result);
            // TODO: 界面上TEXTAREA上显示"'执行命令失败,result=' + result"
            // TODO: 界面上此设备状态显示为Red
        }
        return true;
    }

    public String readResult() {
        NativeLong a = new NativeLong(vipSession.getValue());
        Memory mem = new Memory(200);
        int result = KEYSIGHTINSTANCE.viScanf(a, "%t", mem);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.info(result);
            // TODO: 界面上TEXTAREA上显示"'执行命令失败,result=' + result"
            // TODO: 界面上此设备状态显示为Red
            return null;
        }
        return mem.getString(0);
    }

    public boolean setEleCONF() {
        if (isSetEle) {
            return true;
        }
        if (writeCmd("CONF:CURR 3") && writeCmd("CURR:DC:RANG 3")) {
            logger.info("设置为DC电流模式");
            isSetEle = true;
            return true;
        }
        return false;
    }
}