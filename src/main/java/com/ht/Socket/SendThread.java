package com.ht.Socket;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

/**
 * @program: socket
 * @description: ${description}
 * @author: Zhangzhe
 * @create: 2020-03-11 18:09
 **/

public class SendThread extends Thread {

    private Socket socket = null;
    private String sendStr = null;

    private List<String> messages = null;
    private OutputStream outputStream = null;
    private volatile boolean start = true;

    public SendThread(Socket socket, List<String> messages) {
        this.socket = socket;
        this.messages = messages;
        try {
            this.outputStream = socket.getOutputStream();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("SendThread started!");
        while (true) {
            // System.out.println("ClientThread.....");
            if (messages.size() > 0) {
                sendMessage(outputStream);
                try {
                    synchronized (this) {
                        wait(); //该线程在发送完一个消息后进入等待，放弃CPU不再执行，等待被唤醒
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    public void sendMessage(OutputStream outputStream) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(messages.get(0));
            writer.flush();//必须的要将缓存中的数据刷新到输出流中
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
			/*try {
				writer.close();//报错socket closed
			} catch (IOException e) {
				logger.error(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
        }
        this.messages.remove(0);

    }

    public void stopClient() {
        this.start = false;
        try {
            outputStream.close();
            socket.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void notifyWriter(){
        synchronized (this) {
            notify();
        }
    }

}