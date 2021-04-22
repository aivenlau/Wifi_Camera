package com.joyhonest.wifination;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.simple.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

//import com.joyhonest.jh_ui.JH_App;

public class ObjectDetector
{
    private static Context  AppContext=null;
    //private   static  int   cropSize = 300;
    public    static  int  nWidth=300;
    public    static  int  nHeight=300;

    private  boolean  bBusy = false;
    private  boolean   bStar=false;
    private Classifier detector;

    public static  float MINIMUM_CONFIDENCE_TF_OD_API = 0.25f;
    //private static final int TF_OD_API_INPUT_SIZE = 300;

    private static final String TF_OD_API_MODEL_FILE ="file:///android_asset/frozen_inference_graph.pb";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/mydata.txt";



    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private Bitmap croppedBitmap = null;
    private Canvas canvas;
    private Handler handler;
    private HandlerThread handlerThread;


    public boolean isbBusy()
    {
        return bBusy;
    }


    public void  F_SetWidth_Height(int nW,int nH)
    {
        nWidth = nW;
        nHeight = nH;
//        if(croppedBitmap!=null)
//            croppedBitmap.recycle();
//        croppedBitmap = Bitmap.createBitmap(nWidth, nHeight, Bitmap.Config.ARGB_8888);
//        canvas = new Canvas(croppedBitmap);
    }

    private   ObjectDetector()
    {
        croppedBitmap = Bitmap.createBitmap(nWidth, nHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(croppedBitmap);
    }

    public  void SetAppCentext(Context context)
    {

        if(AppContext==null) {
            AppContext = context;
        }
        if(AppContext!=null)
        {
            if(detector==null)
            {
                try {
//                    detector = JH_ObjectDetectionAPIModel.create(
//                            AppContext.getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
                    //cropSize = TF_OD_API_INPUT_SIZE;
                    detector = JH_ObjectDetectionAPIModel.create(AppContext.getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, nWidth,nHeight);
                } catch (final IOException e) {
                }
            }
        }

    }

    public static ObjectDetector getInstance() {
        return SingleTonHoulder.singleTonInstance;
    }

    //静态内部类
    public static class SingleTonHoulder {
        private static final ObjectDetector singleTonInstance = new ObjectDetector();
    }

    public void F_Start(boolean _bStart)
    {
        if(bStar && !_bStart)
        {
            if(handler!=null)
            {
                handler.removeCallbacksAndMessages(null);
                handler = null;
            }

            if(handlerThread !=null)
            {
                handlerThread.quit();
            }

            bStar = _bStart;
            return;
        }
        if(!bStar && _bStart)
        {

            handlerThread = new HandlerThread("_Obj__jhabc_");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            bStar = _bStart;
        }
    }

    public  int GetNumber(Bitmap bmp)
    {
        if(bmp==null) {
            return -1;
        }
        if(!bStar)
            return -2;

        if(bBusy)
        {
            return -3;
        }

        bBusy = true;
//        //final  Bitmap bmp = bmpA;
//        if(frameToCropTransform ==null)
//        {
//            int width = bmp.getWidth();
//            int height =bmp.getHeight();
//            frameToCropTransform =
//                    ImageUtils.getTransformationMatrix(
//                            width, height,
//                            nWidth, nHeight,
//                            0, false);
//
//            cropToFrameTransform = new Matrix();
//            frameToCropTransform.invert(cropToFrameTransform);
//        }
//        canvas.drawBitmap(bmp, frameToCropTransform, null);


        croppedBitmap = bmp;
        runInBackground(new Runnable() {
            @Override
            public void run() {
                progressImage();
            }
        });
        return 0;

    }


    int nFindD2 = 0;
    int nNoFind = 0;

    private  void progressImage()
    {

     //   saveBitmap2file(croppedBitmap, AppContext);

        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
        boolean bFind =false;
        String id_="";
        boolean bfirset=true;
        int nMax=0;
        float nre=0;

        for (final Classifier.Recognition result : results)
        {
            final RectF location = result.getLocation();
            if(location!=null) {
                nre = (float)result.getConfidence();

                if(nre>=minimumConfidence)
                {

                    id_ = result.getTitle();
                    //Log.e("GGG", id_ + " confidence  D2 = " +id_+" scoe = "+nre);

                     {
                         bFind = true;
                         if(id_.equals("D2") || id_.equals("D3")) {
                             if(nFindD2<5)
                                nFindD2++;
                             if(nFindD2==1) {
                                 nNoFind = 0;
                                 Log.e("GGG", id_ + " confidence  D2");
                                 EventBus.getDefault().post("D2", "GetGueset");
                                 break;
                             }
                         }
                     }
                }
            }
        }
        if(!bFind)
        {

            if(nNoFind<5)
                nNoFind++;
            if(nNoFind==2) {
                nFindD2 = 0;
                Log.e("GGG", id_ + " confidence-------");
                EventBus.getDefault().post("", "GetGueset");
            }
        }

/*
        for (final Classifier.Recognition result : results)
        {

            final RectF location = result.getLocation();
            if(location!=null) {



                if (bfirset)// && result.getConfidence() >= minimumConfidence)
                {
                    nre = (int) (result.getConfidence() * 100);
                    id = result.getTitle();
                    nMax = nre;
                } else {
                    int tm = (int) (result.getConfidence() * 100);
                    if (tm >= nre) {
                        nre = tm;
                        nMax = nre;
                        id = result.getTitle();
                    }
                }
                bfirset = false;
            }

//            if (location != null && result.getConfidence() >= minimumConfidence)
//            {
//                //Log.i("MyTAG",result.getTitle());
//                String id = result.getTitle();
//                bFind = true;
//                EventBus.getDefault().post(id,"GetGueset");
//                break;
//            }
        }

        if(!id.isEmpty())
        {
            if(nMax>=SetMax)
            {
                bFind=true;
                EventBus.getDefault().post(id,"GetGueset");

            }
        }

        if(!bFind)
        {
            //Log.i("MyTAG","Not Found!!");
            EventBus.getDefault().post("","GetGueset");
        }
 */
        bBusy = false;
    }



    private synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }


    public  String getNormalSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public  void saveBitmap2file(Bitmap bmp, Context context) {


        String savePath;
        String fileName = getNormalSDCardPath()+"/abc.jpg";

        File filePic = new File(fileName);
        try {
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            //Toast.makeText(context, "保存成功,位置:" + filePic.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    filePic.getAbsolutePath(), fileName, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        // 最后通知图库更新

    }

}
