package com.ht.comm;


import com.alibaba.fastjson.JSONObject;
import com.ht.entity.EolStatus;
import com.ht.jna.KeySightManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;


public class NetPortListener {
    private static final Log logger = LogFactory.getLog(NetPortListener.class);

    ServerSocket server = null;
    /*JTextField codeField = null;
    JTextField qcField = null;
    JTextField temp = null;
    JTextField textFieldRt_R25 = null;
    JTextField textFieldRw_R16 = null; // Rw
    JTextField textFieldRntc_NTCRValue = null; // Rntc
    JTextField textFieldTemperature = null; //换算温度
    JLabel labelResultOne = null; //分流电阻结果
    JLabel labelResultTwo = null; //ntc结果
    JLabel labelQRCode = null;
    JTextArea mDataView = null;*/
    KeySightManager keySightManager;
    JSONObject allDataJsonObject;
    /*DataOutputStream dos;*/

    public NetPortListener(KeySightManager manager, JSONObject jsonObject) {
        this.keySightManager = manager;
        this.allDataJsonObject = jsonObject;

        /*this.codeField = (JTextField) jsonObject.get("visualPartNumber");
        this.qcField = (JTextField) jsonObject.get("textFieldResistorsID");
        this.temp = (JTextField) jsonObject.get("textFieldTemp");
        this.textFieldRt_R25 = (JTextField) jsonObject.get("textFieldRt_R25");
        this.textFieldRntc_NTCRValue = (JTextField) jsonObject.get("textFieldRntc_NTCRValue");
        this.textFieldRw_R16 = (JTextField) jsonObject.get("textFieldRw_R16");
        this.textFieldTemperature = (JTextField) jsonObject.get("textFieldTemperature");
        this.labelResultOne = (JLabel) jsonObject.get("labelResultOne");
        this.labelResultTwo = (JLabel) jsonObject.get("labelResultTwo");
        this.labelQRCode = (JLabel) jsonObject.get("labelQRCode");
        this.mDataView = (JTextArea) jsonObject.get("mDataView");*/

        String ipMainPLC = "192.168.10.80";
        int port = 8088;

        try {
            String[] ipStr = ipMainPLC.split("\\.");
            byte[] ipBuf = new byte[4];
            for (int i = 0; i < 4; i++) {
                ipBuf[i] = (byte) (Integer.parseInt(ipStr[i]) & 0xff);
            }

            InetAddress inetAddresd = InetAddress.getByAddress(ipBuf);
            server = new ServerSocket(port, 5, inetAddresd);

            // 如果能绑定端口，之前状态为Error，得修正为Ready；否则都是Error，出不来了
            String s = EolStatus.getInstance().getEolStatus();
            logger.info("主控PLC侦听端口状态：" + s);
            if ("Error".equals(s)) {
                EolStatus.getInstance().setEolStatus("Ready");
                logger.info("主控PLC侦听端口调整为Ready");
            }

            AcceptThread sendThread = new AcceptThread(manager, jsonObject, server);
            sendThread.start();
        } catch (Exception e) {
            EolStatus.getInstance().setEolStatus("Error"); // 端口都绑不上，肯定是错的
            logger.warn("主控PLC端口侦听异常: " + e.getMessage());
        }
    }

    public void close() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                logger.warn(e);
            }
        }
        server = null;
    }
}


