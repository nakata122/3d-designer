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

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Pen {

//    "uniform mat4 uMVPMatrix;" +
//            "attribute vec4 vPosition;" +
private final String vertexShaderCode =
        "uniform mat4 u_MVPMatrix;    \n" +
                "attribute vec3 a_Position;      \n" +
                "attribute vec3 a_Color;      \n" +
                " \n" +
                "varying vec3 v_Position;        \n" +
                "varying vec3 v_Color;      \n" +
                "void main()\n" +
                "{\n" +
                "    v_Position = a_Position;  \n" +
                "    v_Color = a_Color;  \n" +
                "    gl_PointSize = 20.0;  \n" +
                "    gl_Position = u_MVPMatrix * vec4(v_Position,1.0);\n" +
                "}";

    private final String fragmentShaderCode =
            "precision mediump float;        \n" +
                    "varying vec3 v_Position;        \n" +
                    "varying vec3 v_Color;      \n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_FragColor = vec4(v_Color,1.0);\n" +
                    "  }";

    private static FloatBuffer[] vertexBuffer = new FloatBuffer[100];
    private static FloatBuffer[] colorBuffer = new FloatBuffer[100];
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    //VBOS IBOS
    final private int vbos[] = new int[100];
    Particle[] p = new Particle[1000];
    float[] modelCoords = new float[4], colors = new float[4];
    static  float[] drawCoords = new float[10000];

    private int br = 0, maxN=10;
    private static int objBr=0;
    public float radius=2;
    private boolean plus=false;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    Random rand = new Random();

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Pen() {
        GLES20.glGenBuffers(3, vbos, 0);

        colors[0]=0.5f;
        colors[1]=0.5f;
        colors[2]=0.5f;

        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                (modelCoords.length * 4));
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer[0] = bb.asFloatBuffer();
        vertexBuffer[0].put(modelCoords);
        vertexBuffer[0].position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                (colors.length * 4));
        cb.order(ByteOrder.nativeOrder());
        colorBuffer[0] = cb.asFloatBuffer();
        colorBuffer[0].put(colors);
        colorBuffer[0].position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0]);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer[0].capacity() * 1000,
                null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[1]);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer[0].capacity() * 1000,
                null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program

        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public void penUp() {
        plus=true;
        Log.e("toch", String.valueOf(br/4));
    }
    public void penDown(float x,float y, float Z){
//        for(int i=0;i<objBr;i+=3){
//            modelCoords[i]=drawCoords[i]+x;
//            modelCoords[i+1]=drawCoords[i+1]+y;
//            modelCoords[i+2]=drawCoords[i+2];
//            colors[i] = (float)rand.nextInt(2);
//            colors[i+1] = (float)rand.nextInt(2);
//            colors[i+2] = (float)rand.nextInt(2);
//        }
//        br+=objBr*4;
        if(plus) {
            br += 3 * 4;
            plus=false;
        }
        modelCoords[0]=x;
        modelCoords[1]=y;
        modelCoords[2]=Z;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                (modelCoords.length * 4));
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer[0] = bb.asFloatBuffer();
        vertexBuffer[0].put(modelCoords);
        vertexBuffer[0].position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                (colors.length * 4));
        cb.order(ByteOrder.nativeOrder());
        colorBuffer[0] = cb.asFloatBuffer();
        colorBuffer[0].put(colors);
        colorBuffer[0].position(0);
    }
    public static void loadVerts(float[] coords,int brs){
        drawCoords=coords;
        objBr=brs;
        Log.e("coutnt", String.valueOf(brs));
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        if(br>=40000) br=0;
        //Log.e("sas", String.valueOf(br));
        //VERTEX
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0]);

        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,br, vertexBuffer[0].capacity() * 4,
                vertexBuffer[0]);

        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //COLOR

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[1]);

        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,br, colorBuffer[0].capacity() * 4,
                colorBuffer[0]);

        GLES20.glVertexAttribPointer(
                mColorHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glEnableVertexAttribArray(mColorHandle);


        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glLineWidth(4);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, br/12+1);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, br/12+1);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
    }

    float lerp(float a,float b, float time){

        return (a + (b-a)*time);
    }
}