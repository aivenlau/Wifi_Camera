package com.joyhonest.wifination;

import android.util.Log;

import org.simple.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class GP4225_Device {

    int nInxCount = 0;


    public int nAdjAngle = 0;

    int[] nCountX = new int[10];
    int[] nCountY = new int[10];
    int[] nCountZ = new int[10];
    public boolean bProcessesGsensorDataByApp = false;
    public boolean bAdjGsensorData = true;


    public int nMode;
    public boolean bSD;
    public boolean bSDRecording;

    public int VideosCount;
    public int LockedCount;
    public int PhotoCount;

    public long nSDAllSize;        //1024*1024 unit
    public long nSDAvaildSize;

    public int nBattery;
    public boolean bAdjfocus = false;

    public int nFuncMask = 0;


    public int nSDRecordTime = 0;

    public String sVer = "";


    private int nXX_pre = 0;
    private int nYY_pre = 0;
    private int nZZ_pre = 0;



//    public List<MyFile> PhotoFileList;
//    public List<MyFile> LockedFileList;
//    public List<MyFile> VideoFileList;

    public GP4225_Device() {
        for (int x = 0; x < 10; x++) {
            nCountX[x] = 0;
            nCountY[x] = 0;
            nCountZ[x] = 0;
        }
//        PhotoFileList = new ArrayList<>();
//        LockedFileList = new ArrayList<>();
//        VideoFileList = new ArrayList<>();
        nBattery = -1;
        VideosCount = 0;
        LockedCount = 0;
        PhotoCount = 0;
        bSD = false;
        nSDAllSize = -1;
        nSDAvaildSize = -1;

        bSDRecording = false;
        nMode = 0;
    }


//    private  void F_AddFile(int i,MyFile file)
//    {
//        if(i==1)
//        {
//            VideoFileList.add(file);
//        }
//        if(i==2)
//        {
//            LockedFileList.add(file);
//        }
//        if(i==3)
//        {
//            PhotoFileList.add(file);
//        }
//    }
//
//
//    public  void F_ClearAllList()
//    {
//        VideoFileList.clear();
//        LockedFileList.clear();
//        PhotoFileList.clear();
//    }


    byte bytes[] = new byte[32];
    int GsensorData[] = new int[3];


    int nMax = 0;
    int nMin = 0x7FFFF;

    private int GXX = 0;
    private int GYY = 0;
    private int GZZ = 0;


    private float alpha = (0.00033f);
    private float alpha1 = (1 - 0.00033f);


    private int nNoesie = 0;
    private int nNoesie_No = 0;
    private boolean bFlash = false;


    private  int G_xx[]=new int[20];
    private  int G_yy[]=new int[20];
    private  int G_zz[]=new int[20];
    private  int nG_inx = 0;
    private  long nCount1=0;
    private  long nCount2=0;

    public boolean GP4225_PressData(byte[] data) {
        int nStartInx, nEndInx, inx, nLen;
        if (data == null)
            return false;
        if (data.length <= 10)
            return false;

        if ((data[0] & 0xFF) != 'F' ||
                (data[1] & 0xFF) != 'D' ||
                (data[2] & 0xFF) != 'W' ||
                (data[3] & 0xFF) != 'N') {
            return false;
        }

        String sFileName = "";
        byte nStatus = 0;

        int m_cmd = (data[4] & 0xFF) + (data[5] & 0xFF) * 0x100;
        int s_cmd = (data[6] & 0xFF) + (data[7] & 0xFF) * 0x100;
        int n_len = (data[8] & 0xFF) + (data[9] & 0xFF) * 0x100;
        if (n_len == 0)
            return false;
        if (n_len + 10 > data.length)
            return false;

        if (m_cmd == 0x0000 && s_cmd == 0x0001) {   //Device Status
            nMode = data[10] & 0xFF;
            bSD = ((data[11] & 0x01) == 0); // 0 have SD  1 NoSD
            bSDRecording = ((data[11] & 0x02) != 0);

            VideosCount = ((data[12] & 0xFF) + (data[13] & 0xFF) * 0x100 + (data[14] & 0xFF) * 0x10000 + (data[15] & 0xFF) * 0x1000000);
            LockedCount = ((data[16] & 0xFF) + (data[17] & 0xFF) * 0x100 + (data[18] & 0xFF) * 0x10000 + (data[19] & 0xFF) * 0x1000000);
            PhotoCount = ((data[20] & 0xFF) + (data[21] & 0xFF) * 0x100 + (data[22] & 0xFF) * 0x10000 + (data[23] & 0xFF) * 0x1000000);

            if (n_len >= 0x1A) {
                nSDAllSize = ((data[24] & 0xFF) + (data[25] & 0xFF) * 0x100 + (data[26] & 0xFF) * 0x10000 + (data[27] & 0xFF) * 0x1000000 + (data[34] & 0xFF) * 0x100000000L);
                nSDAvaildSize = ((data[28] & 0xFF) + (data[29] & 0xFF) * 0x100 + (data[30] & 0xFF) * 0x10000 + (data[31] & 0xFF) * 0x1000000 + (data[35] & 0xFF) * 0x100000000L);
            }
            if (data.length >= 34) {
                nBattery = data[32] & 0xFF;
                bAdjfocus = (data[33] != 0);
                if (data.length >= 40) {
                    nSDRecordTime = (data[36] & 0xFF) + (data[37] & 0xFF) * 0x100 + (data[38] & 0xFF) * 0x10000 + (data[39] & 0xFF) * 0x1000000;
                } else {
                    nSDRecordTime = 0;
                }
                if (data.length >= 35) {
                    nFuncMask = data[34] & 0xFF;
                }
            } else {
                nFuncMask = 0;
                nBattery = 4;
                bAdjfocus = false;
                nSDRecordTime = 0;
            }
            EventBus.getDefault().post("", "GP4225_GetStatus");
            return true;
        }
        if (m_cmd == 0x0002)  //GetFileList
        {
            if (s_cmd == 0x0001 || s_cmd == 0x0002 || s_cmd == 0x0003) {  //VideoList   LockFileList  //图片文件
                nStartInx = (data[10] & 0xFF) + (data[11] & 0xFF) * 0x100;
                nEndInx = (data[12] & 0xFF) + (data[13] & 0xFF) * 0x100;

                GetFiles FF = new GetFiles();

                FF.files = new ArrayList<>();
                for (int ii = 0; ii <= nEndInx - nStartInx; ii++) {

                    inx = 14 + 32 + (ii * 68);
                    nLen = (data[inx] & 0xFF) + (data[inx + 1] & 0xFF) * 0x100 + (data[inx + 2] & 0xFF) * 0x10000 + (data[inx + 3] & 0xFF) * 0x1000000;
                    inx += 4;
                    int da = 0;
                    for (int xx = 0; xx < 32; xx++) {
                        if (data[inx + xx] != 0) {
                            da++;
                        }
                    }
                    sFileName = "";
                    if (da != 0) {
                        System.arraycopy(data, inx, bytes, 0, da);
                        sFileName = new String(bytes, 0, da);
                    }
                    MyFile file = new MyFile("", sFileName, nLen);
                    file.nInx1 = nStartInx;
                    file.nInx2 = nEndInx;
                    FF.files.add(file);
                    //EventBus.getDefault().post(file,"GP4225_RevFile");
                }
                EventBus.getDefault().post(FF, "GP4225_RevFiles");
            }
            return true;
        }

        if (m_cmd == 0x0009)  //Delete File
        {
            nStatus = data[10];
            sFileName = "";
            if (n_len > 64) {
                nLen = 0;
                for (int xx = 0; xx < 32; xx++) {
                    if (data[xx + 11 + 32] == 0) {
                        break;
                    } else {
                        nLen++;
                    }
                }
                if (nLen != 0)
                    sFileName = new String(data, 11 + 32, nLen);
            }

            switch (s_cmd) {
                case 0x00001:          //delete a file
                    MyFile file = new MyFile("", sFileName, (int) nStatus);
                    EventBus.getDefault().post(file, "GP4225_DeleteFile");
                    break;
                case 0x0002:             //delete all file
                    Integer i = (int) nStatus;
                    EventBus.getDefault().post(i, "GP4225_DeleteAllFile");
                    break;
            }
            return true;
        }
        if (m_cmd == 0x0021) {
            if (s_cmd == 0x0001) {   //透传数据
                if (n_len != 0) {
                    byte[] buffer = new byte[n_len];
                    System.arraycopy(data, 10, buffer, 0, n_len);
                    EventBus.getDefault().post(buffer, "GP4225_Get_Transfer_data");
                    return true;
                }
            }
            return false;
        }
        if (m_cmd == 0x0020) {
            boolean bOK = true;
            switch (s_cmd) {
                case 0x0001: //时间
                {
                    byte[] buffer = new byte[n_len];
                    System.arraycopy(data, 10, buffer, 0, n_len);
                    EventBus.getDefault().post(buffer, "GP4225_GetDeviceDateTime");
                }

                break;
                case 0x0002: //水印开关
                {
                    byte a = data[10];
                    Integer aa = (int) a;
                    EventBus.getDefault().post(aa, "GP4225_GetDeviceOsdStatus");
                }

                break;
                case 0x0003:  //图像翻转
                {
                    byte a = data[10];
                    Integer aa = (int) a;
                    Log.e("图像", a + "");
                    EventBus.getDefault().post(aa, "GP4225_GetDeviceReversaltatus");
                }

                break;
                case 0x0004: //录像分段时间
                {
                    byte a = data[10];
                    Integer aa = (int) a;
                    Log.e("录像时间", a + "");
                    EventBus.getDefault().post(aa, "GP4225_GetDeviceRecordTime");
                }
                break;
//                case 0x0005:
//                    break;
//                case 0x0006:
//                    break;
                case 0x0007: //WifiChannel
                {
                    byte a = data[10];
                    Integer aa = (int) a;
                    Log.e("Format", a + "");
                    EventBus.getDefault().post(aa, "GP4225_WifiChannel");
                }
                break;
                case 0x0008:  //format SD卡
                {
                    byte a = data[10];
                    Integer aa = (int) a;
                    Log.e("Format", a + "");
                    EventBus.getDefault().post(aa, "GP4225_FormatSD_Status");
                }
                break;
                case 0x0009:  //Ver
                {
                    byte[] buffer = new byte[n_len];
                    System.arraycopy(data, 10, buffer, 0, n_len);
                    sVer = new String(buffer);
                    Log.e("ABC", sVer);
                    EventBus.getDefault().post(sVer, "GP4225_GetFireWareVersion");
                }
                break;
                case 0x000A: {
                    byte a = data[10];
                    Integer aa = (int) a;
                    EventBus.getDefault().post(aa, "GP4225_Reset_Status");
                }
                break;
                case 0x000B: {
                    int val = (data[10] & 0xFF) + (data[11] & 0xFF) * 0x100;
                    val >>= 4;
                    val &= 0x3FF;
                    Integer aa = val;
                    EventBus.getDefault().post(aa, "GP4225_34_GetAdjFocus");
                }
                break;
                case 0x000C: {
                    int val = (data[10] & 0xFF) + (data[11] & 0xFF) * 0x100;
                    Integer aa = (int) val;
                    EventBus.getDefault().post(aa, "GP4225_GetVcm");
                }
                break;
                case 0x000E:  //GP4225_GetLed
                {
                    byte a = data[10];
                    Integer aa = (int) a;
                    EventBus.getDefault().post(aa, "GP4225_GetLed");
                }

                break;
                case 0x0010: //GP4225_GetResolution
                {
                    byte a = data[10];
                    Integer aa = (int) a;
                    EventBus.getDefault().post(aa, "GP4225_GetResolution");
                }
                break;
                case 0x0012:    //G-Sensor 数据
                {
                    if (n_len == 8) {
                        int status = data[10] & 0xFF + (data[11] & 0xFF) * 0x100;
                        int yy = (data[12] & 0xFF) | ((data[13]) * 0x100);
                        int xx = (data[14] & 0xFF) | ((data[15]) * 0x100);
                        int zz = (data[16] & 0xFF) | ((data[17]) * 0x100);
                        zz = -zz;
//                        if(nG_inx<10)
//                        {
//                            G_xx[nG_inx]=xx;
//                            nG_inx++;
//                            break;
//                        }
//                        for(int x =1;x<10;x++)
//                        {
//                            G_xx[x-1]=G_xx[x];
//                        }
//                        G_xx[9]=xx;
//                        long countx = 0;
//                        for(int x=0;x<10;x++)
//                        {
//                            countx+=G_xx[x];
//                            if(x==0)
//                            {
//                                nMax = G_xx[0];
//                                nMin = G_xx[0];
//                            }
//                            else {
//                                if (G_xx[x] > nMax) {
//                                    nMax = G_xx[x];
//                                }
//                                if (G_xx[x] < nMin) {
//                                    nMin = G_xx[x];
//                                }
//                            }
//                        }
//                        countx-=nMax;
//                        countx-=nMin;
//                        xx = (int)(countx/8);

                        int nLevelMax = 210;
                        int nLevelMin = 60;

                        boolean bStatus = ((status & 0x1) == 0);
                        boolean bAdj = (((status >> 1) & 0x01) == 1);
                        int nType = (status >> 8) & 0xFF;
                        int nCount = 1;
//                        xx = 0;
//                        yy = 0;
//                        zz = 1024;
                        //nType = 1;
//                        Log.e("TTT2","XX = "+xx+"  YY = "+yy+" zz ="+zz);
//                        Log.e("TTT0"," zz ="+zz);
                        if (bStatus)
                        {
                            if (nType == 0)
                            {
                                nLevelMax = 210;
                                nLevelMin = 60;
                                xx /= 4;
                                yy /= 4;
                                zz /= 4;
                            }
                            else if (nType == 1)
                            {
                                nLevelMax = 200;
                                nLevelMin = 50;
                                nCount = 2;
                                xx /= 2;        //4
                                yy /= 2;
                                zz /= 2;
                            }
                            else
                            {
                                nLevelMax = 180;
                                nLevelMin = 40;
                                xx /= 2;        //4
                                yy /= 2;
                                zz /= 2;
                            }
                            int daV = Math.abs(xx - nXX_pre);
                            if (bProcessesGsensorDataByApp) {
                                GsensorData gsensorData = new GsensorData();
                                gsensorData.x = xx;
                                gsensorData.y = yy;
                                gsensorData.z = zz;
                                EventBus.getDefault().post(gsensorData, "GP4225_GetGsensorData");
                            } else {

                                if (bAdjGsensorData) {
                                    if (daV > nLevelMax) //220
                                    {
                                        nNoesie++;
                                        if (nNoesie > 20) {
                                            nNoesie = 20;
                                        }
                                        if (nNoesie > nCount) {
                                            if (!bFlash) {
                                                nNoesie_No = 0;
                                                bFlash = true;
                                            }
                                        }
                                    } else {
                                        if (bFlash) {
                                            if (daV < nLevelMin) //60
                                            {
                                                if (nNoesie_No < 20) {
                                                    nNoesie_No++;
                                                } else {
                                                    bFlash = false;
                                                    nNoesie = 0;
                                                //    Log.e("TT5","NOFlash");
                                                }
                                            } else{
                                                nNoesie_No = 0;
                                            }
                                        }
                                    }
                                    if (!bFlash) {
                                            wifination.naSetGsensor2SDK(xx + nAdjAngle, yy, zz);
                                    }
                                    nXX_pre = xx;
                                    nYY_pre = yy;
                                    nZZ_pre = zz;
                                } else {
                                        wifination.naSetGsensor2SDK(xx, yy, zz);
                                }


                            }
                        }
                    }
                }
                break;
                case 0x0013:    //AC检测 数据
                {
                    byte a = data[10];
                    Integer aa = (int) a;
                    EventBus.getDefault().post(aa, "GP4225_GetAC_Data");
                }
                break;
                case 0x0015: {
                    byte a = data[10];
                    Integer aa = (int) a;
                    EventBus.getDefault().post(aa, "GP4225_GetIR_Status");
                }
                break;
                case 0x0016: {
                    byte a = data[10];
                    Integer aa = (int) a;
                    EventBus.getDefault().post(aa, "GP4225_GetPIR_Status");
                }
                break;
                case 0x0017: {
                    byte a = data[10];
                    Integer aa = (int) a;
                    EventBus.getDefault().post(aa, "GP4225_GetLed_Status");
                }
                break;
                case 0x0018:   //WIFI 板主动发送按键指令
                    if (n_len == 4) {
                        byte[] da = new byte[4];
                        System.arraycopy(data, 10, da, 0, 4);
                        EventBus.getDefault().post(da, "GP4225_GetKey");
                    } else {
                        bOK = false;
                    }

                    break;
                case 0x0019: //雷达数据发送(设备发起或应答)
                    if (n_len != 0x000D) {
                        bOK = false;
                        break;
                    } else {
                        byte[] da = new byte[0x0D];
                        System.arraycopy(data, 10, da, 0, 0x0D);
                        EventBus.getDefault().post(da, "GP4225_GetRadarData");
                    }
                    break;

                case 0x0050: {
                    byte[] aa = null;
                    if (n_len > 0) {
                        aa = new byte[n_len];
                        System.arraycopy(data, 10, aa, 0, n_len);
                        EventBus.getDefault().post(aa, "GP4225_GetDeviceInfo");
                    }
                }
                break;
                default:
                    bOK = false;
                    break;
            }
            return bOK;
        }
        return false;
    }


//    int nPreX =0;
//    int nPreY =0;
//    int nPreZ =0;
//
//    int nCCX =0;
//    int nCCY =0;
//    int nCCZ =0;
//
//    private  final int nAll = 2;
//
//    private  void F_Push(int x,int y,int z)
//    {
//        if(nInxCount<nAll)
//        {
//            nCountX[nInxCount]=x;
//            nCountY[nInxCount]=y;
//            nCountZ[nInxCount]=z;
//            nInxCount++;
//        }
//        else
//        {
//            nCCX=0;
//            nCCY=0;
//            nCCZ=0;
//            for(int i=1;i<nAll;i++)
//            {
//                nCountX[i-1]=nCountX[i];
//                nCountY[i-1]=nCountY[i];
//                nCountZ[i-1]=nCountZ[i];
//                nCCX+=nCountX[i-1];
//                nCCY+=nCountY[i-1];
//                nCCZ+=nCountZ[i-1];
//            }
//            nCountX[nAll-1]=x;
//            nCountY[nAll-1]=y;
//            nCountZ[nAll-1]=z;
//            nCCX+=x;
//            nCCY+=y;
//            nCCZ+=z;
//            nPreX = nCCX/nAll;
//            nPreY = nCCY/nAll;
//            nPreZ = nCCZ/nAll;
//
//        }
//    }

    public class GetFiles {
        public List<MyFile> files;
    }

    public class GsensorData {

        public int x;
        public int y;
        public int z;

        public GsensorData() {
            x = 0;
            y = 0;
            z = 0;
        }
    }

    public class MyFile {
        public String sPath;
        public String sFileName;
        public int nLength;
        public int nInx1;
        public int nInx2;

        public MyFile(String sPath_, String sFileName_, int nLength_) {
            sPath = sPath_;
            sFileName = sFileName_;
            nLength = nLength_;
            nInx1 = 0;
            nInx2 = 0;
        }

    }


    public class G_SensorData {
        public int nStatus;
        public int XX;
        public int YY;
        public int ZZ;

        public G_SensorData(int nS, int x, int y, int z) {
            nStatus = nS;
            XX = x;
            YY = y;
            ZZ = z;
        }
    }

}
