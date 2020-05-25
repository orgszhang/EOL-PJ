package com.ht.comm;


import com.alibaba.fastjson.JSONObject;
import com.ht.entity.EolStatus;
import com.ht.entity.ProRecords;
import com.ht.jna.KeySightDevice_Electricity;
import com.ht.jna.KeySightManager;
import com.ht.jna.TcpClient;
import com.ht.printer.PrinterListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Date;

/**
 * @program: eol
 * @description: ${description}
 * @author: Zhangzhe
 * @create: 2020-03-24 18:33
 **/
public class DataTest extends Thread {
    private static final Log logger = LogFactory.getLog(DataTest.class);
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
    EolStatus eolStatus;
    ServerSocket printSeverSocket;
    Socket printSocket;
    KeySightManager keySightManager;
    JSONObject jsonObject;
    JSONObject params;
    DataOutputStream dos;
    public DataTest() {
    }



    public DataTest(KeySightManager keySightManager, JSONObject params, JSONObject allDataJsonObject, DataOutputStream dos, EolStatus eolStatus ) {
        this.params=params;
        this.keySightManager = keySightManager;
        this.jsonObject=allDataJsonObject;
        this.qcField = (JTextField) jsonObject.get("textFieldResistorsID");
        this.temp = (JTextField) jsonObject.get("textFieldTemp");
        this.codeField = (JTextField) jsonObject.get("visualPartNumber");
        this.qcField = (JTextField) jsonObject.get("textFieldResistorsID");
        this.textFieldRt_R25 = (JTextField) jsonObject.get("textFieldRt_R25");
        this.textFieldRntc_NTCRValue = (JTextField) jsonObject.get("textFieldRntc_NTCRValue");
        this.textFieldRw_R16 = (JTextField) jsonObject.get("textFieldRw_R16");
        this.textFieldTemperature = (JTextField) jsonObject.get("textFieldTemperature");
        this.labelResultOne = (JLabel) jsonObject.get("labelResultOne");
        this.labelResultTwo = (JLabel) jsonObject.get("labelResultTwo");
        this.labelQRCode = (JLabel) jsonObject.get("labelQRCode");
        this.mDataView = (JTextArea) jsonObject.get("mDataView");
        this.eolStatus = eolStatus;
        this.printSeverSocket = printSeverSocket;
        this.keySightManager = keySightManager;
        this.dos=dos;
    }



    @Override
    public void run() {


            synchronized (this) {
                if (true) {
                    // 开电源
                    TcpClient client = new TcpClient();
                    mDataView.append(new Date() + "生产模式 - 测试开始 ...\r\n");
                    client.open();

                    ProRecords proRecords = keySightManager.testThePart(params.getString("VirtualPartNumber"), Double.valueOf(temp.getText()), qcField.getText(), null, mDataView, eolStatus, dos);

                    // 关电源
                    client.close();
                    mDataView.append(new Date() + "生产模式 - 测试结束 ...\r\n");
                    NumberFormat ddf = NumberFormat.getNumberInstance();
                    ddf.setMaximumFractionDigits(4);
                    textFieldRt_R25.setText(ddf.format(proRecords.getR25()));
                    textFieldRw_R16.setText(ddf.format(proRecords.getR16()));
                    textFieldRntc_NTCRValue.setText(ddf.format(proRecords.getRntc()));
                    textFieldTemperature.setText(ddf.format(proRecords.getTntc()));
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


               /*     //这里发送给printerSocket客户端----------------------------------------------
                    if (null != proRecords && null != proRecords.getProCode()) {
                        Socket socketPrint = new PrinterListener().getSocket();
                        if (StringUtils.isNotEmpty(proRecords.getProCode())) {
                            DataOutputStream dosPrint = null;
                            try {
                                dosPrint = new DataOutputStream(socketPrint.getOutputStream());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String json = proRecords.getProCode();
                            try {
                                dosPrint.write(json.getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }*/
                    logger.info("测试完成！");

                    eolStatus.setEolStatus("Finish");
                }
            }

    }


    public static void main(String[] args) {

    }
}
