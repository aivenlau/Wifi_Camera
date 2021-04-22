package com.joyhonest.wifination;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;


import static android.media.MediaFormat.KEY_HEIGHT;
import static android.media.MediaFormat.KEY_SLICE_HEIGHT;
import static android.media.MediaFormat.KEY_WIDTH;


public class VideoMediaCoder {
    long pts;
    public long  pts_;
    int fps;
    private MediaCodec mMediaCodec;

    private final  static String VCODEC="video/avc";
    private final  static String TAG="MediaCoder";

    private  boolean  bGetPPS=false;


    public long  nCountFrame=0;


    public VideoMediaCoder() {
        mMediaCodec=null;
    }

    private  MediaFormat F_GetMediaFormat(int width,int height,int bitrate,int fps,int color)
    {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
            pts = 0;
            pts_=0;
            nCountFrame = 0;
        }

        bGetPPS = false;
        pts = 0;
        pts_=0;
        nCountFrame = 0;
        this.fps = fps;
        boolean bOK = true;
        try {
            mMediaCodec = MediaCodec.createEncoderByType(VCODEC);

            //mMediaCodec.createInputSurface();

        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
            bOK = false;
        }
        if (!bOK) {
            mMediaCodec = null;
            return null;
        }

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(VCODEC, width,height); //height和width一般都是照相机的height和width。


        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, width);
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, height);



        //描述平均位速率（以位/秒为单位）的键。 关联的值是一个整数
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
                //描述视频格式的帧速率（以帧/秒为单位）的键。
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);//帧率，一般在15至30之内，太小容易造成视频卡顿。
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, color);//色彩格式，具体查看相关API，不同设备支持的色彩格式不尽相同
        //关键帧间隔时间，单位是秒
        if(Build.VERSION.SDK_INT >= 21) {
            mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
            mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
            mediaFormat.setInteger(MediaFormat.KEY_LEVEL,MediaCodecInfo.CodecProfileLevel.AVCLevel1);

            mediaFormat.setInteger(MediaFormat.KEY_CAPTURE_RATE, fps);
            //mediaFormat.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / fps);

        }





        try {
            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            mediaFormat = null;
        }
        return mediaFormat;

    }


//    private  MediaCodecInfo selectCodec(String mimeType) {
//        int numCodecs = MediaCodecList.getCodecCount();
//        for (int i = 0; i < numCodecs; i++) {
//            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
//
//            if (!codecInfo.isEncoder()) {
//                continue;
//            }
//            String[] types = codecInfo.getSupportedTypes();
//            for (int j = 0; j < types.length; j++) {
//                if (types[j].equalsIgnoreCase(mimeType)) {
//                    return codecInfo;
//                }
//            }
//        }
//        return null;
//    }


    public int initMediaCodec(int width,int height,int bitrate,int fps) {

        //chooseVideoEncoder();
        int nColor;

//        nColor = getColorFormat();
//        if(nColor==0)
//            return 0;


        nColor = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        if(F_GetMediaFormat(width,height,bitrate,fps,nColor) ==null)
        {
            nColor = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
            if(F_GetMediaFormat(width,height,bitrate,fps,nColor) ==null)
            {
                nColor = 0;
            }
        }

        if(nColor!=0)
        {
            mMediaCodec.start();
        }
        else
        {
            mMediaCodec.release();
            mMediaCodec = null;
        }
        return nColor;

    }
    public void F_CloseEncoder()
    {
        if(mMediaCodec==null)
        {
            return;
        }
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec=null;
        bGetPPS = false;
        nCountFrame = 0;
        Log.e(TAG,"Close MediaCodec!!!---");
    }

    public  long getRecordTime()
    {
        if(fps>0) {
            float df = 1000.0f / fps;
            return (long)(nCountFrame*df);
        }
        else
        {
            return 0;
        }
    }

    private int getColorFormat() {
        MediaCodecInfo mediaCodecInfo = chooseVideoEncoder(null);
        if(mediaCodecInfo==null)
            return 0;
        MediaCodecInfo.CodecCapabilities codecCapabilities =
                mediaCodecInfo.getCapabilitiesForType(VCODEC);
        for (int i = 0; i < codecCapabilities.colorFormats.length; i++) {
            int format = codecCapabilities.colorFormats[i];
            if(format == codecCapabilities.COLOR_FormatYUV420Planar ||
              format == codecCapabilities.COLOR_FormatYUV420SemiPlanar)
                return format;
        }
        return 0;
    }



        int  ddd=0;
    public  void  offerEncoder(byte[] data,int nLen)
    {
        if(mMediaCodec==null)
        {
            return;
        }
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();//拿到输入缓冲区,用于传送数据进行编码
       // ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();//拿到输出缓冲区,用于取到编码后的数据

        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(5000);
        if (inputBufferIndex >= 0) {//当输入缓冲区有效时,就是>=0
            pts_ = (pts * (1000000 / fps));
            pts++;

            ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);

           // ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(data);//往输入缓冲区写入数据,
            ////五个参数，第一个是输入缓冲区的索引，第二个数据是输入缓冲区起始索引，第三个是放入的数据大小，第四个是时间戳，保证递增就是
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, pts_, 0);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 2000);//拿到输出缓冲区的索引
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                MyMediaMuxer.AddVideoTrack(newFormat);
            }
            if(outputBufferIndex >= 0)
            {
               // ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                boolean bKeyframe = false;
                if(bufferInfo.flags==MediaCodec.BUFFER_FLAG_CODEC_CONFIG)
                {
                    if(!bGetPPS)
                    {
                        bGetPPS = true;
                        //wifination.naSave2FrameMp4(outData, bufferInfo.size, 0, bKeyframe);
                    }
                }
                else
                {
                    if(bufferInfo.flags==MediaCodec.BUFFER_FLAG_KEY_FRAME)
                        bKeyframe = true;
                    //wifination.naSave2FrameMp4(outData, bufferInfo.size, 1, bKeyframe);
                    nCountFrame++;
                    MyMediaMuxer.WritSample(outData,bKeyframe,true,pts_);
                }

                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
               // outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 2000);//拿到输出缓冲区的索引
            }
        }
    }


    private MediaCodecInfo chooseVideoEncoder(String name) {
        int nbCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < nbCodecs; i++) {
            MediaCodecInfo mci = MediaCodecList.getCodecInfoAt(i);
            if (!mci.isEncoder()) {
                continue;
            }
            String[] types = mci.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(VCODEC)) {
                    if (name == null) {
                        return mci;
                    }
                    if (mci.getName().contains(name)) {
                        return mci;
                    }
                }
            }
        }
        return null;
    }
    //
//    private int chooseVideoEncoder() {
//        // choose the encoder "video/avc":
//        //      1. select one when type matched.
//        //      2. perfer google avc.
//        //      3. perfer qcom avc.
//        MediaCodecInfo vmci = chooseVideoEncoder(null);
//        //vmci = chooseVideoEncoder("google", vmci);
//        //vmci = chooseVideoEncoder("qcom", vmci);
//
//        int matchedColorFormat = 0;
//        MediaCodecInfo.CodecCapabilities cc = vmci.getCapabilitiesForType(VCODEC);
//        for (int i = 0; i < cc.colorFormats.length; i++) {
//            int cf = cc.colorFormats[i];
//            Log.i(TAG, String.format("vencoder %s supports color fomart 0x%x(%d)", vmci.getName(), cf, cf));
//
//            // choose YUV for h.264, prefer the bigger one.
//            // corresponding to the color space transform in onPreviewFrame
//            if ((cf >= cc.COLOR_FormatYUV420Planar && cf <= cc.COLOR_FormatYUV420SemiPlanar)) {
//                if (cf > matchedColorFormat) {
//                    matchedColorFormat = cf;
//                }
//            }
//        }
//        for (int i = 0; i < cc.profileLevels.length; i++) {
//            MediaCodecInfo.CodecProfileLevel pl = cc.profileLevels[i];
//            Log.i(TAG, String.format("vencoder %s support profile %d, level %d", vmci.getName(), pl.profile, pl.level));
//        }
//        Log.i(TAG, String.format("vencoder %s choose color format 0x%x(%d)", vmci.getName(), matchedColorFormat, matchedColorFormat));
//        return matchedColorFormat;
//    }



}
