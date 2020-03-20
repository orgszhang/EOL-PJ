package com.ht.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;

public interface KeySightVci extends Library {
    String dllName = ".\\libs\\visa32";
    /**
     * 默认的成功
     */
    int STATUS_FAIL = 1;
    /**
     * 默认的失败
     */
    int STATUS_OK = 0;

    KeySightVci KEYSIGHTINSTANCE = Native.loadLibrary(dllName, KeySightVci.class);

    /**
     * 打开设备前需要的操作
     * @param session 设备启动信息
     * @return
     */
    int viOpenDefaultRM(LongByReference session);

    /**
     * 打开设备
     * @param viSession
     * @param rsrcName
     * @param accessMode
     * @param timeout
     * @param session
     * @return
     */
    int viOpen(NativeLong viSession, String rsrcName,
               NativeLong accessMode, NativeLong timeout,
               LongByReference session);

    int viClose(NativeLong vi);

    int viScanf(NativeLong vi, String readFmt, Object... args);

    int viPrintf(NativeLong vi, String writeFmt, Object... args);

    int viSetAttribute(NativeLong vi, NativeLong AttrbuteName, String Status);
}
