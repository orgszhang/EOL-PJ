package com.ht.comm;

import com.alibaba.fastjson.JSONObject;
import com.ht.base.SpringContext;
import com.ht.entity.EolStatus;
import com.ht.entity.LatestQRCodes;
import com.ht.entity.ProRecords;
import com.ht.jna.KeySightManager;
import com.ht.jna.TestThread;
import com.ht.repository.ProRecordsRepo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.Example;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class sendMessageThread extends Thread {
    private static final Log logger = LogFactory.getLog(sendMessageThread.class);

    KeySightManager keySightManager;
    JSONObject allDataJsonObject;
    Socket client;

    JTextField codeField;
    JTextField qcField;

    public sendMessageThread(KeySightManager keySightManager, JSONObject jsonObject, Socket socket) {
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
                    // 一旦有堵塞,
                    InputStream in = client.getInputStream();
                    OutputStream out = client.getOutputStream();
                    // dos = new DataOutputStream(client.getOutputStream());
                    // new sendMessThread(keySightManager, allDataJsonObject, client);// 连接并返回socket后，再启用发送消息线程

                    // 读取客户端数据
                    // DataInputStream input = new DataInputStream(client.getInputStream());
                    // 这里要注意和客户端输出流的写方法对应,否则会抛 EOFException
                    String clientInputStr = null;
                    /* try {
                        clientInputStr = input.readUTF();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/



                    int len = 0;
                    byte[] buf = new byte[1024];
                    synchronized (this) {
                        while ((len = in.read(buf)) != -1) {
                            clientInputStr = new String(buf, 0, len, StandardCharsets.UTF_8);
                            logger.info("接收结束: （" + client.getInetAddress().getHostAddress() + "）说：" + clientInputStr);
                            // mDataView.append(DateUtil.formatInfo("打码结束"));
                            // this.notify();

                            // 处理客户端数据
                            // logger.info("客户端发过来的内容:" + clientInputStr);

                            // if (clientInputStr == null) continue;

                            // 向客户端回复信息
                            // DataOutputStream dos = new DataOutputStream(client.getOutputStream());

                            // logger.info("Come here");

                            JSONObject jsonObject = JSONObject.parseObject(clientInputStr);
                            if (jsonObject.getString("Command").equals("QueryStatus")) {
                                // logger.info("OK1");
                                JSONObject result = new JSONObject();
                                result.put("Command", "QueryStatus");
                                JSONObject resultValue = new JSONObject();
                                resultValue.put("EolStatus", EolStatus.getInstance().getEolStatus());
                                result.put("ResultValue", resultValue);
                                logger.info(result.toJSONString());
                                out.write(("" + result.toJSONString()).getBytes(StandardCharsets.UTF_8));
                                out.flush();// 清空缓存区的内容
                                // logger.info("OK2");
                                // dos.writeUTF(result.toJSONString());
                            } else if (jsonObject.getString("Command").equals("StartTest")) {
                                EolStatus.getInstance().setEolStatus("Busy");
                                JSONObject result = new JSONObject();
                                result.put("Command", "StartTest");
                                logger.info(result.toJSONString());
                                out.write(("" + result.toJSONString()).getBytes(StandardCharsets.UTF_8));
                                out.flush();// 清空缓存区的内容
                                // out.writeUTF(result.toJSONString());
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
                                ProRecords proRecords = new ProRecords();
                                try {
                                    ProRecordsRepo rRepo = SpringContext.getBean(ProRecordsRepo.class);
                                    /*proRecords.setVisualPartNumber(params.getString("VirtualPartNumber"));
                                    Example<ProRecords> resultOne = Example.of(proRecords);
                                    proRecords = rRepo.findOne(resultOne).get();*/

                                    Optional<ProRecords> oneRow = rRepo.findById(params.getString("VirtualPartNumber"));
                                    proRecords = oneRow.get();
                                    logger.info(proRecords.toString());
                                } catch (Exception e) {
                                    logger.error("从数据库读取零件信息异常 - " + params.getString("VirtualPartNumber"));
                                    logger.error(e.getMessage());
                                    // mDataView.append("从数据库读取零件信息异常，请检查！ - " + params.getString("VirtualPartNumber"));
                                    proRecords = null;
                                }
                                JSONObject result = new JSONObject();
                                result.put("Command", "QueryResult");
                                JSONObject js = new JSONObject();
                                js.put("EolStatus", EolStatus.getInstance().getEolStatus());
                                if (null == proRecords || StringUtils.isEmpty(proRecords.getProCode())) {
                                    js.put("testResult", "fail");
                                    result.put("ResultValue", js);
                                    logger.info(result.toJSONString());
                                    out.write(("" + result.toJSONString()).getBytes(StandardCharsets.UTF_8));
                                    out.flush();// 清空缓存区的内容
                                    // dos.writeUTF(result.toJSONString());
                                } else {
                                    String str = JSONObject.toJSONString(proRecords);
                                    JSONObject json = JSONObject.parseObject(str);
                                    json.put("testResult", "success");
                                    js.putAll(json);
                                    result.put("ResultValue", js);
                                    logger.info(result.toJSONString());
                                    out.write(("" + result.toJSONString()).getBytes(StandardCharsets.UTF_8));
                                    out.flush();// 清空缓存区的内容
                                    // dos.writeUTF(result.toJSONString());
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
                                out.flush();// 清空缓存区的内容
                                // dos.writeUTF(result.toJSONString());
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
