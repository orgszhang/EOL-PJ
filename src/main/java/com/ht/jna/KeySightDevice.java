package com.ht.jna;

import com.alibaba.fastjson.JSONObject;
import com.ht.base.SpringContext;
import com.ht.entity.Devices;
import com.ht.repository.DevicesRepo;
import com.ht.utils.DateUtil;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;

public class KeySightDevice {
    private static final Log logger = LogFactory.getLog(KeySightDevice.class);

    protected KeySightVci KEYSIGHTINSTANCE;

    protected boolean isOpened = false;
    protected boolean isSetVol = false;
    protected boolean isSetEle = false;
    protected LongByReference defaultSession;
    protected LongByReference vipSession;
    protected LongByReference VI_ATTR_SUPPRESS_END_EN;
    protected LongByReference VI_ATTR_TERMCHAR_EN;

    public KeySightDevice() {
        KEYSIGHTINSTANCE = KeySightVci.KEYSIGHTINSTANCE;
    }

    public boolean open(String name, String ipIn) {
        if (isOpened) {
            return true;
        }

        String ip = ipIn;
        try {
            DevicesRepo repo = SpringContext.getBean(DevicesRepo.class);
            Optional<Devices> oneRow = repo.findById(name);
            ip = oneRow.get().getIpAddress();
        } catch (Exception e) {
            logger.warn("Cannot find IP address of " + name + " in database. Use the default configuration.");
            logger.warn(e);
        }

        try {
            defaultSession = new LongByReference(59005407);
            int result = KEYSIGHTINSTANCE.viOpenDefaultRM(defaultSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                // TODO: 界面上，此设备状态设为Red
                // TODO: 界面上，TextArea增加一行"不能与设备 + name + 建立通信"
                // TODO: 通知主控，设备有错
                return false;
            }
            vipSession = new LongByReference(0);
            String cmd = "TCPIP0::" + ip + "::inst0::INSTR";
            NativeLong a = new NativeLong(defaultSession.getValue());
            NativeLong b = new NativeLong(0);
            result = KEYSIGHTINSTANCE.viOpen(a, cmd, b, b, vipSession);
            if (result != KEYSIGHTINSTANCE.STATUS_OK) {
                // TODO: 界面上，此设备状态设为Red
                // TODO: 界面上，TextArea增加一行"不能找到设备 + name + IP"
                // TODO: 通知主控，设备有错
                return false;
            }
            logger.info("连接ip=" + ip + "的设备成功");

            // TODO: 界面上，此设备状态设为Green
            // TODO: 界面上，TextArea增加一行"设备 + DefaultDevicesConfig.eleName + 连接成功"
        } catch (Exception e) {
            logger.error(e);
            // TODO: 界面上，此设备状态设为Red
            // TODO: 通知主控，设备有错
        }
        isOpened = true;
        return true;
    }

    public boolean close() {
        // TODO: 退出远程模式失败，界面上要显示不？
        NativeLong a = new NativeLong(vipSession.getValue());
        int result = KEYSIGHTINSTANCE.viClose(a);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.error(result);
            return false;
        }

        // TODO: 退出远程模式失败，界面上要显示不？
        NativeLong b = new NativeLong(defaultSession.getValue());
        result = KEYSIGHTINSTANCE.viClose(b);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.error("KeySight退出远程模式失败");
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

    public Boolean writeCmd(String cmdStr, JTextArea mDataView, ThreadLocal<String> eolStatus, DataOutputStream dos) {
        // VI_ATTR_SUPPRESS_END_EN();
        // VI_ATTR_TERMCHAR_EN();
        NativeLong a = new NativeLong(vipSession.getValue());
        int result = KEYSIGHTINSTANCE.viPrintf(a, "%s\n", cmdStr);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.error("执行命令失败,result=" + result);
            // TODO: 界面上TEXTAREA上显示"'执行命令失败,result=' + result"
            mDataView.append(DateUtil.formatInfo("执行命令失败"));
            // TODO: 界面上此设备状态显示为Red
            eolStatus.set("RED");
            mDataView.append(DateUtil.formatInfo("设备状态: " + eolStatus.get()));
            // TODO: 通知主控，设备有错
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Error", "xxx");
            byte[] bytes = jsonObject.toJSONString().getBytes();
            try {
                dos.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public String readResult() {
        NativeLong a = new NativeLong(vipSession.getValue());
        Memory mem = new Memory(200);
        int result = KEYSIGHTINSTANCE.viScanf(a, "%t", mem);
        if (result != KEYSIGHTINSTANCE.STATUS_OK) {
            logger.error(result);
            // TODO: 界面上TEXTAREA上显示"'执行命令失败,result=' + result"
            // TODO: 界面上此设备状态显示为Red
            // TODO: 通知主控，设备有错
            return null;
        }
        return mem.getString(0);
    }
}