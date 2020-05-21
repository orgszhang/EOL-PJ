package com.ht.printer;

import com.ht.comm.NetPortListener;
import com.ht.utils.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PrinterListener extends Thread {
    private static final Log logger = LogFactory.getLog(NetPortListener.class);
    private  static Socket socket = null;
    public Boolean isConnect = false;
    ServerSocket server = null;

    JTextArea status;

    public PrinterListener(ServerSocket serverSocket) {
        this.server = serverSocket;
    }
    public  PrinterListener() {
        try {
            server = new ServerSocket(8082);
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    public void setStatus(JTextArea status) {
        this.status = status;
    }

    public Boolean getConnect() {
        return isConnect;
    }

    public void setConnect(Boolean connect) {
        isConnect = connect;
    }

    public ServerSocket getServer() {
        return server;
    }

    public void setServer(ServerSocket server) {
        this.server = server;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void closePort() {
        try {
            server.close();
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            logger.info(DateUtil.formatInfo("等待激光打码机客户端连接..."));
            //這裏得到激光打碼機socket
            socket = server.accept();
            this.setSocket(socket);
            this.setConnect(true);
            // new PrinterSendMessThread().start();// 连接并返回socket后，再启用发送消息线程
             System.out.println(DateUtil.formatInfo("激光打码机客户端 （" + socket.getInetAddress().getHostAddress() + "） 连接成功..."));

            InputStream in = socket.getInputStream();
            int len = 0;
            byte[] buf = new byte[1024];
            synchronized (this) {
                while ((len = in.read(buf)) != -1) {
                    String message = new String(buf, 0, len, StandardCharsets.UTF_8);
                    System.out.println(DateUtil.formatInfo("激光打码机客户端: （" + socket.getInetAddress().getHostAddress() + "）说：" + message));
                    this.notify();
                }

            }
        } catch (IOException e) {
            logger.warn(e);
        }

    }

    public void sendMessage(String message) {
        OutputStream out = null;
        try {
            if (socket != null) {
                out = socket.getOutputStream();
                out.write(("" + message).getBytes(StandardCharsets.UTF_8));
                out.flush();// 清空缓存区的内容
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

/*    // 函数入口
    public static void main(String[] args) {
        NetPortListener server = new NetPortListener(1234);
        server.start();
    }*/
}
