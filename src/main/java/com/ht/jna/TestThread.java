package com.ht.jna;


import com.alibaba.fastjson.JSONObject;
import com.ht.dc.TcpClient;
import com.ht.entity.EolStatus;
import com.ht.entity.ProRecords;
import com.ht.printer.PrinterListener;
import com.ht.utils.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

/**
 * @program: eol
 * @description: ${description}
 * @author: Zhangzhe
 * @create: 2020-03-24 18:33
 **/
public class TestThread extends Thread {
    private static final Log logger = LogFactory.getLog(TestThread.class);

    KeySightManager keySightManager;
    JSONObject jsonObject;
    boolean production;
    boolean print;

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

    public TestThread(KeySightManager keySightManager, JSONObject allDataJsonObject, boolean production, boolean print) {
        this.keySightManager = keySightManager;
        this.jsonObject = allDataJsonObject;
        this.production = production;
        this.print = print;

        this.temp = (JTextField) jsonObject.get("textFieldTemp");
        this.codeField = (JTextField) jsonObject.get("visualPartNumber");
        this.qcField = (JTextField) jsonObject.get("textFieldResistorsID");
        this.textFieldRt_R25 = (JTextField) jsonObject.get("textFieldRt_R25");
        this.textFieldRw_R16 = (JTextField) jsonObject.get("textFieldRw_R16");
        this.textFieldRntc_NTCRValue = (JTextField) jsonObject.get("textFieldRntc_NTCRValue");
        this.textFieldTemperature = (JTextField) jsonObject.get("textFieldTemperature");
        this.labelResultOne = (JLabel) jsonObject.get("labelResultOne");
        this.labelResultTwo = (JLabel) jsonObject.get("labelResultTwo");
        this.labelQRCode = (JLabel) jsonObject.get("labelQRCode");
        this.mDataView = (JTextArea) jsonObject.get("mDataView");
    }

    @Override
    public void run() {
        synchronized (this) {
            if (true) {
                String vpn = codeField.getText();
                if (production) {
                    mDataView.append(DateUtil.formatInfo("生产模式 [" + vpn + "] 测试开始 ..."));
                } else {
                    mDataView.append(DateUtil.formatInfo("自检模式 [" + vpn + "] 测试开始 ..."));
                }

                // 开电源
                TcpClient client = new TcpClient();
                client.open();

                ProRecords proRecords = keySightManager.testThePart(vpn, qcField.getText(), Double.valueOf(temp.getText()), mDataView, production);

                // 关电源
                client.close();
                if (production) {
                    mDataView.append(DateUtil.formatInfo("生产模式 [" + vpn + "] 测试结束 ..."));
                } else {
                    mDataView.append(DateUtil.formatInfo("自检模式 [" + vpn + "] 测试结束 ..."));
                }

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

                if (null == proRecords || proRecords.getProCode() == null) {
                    logger.info("无二维码");
                    if (production) {
                        EolStatus.getInstance().setEolStatus("Finish");
                        logger.info("检测失败，结束");
                    }
                } else {
                    // 这里发送给printerSocket客户端----------------------------------------------
                    if (print ) {
                        logger.info("开始打印二维码");
                        PrinterListener printerListener = PrinterListener.getInstance(mDataView);
                        if (production) {
                            EolStatus.getInstance().setEolStatus("Finish"); // 这里有问题？？？
                        }
                        logger.info("测试完成！");
                        printerListener.sendMessage(proRecords.getProCode());
                    }
                }
            }
        }
    }
}
