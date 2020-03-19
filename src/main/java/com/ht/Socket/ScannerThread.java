package com.ht.Socket;



import java.util.List;
import java.util.Scanner;

public class ScannerThread extends Thread {
    private List<String> messages = null;
    SendThread sendThread = null;

    public ScannerThread(List<String> messages, SendThread sendThread) {
        this.messages = messages;
        this.sendThread = sendThread;
    }

    @Override
    public void run() {
        while (true) {
            /*System.out.print("请输入:");*/
            Scanner scanner = new Scanner(System.in);//从控制台获得输入数据
            String sendMsg = scanner.nextLine(); //用next方法会多字符，不知为什么
            /*System.out.println("sendMsg:" + sendMsg);*/
            /* .sendMsg(sendMsg); */
            if (sendMsg != null) {
                messages.add(sendMsg);
                sendThread.notifyWriter();//唤醒发送线程，发送消息
            }
        }
    }
}
