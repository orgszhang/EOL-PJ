package com.ht.comm;


import com.alibaba.fastjson.JSONObject;
import com.ht.jna.KeySightManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.swing.JTextField;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
//123
public class NetPortListener extends Thread {
    private static final Log logger = LogFactory.getLog(NetPortListener.class);
    ServerSocket server = null;
    Socket socket = null;
    JTextField codeField=null;
    JTextField qcField=null;
    JTextField temp=null;
    public NetPortListener(int port, JSONObject jsonObject) {
        try {
            server = new ServerSocket(port);
            this.codeField=(JTextField) jsonObject.get("visualPartNumber");
            this.qcField=(JTextField) jsonObject.get("textFieldResistorsID");
            this.temp=(JTextField) jsonObject.get("textFieldTemp");
        } catch (IOException e) {
            logger.warn(e);
        }
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
            System.out.println(getdate() + "  等待客户端连接...");
            socket = server.accept();
            new sendMessThread().start();// 连接并返回socket后，再启用发送消息线程
            System.out.println(getdate() + "  客户端 （" + socket.getInetAddress().getHostAddress() + "） 连接成功...");
            InputStream in = socket.getInputStream();
            int len = 0;
            byte[] buf = new byte[1024];
            synchronized (this) {
                while ((len = in.read(buf)) != -1) {
                    String message = new String(buf, 0, len, "UTF-8");
                    System.out.println(getdate() + "  客户端: （" + socket.getInetAddress().getHostAddress() + "）说："
                            + message);

                    JSONObject jsonObject = JSONObject.parseObject(message);
                    codeField.setText(jsonObject.getString("code"));
                    qcField.setText(jsonObject.getString("qc"));
                    KeySightManager keySightManager=new KeySightManager();
                    keySightManager.testThePart(jsonObject.getString("code"),Double.valueOf(temp.getText()),jsonObject.getString("qc"));
                    this.notify();
                }

            }
        } catch (IOException e) {
            logger.warn(e);
        }

    }

    public static String getdate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String result = format.format(date);
        return result;
    }

    class sendMessThread extends Thread {
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
