package com.ht.printer;

import com.ht.comm.NetPortListener;
import com.ht.utils.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class PrinterListener extends Thread{
    private static final Log logger = LogFactory.getLog(NetPortListener.class);
    ServerSocket server = null;
    public Socket socket = null;
    public Boolean isConnect =false;

    public  PrinterListener(ServerSocket serverSocket) {
        this.server=serverSocket;
    }
    public  PrinterListener( ) {
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
            new PrinterSendMessThread().start();// 连接并返回socket后，再启用发送消息线程
            System.out.println(DateUtil.formatInfo("激光打码机客户端 （" + socket.getInetAddress().getHostAddress() + "） 连接成功..."));
            InputStream in = socket.getInputStream();
            int len = 0;
            byte[] buf = new byte[1024];
            synchronized (this) {
                while ((len = in.read(buf)) != -1) {
                    String message = new String(buf, 0, len, "UTF-8");
                    System.out.println(DateUtil.formatInfo("激光打码机客户端: （" + socket.getInetAddress().getHostAddress() + "）说：" + message));
                    this.notify();
                }

            }
        } catch (IOException e) {
            logger.warn(e);
        }

    }



    public  class PrinterSendMessThread extends Thread {
        @Override
        public void run() {
            super.run();
            Scanner scanner = null;
            OutputStream out = null;
            try {
                if (socket != null) {
                    scanner = new Scanner(System.in);
                    out = socket.getOutputStream();
                    String in = "";
                    do {
                        in = scanner.next();
                        out.write(("" + in).getBytes("UTF-8"));
                        out.flush();// 清空缓存区的内容
                    } while (!in.equals("q"));
                    scanner.close();
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

/*    // 函数入口
    public static void main(String[] args) {
        NetPortListener server = new NetPortListener(1234);
        server.start();
    }*/
}
