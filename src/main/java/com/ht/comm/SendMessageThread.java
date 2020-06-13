package com.ht.comm;

import com.alibaba.fastjson.JSONObject;
import com.ht.base.SpringContext;
import com.ht.entity.EolStatus;
import com.ht.entity.ProRecords;
import com.ht.jna.KeySightManager;
import com.ht.jna.TestThread;
import com.ht.repository.ProRecordsRepo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class SendMessageThread extends Thread {
    private static final Log logger = LogFactory.getLog(SendMessageThread.class);

    KeySightManager keySightManager;
    JSONObject allDataJsonObject;
    Socket client;

    JTextField codeField;
    JTextField qcField;

    public SendMessageThread(KeySightManager keySightManager, JSONObject jsonObject, Socket socket) {
        this.keySightManager = keySightManager;
        this.allDataJsonObject = jsonObject;
        this.client = socket;

        this.codeField = (JTextField) jsonObject.get("visualPartNumber");
        this.qcField = (JTextField) jsonObject.get("textFieldResistorsID");
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                while (true) {
                    InputStream in = client.getInputStream();
                    OutputStream out = client.getOutputStream();

                    String clientInputStr = null;

                    int len = 0;
                    byte[] buf = new byte[1024];
                    synchronized (this) {
                        while ((len = in.read(buf)) != -1) {
                            clientInputStr = new String(buf, 0, len, StandardCharsets.UTF_8);
                            logger.info("接收结束: （" + client.getInetAddress().getHostAddress() + "）说：" + clientInputStr);

                            JSONObject jsonObject = JSONObject.parseObject(clientInputStr);
                            if (jsonObject.getString("Command").equals("QueryStatus")) {
                                JSONObject result = new JSONObject();
                                result.put("Command", "QueryStatus");
                                JSONObject resultValue = new JSONObject();
                                resultValue.put("EolStatus", EolStatus.getInstance().getEolStatus());
                                result.put("ResultValue", resultValue);
                                logger.info(result.toJSONString());
                                out.write(("" + result.toJSONString()).getBytes(StandardCharsets.UTF_8));
                                out.flush();
                            } else if (jsonObject.getString("Command").equals("StartTest")) {
                                EolStatus.getInstance().setEolStatus("Busy");
                                JSONObject result = new JSONObject();
                                result.put("Command", "StartTest");
                                logger.info(result.toJSONString());
                                out.write(("" + result.toJSONString()).getBytes(StandardCharsets.UTF_8));
                                out.flush();// 清空缓存区的内容

                                JSONObject params = JSONObject.parseObject(jsonObject.get("Parameter").toString());
                                codeField.setText(params.getString("VirtualPartNumber"));
                                qcField.setText(params.getString("ResistorID"));
                                ExecutorService pool = Executors.newFixedThreadPool(1);
                                try {
                                    pool.submit(new TestThread(keySightManager, allDataJsonObject, /*out, */true, true));
                                } catch (Exception e) {
                                    logger.error("测试零件错误 - " + allDataJsonObject.getString("VirtualPartNumber"));
                                    logger.error(e.getMessage());
                                } finally {
                                    pool.shutdown();
                                }
                            } else if (jsonObject.getString("Command").equals("QueryResult")) {
                                JSONObject params = JSONObject.parseObject(jsonObject.get("Parameter").toString());
                                ProRecords proRecords = null;
                                try {
                                    ProRecordsRepo rRepo = SpringContext.getBean(ProRecordsRepo.class);
                                    Optional<ProRecords> oneRow = rRepo.findById(params.getString("VirtualPartNumber"));
                                    proRecords = oneRow.get();
                                    logger.info(proRecords.toString());
                                } catch (Exception e) {
                                    logger.error("从数据库读取零件信息异常 - " + params.getString("VirtualPartNumber"));
                                    logger.error(e.getMessage());
                                }

                                JSONObject result = new JSONObject();
                                result.put("Command", "QueryResult");

                                JSONObject js = new JSONObject();
                                js.put("EolStatus", EolStatus.getInstance().getEolStatus());

                                if (null == proRecords) { // 无测试数据
                                    js.put("testResult", "fail");
                                    result.put("ResultValue", js);
                                    logger.info(result.toJSONString());
                                    out.write(("" + result.toJSONString()).getBytes(StandardCharsets.UTF_8));
                                    out.flush();// 清空缓存区的内容
                                } else if (StringUtils.isEmpty(proRecords.getProCode())) { // 数据有错，没有生成二维码
                                    String str = JSONObject.toJSONStringWithDateFormat(proRecords, "yyyy-MM-dd HH:mm:ss:S");
                                    JSONObject json = JSONObject.parseObject(str);
                                    json.put("testResult", "fail");
                                    js.putAll(json);
                                    result.put("ResultValue", js);
                                    logger.info(result.toJSONString());
                                    out.write(("" + result.toJSONString()).getBytes(StandardCharsets.UTF_8));
                                    out.flush();
                                } else {  // 测试成功
                                    String str = JSONObject.toJSONStringWithDateFormat(proRecords, "yyyy-MM-dd HH:mm:ss:S");
                                    JSONObject json = JSONObject.parseObject(str);
                                    json.put("testResult", "success");
                                    js.putAll(json);
                                    result.put("ResultValue", js);
                                    logger.info(result.toJSONString());
                                    out.write(("" + result.toJSONString()).getBytes(StandardCharsets.UTF_8));
                                    out.flush();
                                }
                            } else if (jsonObject.getString("Command").equals("SetStatus")) {
                                EolStatus.getInstance().setEolStatus("Ready");
                                JSONObject result = new JSONObject();
                                result.put("Command", "SetStatus");
                                JSONObject js = new JSONObject();
                                js.put("EolStatus", EolStatus.getInstance().getEolStatus());
                                result.put("ResultValue", js);
                                logger.info(result.toJSONString());
                                out.write(("" + result.toJSONString()).getBytes(StandardCharsets.UTF_8));
                                out.flush();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // EolStatus.getInstance().setEolStatus("Error");
            logger.warn("主控PLC端口处理信息异常: " + e.getMessage());
        }
    }
}
