package com.ht.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.CharBuffer;

public class GetThread extends Thread {

    private Socket socket = null;
    private String ipAddress = null;
    private InputStream inputStream = null;
    private volatile boolean start = true;

    public GetThread(Socket socket, String ipAddress) {
        this.ipAddress = ipAddress;
        this.socket = socket;

        /*this.ipAddress = socket.getInetAddress().getHostAddress();*/
        try {
            this.inputStream = socket.getInputStream();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("GetThread started!");
        while (start) {
            try {
                if (inputStream.available() >0) { //判断inputStream中是否有数据 ，没有数据返回0
                    /*System.out.println(";"+inputStream.available());*/
                    String message = getMessage(inputStream);
                    if (message != null) {
                        if(null!=ipAddress){
                            System.out.println(ipAddress + ":" + message);
                        }else{
                            System.out.println("服务器说："+message);
                        }
                    }
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    public String getMessage(InputStream inputStream) {
        String message = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            /* String message = reader.readLine(); */
            CharBuffer charBuffer = CharBuffer.allocate(1024);
            int length = reader.read(charBuffer);
            /*System.out.println("length:" + length);*/
            charBuffer.flip();
            message = charBuffer.toString();
        } catch (IOException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
			/*try {
				reader.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
        }
        if (message != null) {
            /* System.out.println(ipAddress + ":" + message); */
            return message;
        } else {
            /* System.out.println("message:null"); */
            return null;
        }
    }

    public void stopClient() {
        this.start = false;
        try {
            inputStream.close();
            socket.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
