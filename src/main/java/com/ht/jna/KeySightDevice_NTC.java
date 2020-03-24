package com.ht.jna;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KeySightDevice_NTC extends KeySightDevice {
    private static final Log logger = LogFactory.getLog(KeySightDevice_NTC.class);

    public boolean open() {
        return open("", "");
    }

    public boolean setRCONF() {
        if (writeCmd("CONF:RESistance 1000000", null, null, null)
                && writeCmd("CONF:RESistance 1000000", null, null, null)) {
            logger.info("设置为电阻模式");
            return true;
        }
        return false;
    }
}