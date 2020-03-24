package com.ht.jna;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KeySightDevice_Electricity extends KeySightDevice {
    private static final Log logger = LogFactory.getLog(KeySightDevice_Electricity.class);

    public boolean open() {
        return open("", "");
    }

    public boolean setEleCONF() {
        if (isSetEle) {
            return true;
        }

        if (writeCmd("CONF:CURR 3", null, null, null)
                && writeCmd("CURR:DC:RANG 3", null, null, null)) {
            logger.info("设置为DC电流模式");
            isSetEle = true;
            return true;
        }
        return false;
    }
}