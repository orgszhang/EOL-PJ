package com.ht.printer;

import com.ht.entity.EolStatus;
import com.ht.utils.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PrinterListener {
    private static final Log logger = LogFactory.getLog(PrinterListener.class);

    static PrinterListener listener;
    private static Socket socket = null;


    ServerSocket server = null;
    JTextArea mDataView = null;

    private PrinterListener(JTextArea mDataView) {
        this.mDataView = mDataView;

        String ip = "169.254.210.66";
        int port = 8082;

        String[] ipStr = ip.split("\\.");
        byte[] ipBuf = new byte[4];
        for (int i = 0; i < 4; i++) {
            ipBuf[i] = (byte) (Integer.parseInt(ipStr[i]) & 0xff);
        }

        try {
            InetAddress inetAddresd = InetAddress.getByAddress(ipBuf);
            server = new ServerSocket(port, 5, inetAddresd);
            // 读取客户端数据
            logger.info("等待激光打码机客户端连接...");
            //這裏得到激光打碼機socket
            ListenThread thread = new ListenThread(server, mDataView);
            thread.start();
        } catch (Exception e) {
            logger.warn("打码机端口异常: " + e.getMessage());
            mDataView.append(DateUtil.formatInfo("打码机端口异常"));
            EolStatus.getInstance().setEolStatus("Error");
        }
    }

    public static PrinterListener getInstance(JTextArea mDataView) {
        if (listener == null)
            listener = new PrinterListener(mDataView);
        return listener;
    }

    public void setSocket(Socket s) {
        socket = s;
    }

    public void closePort() {
        try {
            server.close();
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    public void sendMessage(String message) {
        try {
            if (socket != null) {
                OutputStream out = socket.getOutputStream();
                logger.info(message);
                logger.info(("" + message).getBytes(StandardCharsets.UTF_8));
                out.write(("" + message).getBytes(StandardCharsets.UTF_8));
                out.flush();// 清空缓存区的内容

                try {
                    wait(2000);
                } catch (Exception exp) {

                }

                InputStream in = socket.getInputStream();
                int len = 0;
                byte[] buf = new byte[1024];
                synchronized (this) {
                    while ((len = in.read(buf)) != -1) {
                        message = new String(buf, 0, len, StandardCharsets.UTF_8);
                        logger.info("打码结束: （" + socket.getInetAddress().getHostAddress() + "）说：" + message);
                        mDataView.append(DateUtil.formatInfo("激光打码机客户端: （" + socket.getInetAddress().getHostAddress() + "）打码结束：" + message));
                        this.notify();
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("打码机打码发送信息错误" + e.getMessage());
            mDataView.append(DateUtil.formatInfo("打码机打码发送信息错误"));
            EolStatus.getInstance().setEolStatus("Error");
        }
    }
}