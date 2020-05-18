package com.ht.comm;


/**
 * @program: eol
 * @description: ${description}
 * @author: Zhangzhe
 * @create: 2020-03-24 18:33
 **/
public class ClientSendMLisener extends Thread {
    public ClientSendMLisener() {
    }

    @Override
    public void run() {

        while (true) {
            synchronized (this){
                 if(true){
                     System.out.println(1111);
                 }
            }
        }
    }

    public static void main(String[] args) {
        new ClientSendMLisener().start();
    }
}
