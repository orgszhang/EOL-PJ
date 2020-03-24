package com.ht.comm;


import com.alibaba.fastjson.JSONObject;

import com.ht.entity.ProRecords;
import com.ht.jna.KeySightManager;
import com.ht.printer.PrinterListener;

import com.ht.utils.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Color;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
//123
public class NetPortListener extends Thread {
    private static final Log logger = LogFactory.getLog(NetPortListener.class);
    ServerSocket server = null;
    Socket socket = null;
    JTextField codeField=null;
    JTextField qcField=null;
    JTextField temp=null;
    JTextField textFieldRt_R25 = null;
    JTextField textFieldRw_R16 = null; // Rw
    JTextField textFieldRntc_NTCRValue = null; // Rntc
    JTextField textFieldTemperature =  null; //换算温度
    JLabel labelResultOne = null; //分流电阻结果
    JLabel labelResultTwo = null; //ntc结果
    JLabel labelQRCode = null;
    JTextArea mDataView =null;
    ThreadLocal<String> eolStatus;
    ServerSocket printSeverSocket;
    Socket printSocket;



    public NetPortListener(int port,JSONObject jsonObject, ServerSocket printSeverSocket) {
        try {
            server = new ServerSocket(port);
            this.codeField=(JTextField) jsonObject.get("visualPartNumber");
            this.qcField=(JTextField) jsonObject.get("textFieldResistorsID");
            this.temp=(JTextField) jsonObject.get("textFieldTemp");
            this.codeField=(JTextField) jsonObject.get("visualPartNumber");
            this.qcField=(JTextField) jsonObject.get("textFieldResistorsID");
            this.temp=(JTextField) jsonObject.get("textFieldTemp");
            this.textFieldRt_R25=(JTextField)jsonObject.get("textFieldRt_R25");
            this.textFieldRntc_NTCRValue=(JTextField)jsonObject.get("textFieldRntc_NTCRValue");
            this.textFieldRw_R16=(JTextField)jsonObject.get("textFieldRw_R16");
            this.textFieldTemperature=(JTextField)jsonObject.get("textFieldTemperature");
            this.labelResultOne=(JLabel)jsonObject.get("labelResultOne");
            this.labelResultTwo=(JLabel)jsonObject.get("labelResultTwo");
            this.labelQRCode=(JLabel)jsonObject.get("labelQRCode");
            this.mDataView =(JTextArea)jsonObject.get("mDataView");
            this.eolStatus=(ThreadLocal<String>)jsonObject.get("eolStatus");
            this.printSeverSocket=printSeverSocket;
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
            System.out.println(DateUtil.getdate() + "  等待客户端连接...");
            socket = server.accept();
            new sendMessThread().start();// 连接并返回socket后，再启用发送消息线程
            System.out.println(DateUtil.getdate() + "  客户端 （" + socket.getInetAddress().getHostAddress() + "） 连接成功...");
            InputStream in = socket.getInputStream();
            int len = 0;
            byte[] buf = new byte[1024];
            synchronized (this) {
                while ((len = in.read(buf)) != -1) {
                    String message = new String(buf, 0, len, "UTF-8");
                    System.out.println(DateUtil.getdate() + "  客户端: （" + socket.getInetAddress().getHostAddress() + "）说："
                            + message);

                    JSONObject jsonObject = JSONObject.parseObject(message);
                    codeField.setText(jsonObject.getString("code"));
                    qcField.setText(jsonObject.getString("qc"));
                    KeySightManager keySightManager=new KeySightManager();
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    ProRecords proRecords = keySightManager.testThePart(jsonObject.getString("code"), Double.valueOf(temp.getText()), jsonObject.getString("qc"),mDataView,eolStatus,dos);
                    textFieldRt_R25.setText(proRecords.getR25().toString());
                    textFieldRw_R16.setText(proRecords.getR16().toString());
                    textFieldRntc_NTCRValue.setText(proRecords.getRntc().toString());
                    textFieldTemperature.setText(proRecords.getTntc().toString());
                    if ((proRecords.getR25() < 78.25 && proRecords.getR25() > 71.75)) {
                        labelResultOne.setText("合格");
                        labelResultOne.setForeground(Color.green);

                    } else {
                        labelResultOne.setText("不合格");
                        labelResultOne.setForeground(Color.red);
                    }

                    if (Math.abs(proRecords.getTntc() - Double.valueOf(temp.getText())) <= 3) {
                        labelResultTwo.setText("合格");
                        labelResultTwo.setForeground(Color.green);
                    } else {
                        labelResultTwo.setText("不合格");
                        labelResultTwo.setForeground(Color.red);
                    }
                    labelQRCode.setText(proRecords.getProCode());
                    //这里发送给printerSocket客户端----------------------------------------------
                    Socket socketPrint = new PrinterListener().getSocket();
                    DataOutputStream dosPrint = new DataOutputStream(socketPrint.getOutputStream());
                    dosPrint.write(proRecords.getProCode().getBytes());
                    this.notify();
                    }


            }
        } catch (IOException e) {
            logger.warn(e);
        }

    }


    class printSendMessThread extends Thread {
        @Override
        public void run() {
            super.run();
            Scanner scanner = null;
            OutputStream out = null;
            try {
                if (printSocket != null) {
                    scanner = new Scanner(System.in);
                    out = printSocket.getOutputStream();
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
