package com.ht.printer;

import com.ht.entity.EolStatus;
import com.ht.utils.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ListenThread extends Thread {
    private static final Log logger = LogFactory.getLog(ListenThread.class);

    ServerSocket server;
    JTextArea mDataView;

    public ListenThread(ServerSocket server, JTextArea mDataView) {
        this.server = server;
        this.mDataView = mDataView;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                while (true) {
                    Socket socket = server.accept();
                    PrinterListener.getInstance(mDataView).setSocket(socket);
                    // new PrinterSendMessThread().start();// 连接并返回socket后，再启用发送消息线程
                    logger.info("激光打码机客户端 （" + socket.getInetAddress().getHostAddress() + "） 连接成功...");
                    InputStream in = socket.getInputStream();
                    int len = 0;
                    byte[] buf = new byte[1024];
                    synchronized (this) {
                        while ((len = in.read(buf)) != -1) {
                            String message = new String(buf, 0, len, StandardCharsets.UTF_8);
                            logger.info("激光打码机客户端: （" + socket.getInetAddress().getHostAddress() + "）说：" + message);
                            mDataView.append(DateUtil.formatInfo("激光打码机客户端: （" + socket.getInetAddress().getHostAddress() + "）说：" + message));
                            this.notify();
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("激光打码机端口侦听异常: " + e.getMessage());
                mDataView.append(DateUtil.formatInfo("激光打码机端口侦听异常"));
                EolStatus.getInstance().setEolStatus("Error");
            }
        }
    }
}
