package com.joyhonest.wifination;

import android.content.Context;

import android.opengl.GLSurfaceView;

import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aiven on 2017/12/29.
 */

public class JH_GLSurfaceView extends GLSurfaceView  implements GLSurfaceView.Renderer {


    public boolean bDraw = true;

    public JH_GLSurfaceView(Context context_) {
        super(context_);
        init(context_);
    }
    /**
     * Standard View constructor. In order to render something, you
     * must call {@link #setRenderer} to register a renderer.
     */
    public JH_GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    private void init(final Context context) {
        setEGLContextClientVersion(2);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        /*渲染方式，RENDERMODE_WHEN_DIRTY表示被动渲染，只有在调用requestRender或者onResume等方法时才会进行渲染。RENDERMODE_CONTINUOUSLY表示持续渲染*/

        setRenderer(this);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        wifination.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        wifination.changeLayout(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        {
            if (bDraw)
                wifination.drawFrame();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        wifination.release();
    }

    public static native int naDecordInit();

    public static native int naDecordRelease();

    public static native int naDecordFrame(byte[] data, int nLenA);


}
