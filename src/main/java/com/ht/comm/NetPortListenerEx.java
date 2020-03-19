package com.ht.comm;


import com.alibaba.fastjson.JSONObject;


public class NetPortListenerEx extends NetPortListener {

    public NetPortListenerEx(int port ,JSONObject jsonObject) {
        super(port,jsonObject );
    }
}
