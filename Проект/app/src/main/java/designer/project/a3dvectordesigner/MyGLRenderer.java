package designer.project.a3dvectordesigner;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.StringTokenizer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    public final int MOVING=0;
    public static enum btnTex{
        add(0),edit(1),white(2),draw(3),emit(4),save(5);
        public int value;
        private btnTex(int val){
            this.value = val;
        }
    };
    public enum modes{
        draw(0),blank(1);
        public int value;
        private modes(int val){
            this.value = val;
        }
    };
    private static final String TAG = "MyGLRenderer";
    private Model mSquare;
    private Model2d GUI;
    private modes mode = modes.blank;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    final float[] mMVPMatrix = new float[16], mRotationMatrix = new float[16], mTempMatrix = new float[16], mat = new float[16];
    final float[] mProjectionMatrix = new float[16], blankMat = new float[16], mPerspectiveMatrix = new float[16];

    float[] mViewMatrix = new float[16],m2dViewMatrix = new float[16];
    float mAngleX,mAngleY,lastAngleX,lastAngleY,realX,realY,startX,startY,lastX=0,lastY=0,worldX,worldY;
    float[] lastAngl={0,0,1},curPos=new float[3],lastPos=new float[3],axis=new float[3];
    Object editBtn,addBtn, plane, drawBtn, emitterBtn, saveBtn;
    Object popup,blurr;
    TextObj text;
    Text mainText;
    Object[] emitters = new Object[100];
    int emmBr=0,selected=-1;

    public static boolean[] items = new boolean[100];
    private boolean btnClicked=false, saving=false;

    float mAngle,wid,hei;
    int fps=0;
    long startTime;
    float zoom = 0;
    float fovy;
    MainPhysics physics;
    Vector vec;
    Lines lines;
    Pen pen;
    Random rand = new Random();
    private int[] frameBuffer = new int[10];
    private int frameId,frameTex,frameRender;

    float num;
    Context ctx;


    public MyGLRenderer(Context ctx){
        this.ctx = ctx;
    }
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        float[] defaultPos = {0,0,0};
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        physics = new MainPhysics(this);
        //mSquare = new Model();
        lines = new Lines();
        GUI = new Model2d();
        vec = new Vector();
        pen = new Pen();

        text = new TextObj("particles ",0.2f,-0.2f,0.05f);
        text.posZ=4;
        addBtn = new Obj2d(btnTex.add,0,0);
        editBtn = new Obj2d(btnTex.edit,0,0.2f);
        drawBtn = new Obj2d(btnTex.draw,0,0.4f);
        saveBtn = new Obj2d(btnTex.save,0,0.6f);
        emitterBtn = new Obj2d(btnTex.emit,0.2f,0);
        emitterBtn.posZ=1;

        popup = new Obj2d(btnTex.white, 0.5f, 0.5f);
        popup.width=0.5f;
        popup.height=0.5f;
        popup.posZ=4;

        blurr = new Obj2d(btnTex.white, 0,0);
        blurr.height = 1;
        blurr.width = 2;
        blurr.alpha=0.7f;
        blurr.posZ=4;

        plane = new Obj2d(btnTex.white,0,0);
        plane.width=0.6f;
        plane.height=0.8f;
        plane.alpha=0;
        plane.posZ=1;

        mainText = new Text();
        Matrix.setIdentityM(mRotationMatrix,0);
        startTime = System.nanoTime();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if(System.nanoTime() - startTime >= 1000000000) {
            Log.e("fps: ", String.valueOf(fps));
            fps = 0;
            startTime = System.nanoTime();
        }

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.4f,0.4f,0.4f,1);
        // Use culling to remove back faces.

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        float[] matrix = rotateQuat();
        Matrix.setLookAtM(mTempMatrix, 0, 0, 0, zoom, 0, 0, 0, 0, 1, 0);
        if(mode!=modes.draw) {
            Matrix.multiplyMM(mat, 0, matrix, 0, mRotationMatrix, 0);

            System.arraycopy(mat, 0, mRotationMatrix, 0, 16);
            Matrix.multiplyMM(mViewMatrix, 0, mTempMatrix, 0, mRotationMatrix, 0);
        }
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);


        if(mode == modes.draw || mode == modes.blank){
            pen.draw(mMVPMatrix);
        }

        for(int i=0;i<emmBr;i++) {
            emitters[i].posX=physics.touchPos[0];
            emitters[i].posY=physics.touchPos[1];
            emitters[i].posZ=physics.touchPos[2];
            Draw3dPatricles(emitters[i]);

        }
        // Draw
        //mSquare.draw(mMVPMatrix, 0,0);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        Draw2dObj(text);

//        GLES20.glDepthMask(false);
//        for(int i=0;i<emmBr;i++)
//            Draw3dSprites(emitters[i]);
//        GLES20.glDepthMask(true);

        if(plane.open) Draw2dObj(emitterBtn);
        Draw2dObj(plane);
        Draw2dObj(saveBtn);
        Draw2dObj(addBtn);
        Draw2dObj(editBtn);
        Draw2dObj(drawBtn);

//        if(saving) {
//            Draw2dObj(popup);
//            Draw2dObj(blurr);
//        }
        GLES20.glDisable(GLES20.GL_BLEND);

        if(saving){
            ByteBuffer pixels = ByteBuffer.allocateDirect((int)wid*(int)hei*4);
            pixels.order(ByteOrder.nativeOrder());
            pixels.position(0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTex);
            GLES20.glReadPixels(0,0, (int)wid, (int)hei, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixels);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            int data[] = new int[(int)wid*(int)hei];
            pixels.asIntBuffer().get(data);
            pixels = null;
            Bitmap screenshot = Bitmap.createBitmap(data,(int)wid,(int)hei, Bitmap.Config.RGB_565);

            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "myScreen.jpg");
            // path to /data/data/yourapp/app_data/imageDir
            if (file.exists()) {
                file.delete();
            }
            try {
                FileOutputStream out = new FileOutputStream(file);
                screenshot.compress(Bitmap.CompressFormat.JPEG, 100, out);

                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            saving=false;
        }
        fps++;
    }

    public void Fade(){
        if(!plane.open) {
            Runnable ran;
            Thread effect = new Thread(ran = new Runnable() {
                @Override
                public void run() {
                    plane.alpha = 0;
                    plane.posX = 0;
                    emitterBtn.posX = 0;
                    emitterBtn.alpha = 0;
                    for (int i = 0; i < 100; i++) {
                        plane.alpha += 0.01f;
                        plane.posX += 0.002f;
                        emitterBtn.alpha += 0.01f;
                        emitterBtn.posX += 0.002f;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(!plane.open) break;
                    }
                }
            });
            effect.start();
            plane.open = true;
        }
    }
    public void Hide(){
        if(plane.open) {
            plane.open=false;
            Runnable ran;
            Thread effect = new Thread(ran = new Runnable() {
                @Override
                public void run() {
                    plane.alpha = 1;
                    plane.posX = 0.2f;
                    for (int i = 0; i < 100; i++) {
                        plane.alpha -= 0.01f;
                        plane.posX -= 0.002f;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(plane.open) break;
                    }
                }
            });
            effect.start();
        }
    }
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);
        float   ssu = 1.0f;
        float   ssx = 1.0f;
        float   ssy = 1.0f;
        float   swp = 320.0f;
        float   shp = 480.0f;

        float ratio = (float) width / height;
        float top = (float)Math.tan(60 * Math.PI / 360.0f);
        wid = width;
        hei = height;

        initFbo();

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio*top, ratio*top, -top, top, 1, 1000);
        float halfwidth = 5/2;
        float halfheight = 5/2;
        if (halfwidth/halfheight > ratio) {
        // use horizontal angle to set fovy
            fovy = 2*(float)Math.atan(halfwidth/ratio);
        } else {
        // use vertical angle to set fovy
            fovy = 5;
        }

        Matrix.orthoM(mPerspectiveMatrix,0, 0, ratio, -ratio/2, 0, 1, 100);
        fovy = (float)(fovy*180/Math.PI);

        Matrix.setLookAtM(m2dViewMatrix, 0, 0, 0, 20, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(blankMat, 0, mPerspectiveMatrix, 0, m2dViewMatrix, 0);
        //mainPhysics.foxy = fovy;
    }

    private void initFbo(){

        GLES20.glGenFramebuffers(1,frameBuffer,0);
        frameId=frameBuffer[0];
        GLES20.glGenTextures(1,frameBuffer,0);
        frameTex = frameBuffer[0];
        GLES20.glGenRenderbuffers(1, frameBuffer, 0);
        frameRender = frameBuffer[0];


        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameId);
        //Bind texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTex);
        //Define texture parameters
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, (int)wid,(int)hei, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        //Bind render buffer and define buffer dimension
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, frameRender);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, (int)wid,(int)hei);
        //Attach texture FBO color attachment
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, frameTex, 0);
        //Attach render buffer to depth attachment
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, frameRender);
        //we are done, reset
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }



    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public float getAngle() {
        return mAngle;
    }

    public float getZoom() {
        return zoom;
    }


    public void setAngle(float angle) {
        mAngle = angle;
    }

    public void setZoom(float tzoom) { this.zoom = tzoom;}

    public float[] sphereCords(float tempX, float tempY){
        float d, a;
        float[] vert=new float[3];
        vert[0] = (2*tempX - wid) / wid;
        vert[1] = (hei - 2*tempY) / hei;
        d = (float)Math.sqrt(vert[0]*vert[0] + vert[1]*vert[1]);
        vert[2] = (float)Math.cos((Math.PI/2.0) * ((d < 1.0) ? d : 1.0));
        a = 1 / (float)Math.sqrt(vert[0]*vert[0] + vert[1]*vert[1] + vert[2]*vert[2]);
        vert[0] *= a; vert[1] *= a; vert[2] *= a;

        return vert;
    }

    public float[] rotateQuat(){

        float[] v1 = sphereCords(realX, realY);
        float[] v0 = lastAngl;
        lastX = realX;
        lastY = realY;

        v1 = physics.normalizeVector3(v1);
        v0 = physics.normalizeVector3(v0);
        System.arraycopy(v1, 0, lastAngl, 0, 3);

        float cosTheta = physics.dotProduct(v0, v1);

        float[] rotAxis = physics.crossProduct(v0, v1);

        float s = (float)Math.sqrt( (1+cosTheta)*2 );
        float invs = 1 / s;
        float angl=(float)Math.acos(cosTheta)/(physics.length(v0)*physics.length(v1));
        // Converts all degrees angles to radians.

        float[] quat = new float[4];

        quat[0] = s/2;
        quat[1] = rotAxis[0]*invs;
        quat[2] = rotAxis[1]*invs;
        quat[3] = rotAxis[2]*invs;
        physics.x=quat[1];
        physics.y=quat[2];
        physics.z=quat[3];


        float[] matrix = new float[16];
        matrix[0] = 1 - 2 * (quat[2] * quat[2] + quat[3] * quat[3]);
        matrix[1] = 2 * (quat[1] * quat[2] + quat[3] * quat[0]);
        matrix[2] = 2 * (quat[1] * quat[3] - quat[2] * quat[0]);
        matrix[3] = 0;

        // Second Column
        matrix[4] = 2 * (quat[1] * quat[2] - quat[3] * quat[0]);
        matrix[5] = 1 - 2 * (quat[1] * quat[1] + quat[3] * quat[3]);
        matrix[6] = 2 * (quat[3] * quat[2] + quat[1] * quat[0]);
        matrix[7] = 0;

        // Third Column
        matrix[8] = 2 * (quat[1] * quat[3] + quat[2] * quat[0]);
        matrix[9] = 2 * (quat[2] * quat[3] - quat[1] * quat[0]);
        matrix[10] = 1 - 2 * (quat[1] * quat[1] + quat[2] * quat[2]);
        matrix[11] = 0;

        // Fourth Column
        matrix[12] = 0;
        matrix[13] = 0;
        matrix[14] = 0;
        matrix[15] = 1;
        return matrix;
    }
    public void mouseUp(){
        if(mode == modes.draw)pen.penUp();
        if(btnClicked) {
            float[] pos = physics.rayPicking(startX, startY,1);
            if(addBtn.isClicked(pos[0], pos[1])) Fade();
            if(editBtn.isClicked(pos[0], pos[1]))
                Log.e("SecondC", "cli");
            if(drawBtn.isClicked(pos[0], pos[1])) {
                if(mode!=modes.draw)  mode = modes.draw;
                else mode = modes.blank;
            }
            if(emitterBtn.isClicked(pos[0],pos[1])){
                emitters[emmBr] = new Obj2d(null,0,0);
                emmBr++;
            }
            if(saveBtn.isClicked(pos[0],pos[1])){
                saving=true;
            }
        }
        else if(plane.open){
            Hide();
            plane.open=false;
        }

    }

    public void setStartPos(float x, float y){
        this.startX = x;
        this.startY = y;
        btnClicked=false;

        float[] pos = physics.rayPicking(startX, startY,1);
        Log.e("toch", String.valueOf(pos[0] + " " + pos[1]));
        if(addBtn.isClicked(pos[0], pos[1])) btnClicked=true;
        if(editBtn.isClicked(pos[0], pos[1])) btnClicked=true;
        if(drawBtn.isClicked(pos[0], pos[1])) btnClicked=true;
        if(saveBtn.isClicked(pos[0], pos[1])) btnClicked=true;
        if(plane.isClicked(pos[0], pos[1]) && plane.open){
            btnClicked=true;
        }

        physics.startTouch = physics.rayPicking(startX, startY,0);
    }
    public void setPos(float x, float y){
        this.realX = x;
        this.realY = y;

        if(!btnClicked) {
            physics.touchPos = physics.rayPicking(x, y,0);
            if (mode == modes.draw) {
                pen.penDown(physics.touchPos[0], physics.touchPos[1], physics.touchPos[2]);
            }
        }

    }
    private void Draw2dObj(Object obj){
        System.arraycopy(blankMat, 0, obj.PosMatrix, 0, 16);
        if(obj.Type() == "2d") {
            Matrix.rotateM(obj.PosMatrix, 0, 90, 1, 0, 0);
            Matrix.translateM(obj.PosMatrix, 0, obj.posX + obj.width / 2, obj.posZ, obj.posY + obj.height / 2);
            Matrix.scaleM(obj.PosMatrix,0, obj.width*5, 1, obj.height*5);
            GUI.draw(obj.PosMatrix, 0, obj.tex.value,obj.alpha);
        }
        else if(obj.Type() == "Text"){
            Matrix.translateM(obj.PosMatrix, 0, obj.posX + obj.width / 2, obj.posY + obj.height / 2, 0);
            mainText.draw(obj.PosMatrix,0,1);
        }
    }
    private void Draw3dSprites(Object obj){
        System.arraycopy(mMVPMatrix, 0, obj.PosMatrix, 0, 16);
        Matrix.translateM(obj.PosMatrix,0, obj.posX, obj.posY, obj.posZ);
        vec.draw(obj.PosMatrix);
    }

    private void Draw3dPatricles(Object obj){
        System.arraycopy(mMVPMatrix, 0, obj.PosMatrix, 0, 16);
        Matrix.translateM(obj.PosMatrix,0, obj.posX, obj.posY, obj.posZ);
        lines.draw(mMVPMatrix);
    }
}