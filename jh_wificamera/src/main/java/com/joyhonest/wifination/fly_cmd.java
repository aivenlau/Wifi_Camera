package com.joyhonest.wifination;

/**
 * Created by aiven on 2017/11/15.
 */

public class fly_cmd {
    public static int Roll = 0x80;            //左右控制			0~128~255		（左小右大）
    public static int Pitch = 0x80;            //前后控制			0~128~255		（后小前大）
    public static int Thro = 0x80;            //油门控制			0~255			（后小前大）
    public static int Yaw = 0x80;            //转向控制			0~128~255		（左小右大）
    public static int TrimRoll = 0x20;        //左右微调			0~32~63		（左小右大）
    public static int TrimPitch = 0x20;        //前后微调			0~32~63		（后小前大）
    public static int TrimThro = 0x20;        //油门微调			0~32~63		（左小右大）
    public static int TrimYaw = 0x20;        //转向微调			0~32~63		（后小前大）
    public static int FastMode = 0;        //快档模式			00摇杆慢档，01摇杆中档，02摇杆快档
    public static int CFMode = 0;        //无头模式			0普通模式，1无头模式
    public static int FlipMode = 0;        //翻滚模式			100前，101 后，110 左，111 右（翻）
    public static int GpsMode = 0;        //GPS模式			0普通模式，1 GPS模式
    public static int Mode = 0;            //模式标志			0 Mode2，1 Mode1
    public static int LevelCor = 0;        //水平校准			变高1秒后变低
    public static int MagCor = 0;        //罗盘校准			变高1秒后变低
    public static int AutoTakeoff = 0;    //一键起飞			变高1秒后变低
    public static int AutoLand = 0;        //一键降落			变高1秒后变低
    public static int GoHome = 0;        //一键返航			变高1秒后变低
    public static int Stop = 0;            //一键急停			变高1秒后变低
    public static int FollowMe = 0;        //启动跟随飞行		变高1秒后变低
    public static int CircleFly = 0;        //启动环绕飞行		变高1秒后变低
    public static int PointFly = 0;        //启动航点飞行		变高1秒后变低
    public static int FollowMe_A = 0;        //启动指点飞行		变高1秒后变低
    public static int Photo = 0;            //拍照一次			变高1秒后变低
    public static int Video = 0;            //启停摄像			变高1秒后变低
    public static int CamMovStep = 0;    //镜头调节级数		上、下、左、右调节（00-1F）
    public static int CamMovDir = 0;        //镜头调节方向		00 上，01 下，10 左，11 右
    public static byte[] cmd = new byte[30];
}
