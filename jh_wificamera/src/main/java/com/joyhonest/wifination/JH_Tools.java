package com.joyhonest.wifination;


import android.util.Log;

import org.simple.eventbus.EventBus;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by aiven on 2017/11/30.
 */

public class JH_Tools {

    //////////////
    private static int nCmdResType = 0;
    public static List<Byte> wifiData = new ArrayList<Byte>(100);
    private static List<MyCmdData> array = new ArrayList<MyCmdData>(100);


    public static void F_SetCmdResType(int n) {

    }

    private static class MyCmdData {
        private byte[] data = null;
        private int udpInx;
        //private int  nCmdResType = 0;


        public MyCmdData() {
            udpInx = -1;
        }

        public MyCmdData(int idx, byte[] dat) {

            data = null;
            if (dat != null) {
                if (dat.length > 4) {
                    data = new byte[dat.length - 4];
                    for (int ix = 0; ix < dat.length - 4; ix++) {
                        data[ix] = dat[ix + 4];
                    }
                }
            }
            udpInx = idx;
        }
    }

    public static void F_SetResType(int n) {
        nCmdResType = n;
    }


    public static boolean AdjData(byte[] revdata) {
        int idx;
        if (revdata.length <= 4)
            return false;

        idx = revdata[0] + revdata[1] * 0x100 + revdata[2] * 0x10000 + revdata[3] * 0x1000000;
        MyCmdData data = new MyCmdData(idx, revdata);
        MyCmdData data1;
        if (array.size() == 0) {
            array.add(0, data);
        } else {
            boolean bInsert = false;
            for (int i = 0; i < array.size(); i++) {
                data1 = array.get(i);
                if (idx < data1.udpInx) {
                    bInsert = true;
                    array.add(i, data);
                    break;
                } else if (idx == data1.udpInx) {
                    bInsert = true;
                    break;
                }
            }
            if (!bInsert) {
                array.add(data);
            }
        }
        return true;
    }

    private static boolean Process(byte[] data) {
        int ix;
        boolean bOK = false;
        if (data != null) {
            for (ix = 0; ix < data.length; ix++) {
                wifiData.add(data[ix]);
            }
            while (wifiData.size() >= 8) {

                if (wifiData.get(0).byteValue() == (byte) 0x66 && wifiData.get(7).byteValue() == (byte) 0x99) {
                    byte nChecksum = 0;
                    for (ix = 1; ix < 6; ix++) {
                        nChecksum = (byte) (nChecksum ^ wifiData.get(ix).byteValue());
                    }
                    if (nChecksum == wifiData.get(6).byteValue()) {
                        bOK = true;
                        byte[] revData = new byte[8];
                        for (ix = 0; ix < 8; ix++) {
                            revData[ix] = wifiData.get(ix).byteValue();
                        }
                        EventBus.getDefault().post(revData, "GetWifiSendData");
                        for (ix = 0; ix < 8; ix++) {
                            wifiData.remove(0);
                        }
                    } else {
                        wifiData.remove(0);
                    }
                } else {
                    wifiData.remove(0);
                }
            }

        }
        return bOK;
    }


    public static void FindCmd() {
        int nCount = 0;
        int nPre = 0;
        int idx = 0;
        int ix;
        int i;
        int nStart = 0;
        if (nCmdResType == 0) {
            for (i = 0; i < array.size(); i++) {
                MyCmdData d1 = array.get(i);
                if (i == 0) {
                    nCount = d1.data.length;
                    nPre = d1.udpInx;
                    nStart = i;
                    if (nCount >= 8) {
                        byte[] dat = new byte[nCount];
                        for (int yy = 0; yy < d1.data.length; yy++) {
                            dat[yy] = d1.data[yy];
                        }
                        Process(dat);
                        {
                            for (ix = i; ix >= 0; ix--) {
                                array.remove(ix);
                            }
                        }
                        break;
                    }
                } else {
                    idx = d1.udpInx;
                    if (idx - nPre == 1) {
                        nCount += d1.data.length;
                        if (nCount >= 8) {
                            int le = 0;
                            byte[] dat = new byte[nCount];
                            for (int xx = nStart; xx <= i; xx++) {
                                MyCmdData d2 = array.get(xx);
                                for (int yy = 0; yy < d2.data.length; yy++) {
                                    dat[le] = d2.data[yy];
                                    le++;
                                }
                            }
                            Process(dat);
                            {
                                for (ix = i; ix >= 0; ix--) {
                                    array.remove(ix);
                                }
                            }
                            break;
                        }
                    } else {
                        nCount = d1.data.length;
                        nStart = i;
                    }
                    nPre = idx;
                }
            }
        } else if (nCmdResType == 1) {
            F_ProgressResType1();
        }
    }


    private static int ProgressA(byte[] dataA, int nDatCount) {
        int INX = 0;
        int nP = 0;
        if (nDatCount > 2) {
            while (INX < nDatCount - 2) {
                byte flag0 = dataA[INX];
                byte flag1 = dataA[INX + 1];
                int abc = 0;
                if (flag0 == 0x58) {
                    if (flag1 == (byte) 0x83)   //58 83
                    {
                        abc = 1 + 2;
                    }
                    if (flag1 == (byte) 0x84)   //58 84
                    {
                        abc = 1 + 2;
                    }
                    if (flag1 == (byte) 0x8a)   //58 8a
                    {
                        abc = 12 + 2;
                    }
                    if (flag1 == (byte) 0x8b)   //58 8b
                    {
                        abc = 11 + 2;
                    }
                    if (flag1 == (byte) 0x8c)   //58 8c
                    {
                        abc = 13 + 2;
                    }
                    if (flag1 == (byte) 0x8e)   //58 8e
                    {
                        abc = 2 + 2;
                    }
                    if (abc == 0) {
                        INX++;
                    } else {
                        if (INX + 2 + abc < nDatCount) {
                            int zb = 0;
                            byte[] sentData = new byte[2 + abc];
                            System.arraycopy(dataA, INX, sentData, 0, 2 + abc);
                            byte check1 = 0;
                            byte chekc2 = 0;
                            chekc2 = sentData[abc + 1];
                            for (int dfs = 0; dfs < abc; dfs++) {
                                if (dfs == 0) {
                                    check1 = sentData[dfs + 1];
                                } else {
                                    check1 ^= sentData[dfs + 1];
                                }
                            }

                            if (check1 == chekc2) {
                                EventBus.getDefault().post(sentData, "GetWifiSendData");
                            }

                            INX += (2 + abc);
                            nP = INX;
                            if (flag1 == (byte) 0x8b) {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    ;
                                }
                            }

                        } else {
                            if (nP == 0)
                                nP = -1;
                            break;
                        }
                    }
                } else {
                    INX++;
                }
            }
        }
        return nP;
    }

    private static void F_ProgressResType1() {


        int dx = 0;

        if (array.size() <= 0)
            return;


        MyCmdData d1 = array.get(0);
        int len1 = d1.data.length;
        int ret = ProgressA(d1.data, d1.data.length);
        if (ret <= 0) {
            if (array.size() >= 2) {
                MyCmdData d2 = array.get(1);
                int idx1 = d1.udpInx;
                int idx2 = d2.udpInx;
                int len2 = d2.data.length;
                if (idx2 - idx1 == 1) {
                    byte[] tmpdata = new byte[len1];
                    System.arraycopy(d1.data, 0, tmpdata, 0, len1);
                    d1.data = new byte[len1 + len2];
                    System.arraycopy(tmpdata, 0, d1.data, 0, len1);
                    System.arraycopy(d2.data, 0, d1.data, len1, len2);
                    d1.udpInx = idx2;
                    array.remove(1);
                    F_ProgressResType1();
                } else {

                }
            }
        } else if (ret > 0) {
            dx = len1 - ret;
            if (dx > 0) {
                byte[] tmpdata = new byte[dx];
                System.arraycopy(d1.data, ret, tmpdata, 0, dx);
                d1.data = new byte[dx];
                System.arraycopy(tmpdata, 0, d1.data, 0, dx);
            } else if (dx == 0) {
                array.remove(0);
            }
        }


/*

        for (i = 0; i < array.size(); i++)
        {
            MyCmdData d1 = array.get(i);
            int nLen1= d1.data.length;
            if(i==0)
            {
                nPre = d1.udpInx;
                if(nLen1<=2048)
                {
                    for(int xx=0;xx<nLen1;xx++)
                    {
                        dat[xx] = d1.data[xx];
                        datINX[xx] = i;
                    }
                    nDatCount=nLen1;
                    nProsed = ProgressA(dat,nDatCount);
                    if(nProsed>0)
                    {
                        dx = nLen1-nProsed;
                        if(dx<=0)
                        {
                            array.remove(0);
                        }
                        else {
                             byte[] dba = new byte[dx];
                             for(int xx = 0;xx<dx;xx++)
                             {
                                 dba[xx] = d1.data[xx+nProsed];
                             }

                             d1.data = new byte[dx];
                             for(int xx = 0;xx<dx;xx++)
                             {
                                d1.data[xx]=dba[xx];
                             }
                        }
                        break;
                    }
                }
            }
            else
            {
                idx = d1.udpInx;
                if(idx-nPre==1)
                {
                    if(nLen1+nDatCount<=2048)
                    {
                        for(int xx=0;xx<nLen1;xx++)
                        {
                            dat[xx+nDatCount] = d1.data[xx];
                            datINX[xx] = i;
                        }
                        nDatCount+=nLen1;
                        nProsed = ProgressA(dat,nDatCount);
                        if(nProsed>0)
                        {
                            dx = nLen1-nProsed;
                            if(dx<=0)
                            {
                                array.remove(0);
                            }
                            else {
                                byte[] dba = new byte[dx];
                                for(int xx = 0;xx<dx;xx++)
                                {
                                    dba[xx] = d1.data[xx+nProsed];
                                }

                                d1.data = new byte[dx];
                                for(int xx = 0;xx<dx;xx++)
                                {
                                    d1.data[xx]=dba[xx];
                                }
                            }
                            break;
                        }

                    }
                }
                else
                {
                    break;
                }
                nPre= d1.udpInx;
            }
        }
        */


    }

    public static void F_ClearData() {
        if (array.size() > 5) {
            array.clear();
            wifiData.clear();
        }
    }

}
