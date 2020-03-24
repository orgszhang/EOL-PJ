package com.ht.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class QRCodeGenerator {
    private static final Log logger = LogFactory.getLog(QRCodeGenerator.class);
    private static final String checksum = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%";

    public static String getSeqNumber(String lastQRCode) {
        String seq = lastQRCode.substring(27, 33);

        if ("999999".equals(seq))
            return "AAAAAA";

        String newSeq = "";

        try {
            long seqL = Long.parseLong(seq);
            seqL++;
            newSeq = Long.toString(seqL);

            switch (newSeq.length()) {
                case 1:
                    newSeq = "00000" + newSeq;
                    break;
                case 2:
                    newSeq = "0000" + newSeq;
                    break;
                case 3:
                    newSeq = "000" + newSeq;
                    break;
                case 4:
                    newSeq = "00" + newSeq;
                    break;
                case 5:
                    newSeq = "0" + newSeq;
                    break;
                case 6:
                    break;
            }
        } catch (Exception exp) {

            if ("ZZZZZZ".equals(seq))
                return "000001";

            char[] seqChar = seq.toCharArray();

            if (seqChar[5] < 'Z') {
                seqChar[5]++;
            } else if (seqChar[5] == 'Z') {
                seqChar[5] = 'A';
                seqChar[4]++;

                if (seqChar[4] > 'Z') {
                    seqChar[4] = 'A';
                    seqChar[3]++;
                }

                if (seqChar[3] > 'Z') {
                    seqChar[3] = 'A';
                    seqChar[2]++;
                }

                if (seqChar[2] > 'Z') {
                    seqChar[2] = 'A';
                    seqChar[1]++;
                }
                if (seqChar[1] > 'Z') {
                    seqChar[1] = 'A';
                    seqChar[0]++;
                }
            }

            newSeq = String.copyValueOf(seqChar);
        }

        return newSeq;
    }

    public static String calQRCode(String factory, String seq) {
        String barcode = "#";
        barcode = barcode + factory;
        barcode = barcode + "  000###*1GU D5V ";
        barcode = barcode + seq;

        // 1GU D5V AABBEQ
        int sum = 0;
        sum = sum + checksum.indexOf('1');
        sum = sum + checksum.indexOf('G');
        sum = sum + checksum.indexOf('U');
        sum = sum + checksum.indexOf(' ');
        sum = sum + checksum.indexOf('D');
        sum = sum + checksum.indexOf('5');
        sum = sum + checksum.indexOf('V');
        sum = sum + checksum.indexOf(' ');

        char[] seqChar = seq.toCharArray();
        for (int i = 0; i < seqChar.length; i++) {
            sum = sum + checksum.indexOf(seqChar[i]);
        }

        int place = Math.floorMod(sum, 43);

        barcode = barcode + checksum.charAt(place);
        barcode = barcode + "*#";

        return barcode;
    }

    public static String gen(String lastOne, String factory) {
        logger.info("input- lastOne: " + lastOne + ", factory: " + factory);
        String seqNumber = getSeqNumber(lastOne);
        String barcode = calQRCode(factory, seqNumber);

        logger.info("barcode result: " + barcode);
        return barcode;
    }

    public static void main(String[] args) {
        String[] lastQRCode = {
                "#11D915743  000###*1GU D5V AABAUI3*#", "#11G915743  000###*1GU D5V AABBEQ$*#",
                "#11G915743  000###*1GU D5V BCCETZ$*#", "#11G915743  000###*1GU D5V FTTZZZ$*#",
                "#11G915743  000###*1GU D5V ZZZZZZ$*#", "#11G915743  000###*1GU D5V QZZZZZ$*#",
                "#11G915743  000###*1GU D5V TTZZZZ$*#", "#11G915743  000###*1GU D5V VXDCZZ$*#",
                "#11G915743  000###*1GU D5V ABCZZZ$*#", "#11G915743  000###*1GU D5V BCZTVZ$*#"
                /*"#11G915743  000###*1GU D5V 000005$*#", "#11G915743  000###*1GU D5V 003009$*#",
                "#11G915743  000###*1GU D5V 000999$*#", "#11G915743  000###*1GU D5V 009999$*#",
                "#11G915743  000###*1GU D5V 099999$*#", "#11G915743  000###*1GU D5V 689120$*#",
                "#11G915743  000###*1GU D5V 999999$*#", "#11G915743  000###*1GU D5V 059899$*#" */};

        String key = "11D915743";

        try {

            // String lastQR = gnr.readValue(key);
            for (int i = 0; i < lastQRCode.length; i++) {
                String s = QRCodeGenerator.getSeqNumber(lastQRCode[i]);
                System.out.println("-------------------------->" + s);

                String nextBarCode = QRCodeGenerator.calQRCode("11D915743", s);
                System.out.println(nextBarCode + "\n");
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
