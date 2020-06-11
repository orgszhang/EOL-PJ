package com.ht.comm;

import com.alibaba.fastjson.JSONObject;
import com.ht.jna.KeySightManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.ServerSocket;
import java.net.Socket;

public class AcceptThread extends Thread {
    private static final Log logger = LogFactory.getLog(AcceptThread.class);

    KeySightManager keySightManager;
    JSONObject allDataJsonObject;
    ServerSocket server;

    public AcceptThread(KeySightManager keySightManager, JSONObject jsonObject, ServerSocket server) {
        this.keySightManager = keySightManager;
        this.allDataJsonObject = jsonObject;
        this.server = server;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                while (true) {
                    Socket socket = server.accept();
                    SendMessageThread messageThread = new SendMessageThread(keySightManager, allDataJsonObject, socket);
                    messageThread.start();
                }
            } catch (Exception i) {
                logger.error("处理信息错误" + i.getMessage());
            }
        }
    }
}
