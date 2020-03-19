package com.ht.Socket;





import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class EolServer {
    public EolServer() {
        try {
            //用于ScannerThread和SendThread两个线程之间消息通信的桥梁，?不用synchronized SendThread无法获得数据
            List<String> messages = Collections.synchronizedList(new LinkedList<String>());

            //实例化一个ServerSocket，并监听8888端口，
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("监听中........");
			/*	 该方法阻塞直到获得一个socket
				Listens for a connection to be made to this socket and accepts it. The method blocks until a connection is made.
			*/
            Socket socket = serverSocket.accept();
            String ipAddress = socket.getInetAddress().getLocalHost().toString();

          /*  System.out.println("\nPlease input a character:");//提示从控制台输入一个字符S
            String a="1231";

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("我在繁忙中！");*/
            //启动接受消息的线程，无限循环准备接受消息
            GetThread getThread = new GetThread(socket, ipAddress);
            getThread.start();

            //启动发送消息的线程，发送完一个消息就wait，等待ScannerThread获得输入后唤醒
            SendThread sendThread = new SendThread(socket, messages);
            sendThread.start();

            //启动一个等待用户输入消息的线程，通过控制台输入消息后，放入messages消息队列中，然后唤醒SendThread线程发送消息
            new ScannerThread(messages, sendThread).start();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
