package com.joyhonest.wifination;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

/**
 * Created by aivenlau on 2017/4/19.
 */

public class MyThumb {
    public String sFilename;
    public Bitmap thumb;
    public MyThumb(byte[] data, String sFilename_) {
        thumb = Bitmap.createBitmap(160, 90, Bitmap.Config.ARGB_8888);
        thumb.copyPixelsFromBuffer(ByteBuffer.wrap(data));
        sFilename = sFilename_;
    }

}
