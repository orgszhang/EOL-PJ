package com.ht.utils;

public class TempCalculator {
    private static double AbLow = 168.543373125389;
    private static double AbHigh = 1025999.36296925;
    private static double[] T = {
            -55.0, -54.0, -53.0, -52.0, -51.0, -50.0,    /*-50*/
            -49.0, -48.0, -47.0, -46.0, -45.0, -44.0, -43.0, -42.0, -41.0, -40.0,    /*-40*/
            -39.0, -38.0, -37.0, -36.0, -35.0, -34.0, -33.0, -32.0, -31.0, -30.0,    /*-30*/
            -29.0, -28.0, -27.0, -26.0, -25.0, -24.0, -23.0, -22.0, -21.0, -20.0,    /*-20*/
            -19.0, -18.0, -17.0, -16.0, -15.0, -14.0, -13.0, -12.0, -11.0, -10.0,    /*-10*/
            -9.0, -8.0, -7.0, -6.0, -5.0, -4.0, -3.0, -2.0, -1.0, 0.0,    /*0*/
            1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0,    /*10*/
            11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0,    /*20*/
            21.0, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 28.0, 29.0, 30.0,    /*30*/
            31.0, 32.0, 33.0, 34.0, 35.0, 36.0, 37.0, 38.0, 39.0, 40.0,    /*40*/
            41.0, 42.0, 43.0, 44.0, 45.0, 46.0, 47.0, 48.0, 49.0, 50.0,    /*50*/
            51.0, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0,    /*60*/
            61.0, 62.0, 63.0, 64.0, 65.0, 66.0, 67.0, 68.0, 69.0, 70.0,    /*70*/
            71.0, 72.0, 73.0, 74.0, 75.0, 76.0, 77.0, 78.0, 79.0, 80.0,    /*80*/
            81.0, 82.0, 83.0, 84.0, 85.0, 86.0, 87.0, 88.0, 89.0, 90.0,    /*90*/
            91.0, 92.0, 93.0, 94.0, 95.0, 96.0, 97.0, 98.0, 99.0, 100.0,    /*100*/
            101.0, 102.0, 103.0, 104.0, 105.0, 106.0, 107.0, 108.0, 109.0, 110.0,    /*110*/
            111.0, 112.0, 113.0, 114.0, 115.0, 116.0, 117.0, 118.0, 119.0, 120.0,    /*120*/
            121.0, 122.0, 123.0, 124.0, 125.0, 126.0, 127.0, 128.0, 129.0, 130.0,    /*130*/
            131.0, 132.0, 133.0, 134.0, 135.0, 136.0, 137.0, 138.0, 139.0, 140.0,    /*140*/
            141.0, 142.0, 143.0, 144.0, 145.0, 146.0, 147.0, 148.0, 149.0, 150.0,    /*150*/
    };

    private static double[] R = {
            953773.54984637,    /*-- -55 --*/
            886261.171436372,   /*-- -54 --*/
            823958.701165513,   /*-- -53 --*/
            766433.156477172,   /*-- -52 --*/
            713289.955018661,   /*-- -51 --*/
            664169.306275499,   /*-- -50 --*/
            618742.960382899,   /*-- -49 --*/
            576711.277049759,   /*-- -48 --*/
            537800.581546243,   /*-- -47 --*/
            501760.778269041,   /*-- -46 --*/
            468363.195561767,   /*-- -45 --*/
            437398.638278118,   /*-- -44 --*/
            408675.627073181,   /*-- -43 --*/
            382018.805629966,   /*-- -42 --*/
            357267.499005122,   /*-- -41 --*/
            334274.408038298,   /*-- -40 --*/
            312904.426337947,   /*-- -39 --*/
            293033.567754614,   /*-- -38 --*/
            274547.993499881,   /*-- -37 --*/
            257343.129182167,   /*-- -36 --*/
            241322.863024554,   /*-- -35 --*/
            226398.817417852,   /*-- -34 --*/
            212489.686756033,   /*-- -33 --*/
            199520.635211332,   /*-- -32 --*/
            187422.748741824,   /*-- -31 --*/
            176132.536193454,   /*-- -30 --*/
            165591.474868361,   /*-- -29 --*/
            155745.596388443,   /*-- -28 --*/
            146545.109093089,   /*-- -27 --*/
            137944.05357789,    /*-- -26 --*/
            129899.988311491,   /*-- -25 --*/
            122373.702564493,   /*-- -24 --*/
            115328.954151032,   /*-- -23 --*/
            108732.229723516,   /*-- -22 --*/
            102552.525576825,   /*-- -21 --*/
            96761.1471125443,   /*-- -20 --*/
            91331.525288764,    /*-- -19 --*/
            86239.0485386777,   /*-- -18 --*/
            81460.9087833326,   /*-- -17 --*/
            76975.9602921376,   /*-- -16 --*/
            72764.5902604295,   /*-- -15 --*/
            68808.6000778985,   /*-- -14 --*/
            65091.096356047,    /*-- -13 --*/
            61596.3908681649,   /*-- -12 --*/
            58309.9086324387,   /*-- -11 --*/
            55218.1034385899,   /*-- -10 --*/
            52308.3801815949,   /*-- -9 --*/
            49569.0234232305,   /*-- -8 --*/
            46989.1316539966,   /*-- -7 --*/
            44558.5567749289,   /*-- -6 --*/
            42267.8483613955,   /*-- -5 --*/
            40108.2023095982,   /*-- -4 --*/
            38071.4135015657,   /*-- -3 --*/
            36149.8321562554,   /*-- -2 --*/
            34336.3235633079,   /*-- -1 --*/
            32624.2309222763,   /*-- 0 --*/
            31007.3410340566,   /*-- 1 --*/
            29479.8526129802,   /*-- 2 --*/
            28036.347007817,    /*-- 3 --*/
            26671.7611379451,   /*-- 4 --*/
            25381.3624673524,   /*-- 5 --*/
            24160.7258540824,   /*-- 6 --*/
            23005.7121263617,   /*-- 7 --*/
            21912.4482490808,   /*-- 8 --*/
            20877.3089556319,   /*-- 9 --*/
            19896.8997304619,   /*-- 10 --*/
            18968.0410371474,   /*-- 11 --*/
            18087.7536954321,   /*-- 12 --*/
            17253.2453185566,   /*-- 13 --*/
            16461.8977294238,   /*-- 14 --*/
            15711.2552807396,   /*-- 15 --*/
            14999.0140103068,   /*-- 16 --*/
            14323.0115681728,   /*-- 17 --*/
            13681.2178573926,   /*-- 18 --*/
            13071.7263348045,   /*-- 19 --*/
            12492.7459224612,   /*-- 20 --*/
            11942.5934842541,   /*-- 21 --*/
            11419.6868258393,   /*-- 22 --*/
            10922.5381792525,   /*-- 23 --*/
            10449.7481366025,   /*-- 24 --*/
            10000,              /*-- 25 --*/
            9572.05451740929,   /*-- 26 --*/
            9164.74497644443,   /*-- 27 --*/
            8776.97263027142,   /*-- 28 --*/
            8407.7024317484,    /*-- 29 --*/
            8055.95905374706,   /*-- 30 --*/
            7720.82317526619,   /*-- 31 --*/
            7401.42801448248,   /*-- 32 --*/
            7096.95609129679,   /*-- 33 --*/
            6806.63620323624,   /*-- 34 --*/
            6529.74059977152,   /*-- 35 --*/
            6265.58234121496,   /*-- 36 --*/
            6013.5128293843,    /*-- 37 --*/
            5772.91949815766,   /*-- 38 --*/
            5543.2236529133,    /*-- 39 --*/
            5323.87844864872,   /*-- 40 --*/
            5114.36699731345,   /*-- 41 --*/
            4914.20059557332,   /*-- 42 --*/
            4722.91706485508,   /*-- 43 --*/
            4540.07919610424,   /*-- 44 --*/
            4365.27329222826,   /*-- 45 --*/
            4198.10780169632,   /*-- 46 --*/
            4038.21203722881,   /*-- 47 --*/
            3885.234973937,     /*-- 48 --*/
            3738.8441216686,    /*-- 49 --*/
            3598.724466682,     /*-- 50 --*/
            3464.57747811068,   /*-- 51 --*/
            3336.12017499436,   /*-- 52 --*/
            3213.08424994471,   /*-- 53 --*/
            3095.21524578382,   /*-- 54 --*/
            2982.27178174455,   /*-- 55 --*/
            2874.02482605431,   /*-- 56 --*/
            2770.2570119399,    /*-- 57 --*/
            2670.76199429139,   /*-- 58 --*/
            2575.34384440935,   /*-- 59 --*/
            2483.8164804323,    /*-- 60 --*/
            2396.00313120242,   /*-- 61 --*/
            2311.73583147648,   /*-- 62 --*/
            2230.85494652803,   /*-- 63 --*/
            2153.20872431598,   /*-- 64 --*/
            2078.65287351475,   /*-- 65 --*/
            2007.05016581319,   /*-- 66 --*/
            1938.27006099303,   /*-- 67 --*/
            1872.18835339528,   /*-- 68 --*/
            1808.68683847231,   /*-- 69 --*/
            1747.65299820821,   /*-- 70 --*/
            1688.97970426773,   /*-- 71 --*/
            1632.56493780763,   /*-- 72 --*/
            1578.31152495177,   /*-- 73 --*/
            1526.12688699539,   /*-- 74 --*/
            1475.92280446264,   /*-- 75 --*/
            1427.61519419718,   /*-- 76 --*/
            1381.12389871677,   /*-- 77 --*/
            1336.3724871114,    /*-- 78 --*/
            1293.28806680915,   /*-- 79 --*/
            1251.80110557593,   /*-- 80 --*/
            1211.84526315489,   /*-- 81 --*/
            1173.35723198742,   /*-- 82 --*/
            1136.27658649243,   /*-- 83 --*/
            1100.54564041225,   /*-- 84 --*/
            1066.1093117639,    /*-- 85 --*/
            1032.91499496209,   /*-- 86 --*/
            1000.91243970682,   /*-- 87 --*/
            970.053636253032,   /*-- 88 --*/
            940.292706702631,   /*-- 89 --*/
            911.585801980749,   /*-- 90 --*/
            883.891004178446,   /*-- 91 --*/
            857.168233962742,   /*-- 92 --*/
            831.379162772742,   /*-- 93 --*/
            806.48712953712,    /*-- 94 --*/
            782.457061663869,   /*-- 95 --*/
            759.255400067738,   /*-- 96 --*/
            736.850028014576,   /*-- 97 --*/
            715.210203574526,   /*-- 98 --*/
            694.306495488191,   /*-- 99 --*/
            674.110722261108,   /*-- 100 --*/
            654.595894312557,   /*-- 101 --*/
            635.736159014697,   /*-- 102 --*/
            617.506748467352,   /*-- 103 --*/
            599.883929862637,   /*-- 104 --*/
            582.844958301841,   /*-- 105 --*/
            566.368031934803,   /*-- 106 --*/
            550.432249299259,   /*-- 107 --*/
            535.017568744619,   /*-- 108 --*/
            520.10476983099,    /*-- 109 --*/
            505.675416600424,   /*-- 110 --*/
            491.711822623062,   /*-- 111 --*/
            478.197017726224,   /*-- 112 --*/
            465.114716319598,   /*-- 113 --*/
            452.449287234422,   /*-- 114 --*/
            440.185724999083,   /*-- 115 --*/
            428.309622477755,   /*-- 116 --*/
            416.80714480274,    /*-- 117 --*/
            405.665004534872,   /*-- 118 --*/
            394.870437989965,   /*-- 119 --*/
            384.411182672554,   /*-- 120 --*/
            374.275455761376,   /*-- 121 --*/
            364.451933594009,   /*-- 122 --*/
            354.929732100857,   /*-- 123 --*/
            345.698388141348,   /*-- 124 --*/
            336.747841697694,   /*-- 125 --*/
            328.068418883918,   /*-- 126 --*/
            319.650815730078,   /*-- 127 --*/
            311.486082703719,   /*-- 128 --*/
            303.56560993257,    /*-- 129 --*/
            295.881113094367,   /*-- 130 --*/
            288.424619941466,   /*-- 131 --*/
            281.188457429566,   /*-- 132 --*/
            274.165239421471,   /*-- 133 --*/
            267.347854938273,   /*-- 134 --*/
            260.729456931794,   /*-- 135 --*/
            254.303451553427,   /*-- 136 --*/
            248.063487895803,   /*-- 137 --*/
            242.003448184894,   /*-- 138 --*/
            236.1174384013,     /*-- 139 --*/
            230.399779310522,   /*-- 140 --*/
            224.844997883078,   /*-- 141 --*/
            219.447819086216,   /*-- 142 --*/
            214.203158029948,   /*-- 143 --*/
            209.106112450944,   /*-- 144 --*/
            204.151955518671,   /*-- 145 --*/
            199.33612894891,    /*-- 146 --*/
            194.654236410529,   /*-- 147 --*/
            190.102037212086,   /*-- 148 --*/
            185.67544025548,    /*-- 149 --*/
            181.370498244505,   /*-- 150 --*/
    };


    public static void main(String[] args) {
        TempCalculator cal = new TempCalculator();

        double test[] = {141180.930};

        for (int j = 0; j < test.length; j++) {
            double temp = cal.QCalTemp(test[j]); /* Change functions here */
            if (temp < -40) {
                System.out.println("Temperature is too low to calculate\r=================");
            } else if (temp > 150) {
                System.out.println("Temperature is too high to calculate\r=================");
            } else {
                System.out.println(temp + "\r=================");
            }
        }
    }


    public static double CalTemp(double ntc) {
        if (ntc > R[0]) {
            if (ntc > AbHigh)
                return -2000;
            else
                return -55.0;
        }
        if (ntc < R[205]) {
            if (ntc < AbLow)
                return 2000;
            else
                return 150;
        }

        double Tx = 0;
        int i = 0;
        while (ntc < R[i]) {
            i = i + 1;
        }

        double Rl = R[i];
        double Ru = R[i - 1];

        // System.out.println(i);
        // System.out.println(Rl);
        // System.out.println(Ru);
        // System.out.println(T[i]);

        double t = (ntc - Rl) / (Ru - Rl);
        Tx = T[i] - t * 1;

        return Tx;
    }

    public static double QCalTemp(double ntc) {
        if (ntc > R[0]) {
            if (ntc > AbHigh)
                return -2000;
            else
                return -55.0;
        }
        if (ntc < R[205]) {
            if (ntc < AbLow)
                return 2000;
            else
                return 150;
        }

        double Tx = 0;

        int st = 0;
        int ed = R.length - 1;
        int m = 0;

        while (st < ed) {
            m = (st + ed) / 2;
            // System.out.println(st + "  --  " + m + "  --  " + ed);

            if (ntc == R[m]) break;
            if (st == ed || st + 1 == ed) break;

            if (ntc > R[m]) ed = m;
            else st = m;
        }


        double Ru = R[m];
        double Rl = R[m + 1];

        // System.out.println(m);
        // System.out.println(Rl);
        // System.out.println(Ru);
        // System.out.println(T[m]);

        double t = (ntc - Rl) / (Ru - Rl);
        Tx = T[m + 1] - t * 1;

        return Tx;
    }
}
