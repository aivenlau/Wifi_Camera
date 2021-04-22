package com.joyhonest.wifination;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.nio.ByteBuffer;

public class MyMediaMuxer {

    private  static boolean  bStartWrite = false;
    private  static boolean bRecording=false;
    private  static MediaMuxer mediaMuxer=null;

    private static  int audioInx=-1;
    private static  int videoInx=-1;

    public  static MediaFormat formatV;
    public  static MediaFormat formatA;

    public  static int  start(String strNme)
    {
        if(bRecording)
        {

        }

        bRecording = false;
        try {
            bStartWrite = false;
            formatV = null;
            formatA = null;
            mediaMuxer = new MediaMuxer(strNme, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    static void WritSample(byte[] data, boolean bKey, boolean bVideo, long ppp)
    {
        if(!bRecording)
            return;
        if(!bStartWrite && bVideo && bKey)
        {
            bStartWrite = true;
            Log.e("media","firset key Framne");
        }
        if(!bStartWrite)
            return;

        if(bVideo)
        {
            if (data != null && mediaMuxer != null)
            {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                info.offset=0;
                info.size=data.length;
                info.flags = 0;// MediaCodec.BUFFER_FLAG_PARTIAL_FRAME;
                if(bKey)
                {
                    info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                }
                info.presentationTimeUs = ppp;
                try {
                    mediaMuxer.writeSampleData(videoInx, ByteBuffer.wrap(data), info);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            if (data != null && mediaMuxer != null)
            {
                if(audioInx>=0)
                {
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    info.offset=0;
                    info.size=data.length;
                    info.flags = 0;
                    info.presentationTimeUs = ppp;
                    try {
                        mediaMuxer.writeSampleData(audioInx, ByteBuffer.wrap(data), info);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        }
    }


    static void AddVideoTrack(MediaFormat format)
    {
        if(format!=null && mediaMuxer!=null)
        {
            try {
                if (videoInx < 0) {
                    videoInx = mediaMuxer.addTrack(format);
                }
            }
            catch (Exception e)
            {
                videoInx=-1;
                e.printStackTrace();
            }
            if(videoInx>=0)
            {
                if(wifination.bG_Audio)
                {
                    if(audioInx>=0)
                    {
                        bRecording = true;
                        bStartWrite = false;
                        mediaMuxer.start();
                        Log.e("media","Start 111");
                    }
                }
                else
                {
                    bRecording = true;
                    bStartWrite = false;
                    mediaMuxer.start();
                    Log.e("media","Start 222");
                }
            }
        }
    }

    static void AddAudioTrack(MediaFormat format)
    {
        if(format!=null && mediaMuxer!=null)
        {
            try {
                if(audioInx<0) {
                    audioInx = mediaMuxer.addTrack(format);
                }
            }catch (Exception e)
            {
                audioInx=-1;
                e.printStackTrace();
            }
            if(audioInx>=0)
            {
                    if(videoInx>=0)
                    {
                        bRecording = true;
                        bStartWrite = false;
                        mediaMuxer.start();
                        Log.e("media","Start 333");
                    }
                    else
                    {
                        Log.e("media","Start 444");
                    }
            }

        }
    }



    public static void stop()
    {
         if(bRecording)
         {
             try {
                 mediaMuxer.stop();
                 mediaMuxer.release();
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }

             formatV = null;
             formatA = null;

             mediaMuxer=null;
             bRecording = false;
             audioInx = -1;
             videoInx = -1;
         }
    }


}
