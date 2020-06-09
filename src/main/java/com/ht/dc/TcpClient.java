package com.ht.dc;

import com.ht.base.SpringContext;
import com.ht.entity.Devices;
import com.ht.repository.DevicesRepo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class TcpClient {
    private static final Log logger = LogFactory.getLog(TcpClient.class);

    Socket socket;
    String ip = "169.254.210.22";
    int port = 5025;

    public TcpClient() {
    }

    public static void main(String[] args) {
        TcpClient client = new TcpClient();
        String[] cmds = {
                "SYSTem:LOCK ON",
                "OUTPut?",  // off
                "OUTPut ON",
                "OUTPut?",  // on
                "OUTPut OFF",
                "OUTPut?",  // off
                "SYSTem:LOCK OFF"};

        try {
            //创建Socket对象，连接服务器
            for (int i = 0; i < cmds.length; i++) {
                client.socket = new Socket("169.254.210.22", 5025);
                client.writeCmd(cmds[i]);
                client.socket.close();
            }

        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    public void remoteCtl(boolean on) {
        try {
            if (on) {
                socket = new Socket(ip, port);
                writeCmd("SYSTem:LOCK ON");
                socket.close();
                logger.info("电源远程控制模式打开");
            } else {
                socket = new Socket(ip, port);
                writeCmd("SYSTem:LOCK OFF");
                socket.close();
                logger.info("电源远程控制模式关闭");
            }
        } catch (Exception exp) {
            logger.error(exp);
        }
    }

    public void open() {
        try {
            socket = new Socket(ip, port);
            writeCmd("OUTPut ON");
            socket.close();
            logger.info("电源通电");
        } catch (Exception exp) {
            logger.error(exp);
        }
    }

    public void close() {
        try {
            socket = new Socket(ip, port);
            writeCmd("OUTPut OFF");
            socket.close();
            logger.info("电源断电");
        } catch (Exception exp) {
            logger.error(exp);
        }
    }

    public void writeCmd(String cmd) {
        // System.out.println(cmd);
        try {
            OutputStream out = socket.getOutputStream();
            out.write((cmd + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception exp) {
            logger.error(exp);
        }
    }
}