/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package designer.project.a3dvectordesigner;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Calendar;
import java.util.StringTokenizer;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;
    private float xzoom,yzoom,lastX,lastY;
    public boolean zoom=false, move=false;
    public static boolean rotate=true;

    public MyGLSurfaceView(Context context) {
        super(context);
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context);
        setRenderer(mRenderer);


        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    final int MAX_CLICK_DURATION = 150;
    long startClickTime=0;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_UP:
                zoom = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                zoom = true;
                lastX=e.getX(1);
                lastY=e.getY(1);
                break;
            case MotionEvent.ACTION_UP:
                //mRenderer.lastAngl = mRenderer.sphereCords(mRenderer.realX,mRenderer.realY);
                //mRenderer.tempViewMatrix=mRenderer.mViewMatrix;
                mRenderer.mouseUp();
                break;
            case MotionEvent.ACTION_DOWN:
                lastX=x;
                lastY=y;

                float[] now = mRenderer.sphereCords(x,y);
                mRenderer.lastAngl = now;
                mRenderer.setStartPos(x,y);
                mRenderer.setPos(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                if(rotate) {
                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;

                    mRenderer.mAngleX += dx;  // = 180.0f / 320
                    mRenderer.mAngleY += dy;  // = 180.0f / 320
                }
                mRenderer.setPos(x,y);

                if(zoom){
                    float x2 = e.getX(1);
                    float y2 = e.getY(1);
                    if(x2>x)
                        xzoom = ((x2 - lastX) - (x-mPreviousX));
                    else
                        xzoom = ( (x-mPreviousX) - (x2 - lastX) );
                    if(y2>y)
                        yzoom = ((y2 - lastY) - (y-mPreviousY));
                    else
                        yzoom = ( (y-mPreviousY) - (y2 - lastY) );

                    mRenderer.setZoom(mRenderer.getZoom() - (xzoom+yzoom)/150);

                    requestRender();
                    lastX=x2;
                    lastY=y2;
                }
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

}
