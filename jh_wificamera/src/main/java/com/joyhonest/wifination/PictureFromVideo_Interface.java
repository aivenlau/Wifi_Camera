package com.joyhonest.wifination;

import android.graphics.Bitmap;

public interface PictureFromVideo_Interface {
    void onError(int nErrno);
    void onStart(int nFrame,int times);
    void onEnd();
    void onGetaPicture(Bitmap bmp);
}
