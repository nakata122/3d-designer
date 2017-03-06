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

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Lines {

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
    float[] modelCoords = new float[10000], colors = new float[10000];

    private int br = 0, maxN=10;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    Random rand = new Random();

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Lines() {
        GLES20.glGenBuffers(3, vbos, 0);
        for(int i=0;i<100;i++) {
            p[i] = new Particle();
            p[i].nextX=rand.nextInt(maxN);
            p[i].nextY=rand.nextInt(maxN);
            p[i].z=i;
            for(int j=0;j<3;j++) {
                p[i].color[j] = rand.nextInt(10) / 10;
                colors[i*3 + j] = p[i].color[j];
            }
            modelCoords[i*3] = p[i].x;
            modelCoords[i*3+1] = p[i].y;
            modelCoords[i*3+2] = p[i].z;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                (modelCoords.length * 4));
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer[0] = bb.asFloatBuffer();
        vertexBuffer[0].put(modelCoords);
        vertexBuffer[0].position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0]);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer[0].capacity() * 150,
                null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[1]);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer[0].capacity() * 150,
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

        GLES20.glBindAttribLocation(mProgram, 0, "a_Position");


        // Recycle the bitmap, since its data has been loaded into OpenGL.

        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }


    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        if(br>=40000) br=0;
        for(int i=0;i<4;i++) {
            p[i].time+=(float)rand.nextInt(10)/30000;
            if(Math.round(p[i].x) == p[i].nextX) {
                p[i].time=0;
                p[i].nextX=rand.nextInt(maxN);
                p[i].nextY=rand.nextInt(maxN);
                p[i].nextZ=rand.nextInt(maxN);
                for(int j=0;j<3;j++) {
                    p[i].nextColor[j] = (float)rand.nextInt(2);
                }
            }
            p[i].x=lerp(p[i].x, p[i].nextX, p[i].time);
            p[i].y=lerp(p[i].y, p[i].nextY, p[i].time);
            p[i].z=lerp(p[i].z, p[i].nextZ, p[i].time);
            for(int j=0;j<3;j++) {
                p[i].color[j] = lerp(p[i].color[j], p[i].nextColor[j], p[i].time);
                colors[i * 3+j] = p[i].color[j];
            }
            modelCoords[i*3] = p[i].x;
            modelCoords[i*3+1] = p[i].y;
            modelCoords[i*3+2] = p[i].z;
        }
        //Log.e("sas", String.valueOf(br));
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                (modelCoords.length * 4));
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer[0] = bb.asFloatBuffer();
        vertexBuffer[0].put(modelCoords);
        vertexBuffer[0].position(0);
        //VERTEX
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0]);

        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,br, vertexBuffer[0].capacity() * 4,
                vertexBuffer[0]);

        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //COLOR
        ByteBuffer cb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                (colors.length * 4));
        cb.order(ByteOrder.nativeOrder());
        colorBuffer[0] = cb.asFloatBuffer();
        colorBuffer[0].put(colors);
        colorBuffer[0].position(0);

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[1]);

        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,br, colorBuffer[0].capacity() * 4,
                colorBuffer[0]);

        GLES20.glVertexAttribPointer(
                mColorHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glEnableVertexAttribArray(mColorHandle);


        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");


        GLES20.glDrawArrays(
                GLES20.GL_LINE_LOOP, 0, br/16);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);

        br+=4*4;
    }

    float lerp(float a,float b, float time){

        return (a + (b-a)*time);
    }
}