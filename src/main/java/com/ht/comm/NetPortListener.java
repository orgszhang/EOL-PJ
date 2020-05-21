package com.ht.comm;


import com.alibaba.fastjson.JSONObject;
import com.ht.base.SpringContext;
import com.ht.entity.ProRecords;
import com.ht.jna.KeySightManager;
import com.ht.printer.PrinterListener;
import com.ht.repository.ProRecordsRepo;
import com.ht.repository.TestResultsRepo;
import com.ht.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;


import javax.swing.*;
import java.awt.*;
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
    JTextField codeField = null;
    JTextField qcField = null;
    JTextField temp = null;
    JTextField textFieldRt_R25 = null;
    JTextField textFieldRw_R16 = null; // Rw
    JTextField textFieldRntc_NTCRValue = null; // Rntc
    JTextField textFieldTemperature = null; //换算温度
    JLabel labelResultOne = null; //分流电阻结果
    JLabel labelResultTwo = null; //ntc结果
    JLabel labelQRCode = null;
    JTextArea mDataView = null;
    ThreadLocal<String> eolStatus;
    ServerSocket printSeverSocket;
    Socket printSocket;
    KeySightManager keySightManager;


    public NetPortListener(int port, JSONObject jsonObject, ServerSocket printSeverSocket, KeySightManager keySightManager) {
        try {
            server = new ServerSocket(port);
            this.codeField = (JTextField) jsonObject.get("visualPartNumber");
            this.qcField = (JTextField) jsonObject.get("textFieldResistorsID");
            this.temp = (JTextField) jsonObject.get("textFieldTemp");
            this.codeField = (JTextField) jsonObject.get("visualPartNumber");
            this.qcField = (JTextField) jsonObject.get("textFieldResistorsID");
            this.temp = (JTextField) jsonObject.get("textFieldTemp");
            this.textFieldRt_R25 = (JTextField) jsonObject.get("textFieldRt_R25");
            this.textFieldRntc_NTCRValue = (JTextField) jsonObject.get("textFieldRntc_NTCRValue");
            this.textFieldRw_R16 = (JTextField) jsonObject.get("textFieldRw_R16");
            this.textFieldTemperature = (JTextField) jsonObject.get("textFieldTemperature");
            this.labelResultOne = (JLabel) jsonObject.get("labelResultOne");
            this.labelResultTwo = (JLabel) jsonObject.get("labelResultTwo");
            this.labelQRCode = (JLabel) jsonObject.get("labelQRCode");
            this.mDataView = (JTextArea) jsonObject.get("mDataView");
            this.eolStatus = (ThreadLocal<String>) jsonObject.get("eolStatus");
            this.printSeverSocket = printSeverSocket;
            this.keySightManager = keySightManager;
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
            System.out.println(DateUtil.getDate() + "  等待客户端连接...");
            socket = server.accept();
            new sendMessThread().start();// 连接并返回socket后，再启用发送消息线程
            System.out.println(DateUtil.getDate() + "  客户端 （" + socket.getInetAddress().getHostAddress() + "） 连接成功...");
            InputStream in = socket.getInputStream();
            int len = 0;
            byte[] buf = new byte[1024];
            synchronized (this) {
                while ((len = in.read(buf)) != -1) {
                    String message = new String(buf, 0, len, "UTF-8");
                    System.out.println(DateUtil.getDate() + "  客户端: （" + socket.getInetAddress().getHostAddress() + "）说："
                            + message);
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    JSONObject jsonObject = JSONObject.parseObject(message);
                    if (jsonObject.getString("Command").equals("QueryStatus")) {
                        JSONObject result = new JSONObject();
                        result.put("Command", "QueryStatus");
                        JSONObject resultValue = new JSONObject();
                        resultValue.put("EolStatus", eolStatus.get());
                        result.put("ResultValue", resultValue);
                        dos.write(result.toJSONString().getBytes());
                    } else if (jsonObject.getString("Command").equals("TestAPart")) {
                        eolStatus.set("Busy");
                        JSONObject params=JSONObject.parseObject(jsonObject.get("Parameter").toString());
                        codeField.setText(params.getString("VirtualPartNumber"));
                        qcField.setText(params.getString("ResistorID"));
                        ProRecords proRecords = keySightManager.testThePart(jsonObject.getString("VirtualPartNumber"), Double.valueOf(temp.getText()), qcField.getText(), null, mDataView, eolStatus, dos);
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
                       /* JSONObject result = new JSONObject();
                        result.put("Command", "TestAPart");
                        String jsonStr = JSONObject.toJSONString(proRecords);
                        JSONObject js=JSONObject.parseObject(jsonStr);
                      *//*  if(StringUtils.isNotEmpty(proRecords.getProCode())) {
                            js.put("testResult", "success");
                        }else {
                            js.put("testResult", "fail");
                        }*//*
                        result.put("ResultValue", js);
                        dos.write(result.toJSONString().getBytes());*/

                        //这里发送给printerSocket客户端----------------------------------------------
                        Socket socketPrint = new PrinterListener().getSocket();
                        if(StringUtils.isNotEmpty(proRecords.getProCode())){
                            DataOutputStream dosPrint = new DataOutputStream(socketPrint.getOutputStream());
                            String json = proRecords.getProCode();
                            dosPrint.write(json.getBytes());
                        }

                    } else if (jsonObject.getString("Command").equals("QueryResult")) {
                        JSONObject params=JSONObject.parseObject(jsonObject.get("Parameter").toString());
                        ProRecords proRecords=new ProRecords();
                        try {
                            ProRecordsRepo rRepo = SpringContext.getBean(ProRecordsRepo.class);
                            proRecords.setVisualPartNumber(params.getString("VirtualPartNumber"));
                            Example<ProRecords> resultOne=Example.of(proRecords);
                            proRecords=rRepo.findOne(resultOne).get();
                        } catch (Exception e) {
                            mDataView.append("数据库异常，请检查连接！");
                            proRecords=null;
                        }
                        if(null==proRecords || StringUtils.isEmpty(proRecords.getProCode())){
                           JSONObject jsonObj=new JSONObject();
                           jsonObj.put("testResult","fail");
                            dos.write(jsonObj.toJSONString().getBytes());
                        }else {
                            String str=JSONObject.toJSONString(proRecords);
                            JSONObject json=JSONObject.parseObject(str);
                            json.put("testResult","success");
                            dos.write(json.toJSONString().getBytes());
                        }

                    } else if (jsonObject.getString("Command").equals("DataSaved")) {
                        eolStatus.set("Ready");
                    }
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
