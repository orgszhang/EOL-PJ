package com.ht.jna;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KeySightDevice_Voltage extends KeySightDevice {
    private static final Log logger = LogFactory.getLog(KeySightDevice_Voltage.class);

    public boolean open() {
        return open("", "");
    }

    public boolean setVolCONF() {
        if (isSetVol) {
            return true;
        }

        if (writeCmd("CONF:VOLT 0.1", null, null, null)
                && writeCmd("CONF:VOLT 0.1", null, null, null)) {
            logger.info("设置为DC电压模式");
            isSetVol = true;
            return true;
        }
        return false;
    }
}