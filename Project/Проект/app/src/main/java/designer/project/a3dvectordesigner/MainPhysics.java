package designer.project.a3dvectordesigner;

import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

public class MainPhysics{
    public float[] touchPos = new float[3], startTouch = new float[3], lastTouch = new float[3];
    public float foxy=1,x,y,z;

    final MyGLRenderer render;

    MainPhysics(MyGLRenderer render) {
        this.render = render;
    }

    public void onMoveFrame() {

        lastTouch=touchPos;
    }

    public float[] rayPicking(float realX, float realY, int mode){
        float[]  far = new float[4],near = new float[4], ray = new float[4];

        int[] viewport = {0,0 ,(int)render.wid,(int)render.hei};

        float[] cam = {x, y, z};

        if(mode==0) {
            GLU.gluUnProject(realX, render.hei - realY, 0, render.mViewMatrix, 0, render.mProjectionMatrix, 0, viewport, 0, near, 0);
            GLU.gluUnProject(realX, render.hei - realY, 1, render.mViewMatrix, 0, render.mProjectionMatrix, 0, viewport, 0, far, 0);
        }else if(mode==1){
            GLU.gluUnProject(realX, realY, 0, render.m2dViewMatrix, 0, render.mPerspectiveMatrix, 0, viewport, 0, near, 0);
            float[] endResult = {near[0],near[1]+1, render.zoom};
            return endResult;
        }


        near = perspectiveDevision(near);
        far = perspectiveDevision(far);

        far[0]/=999;far[1]/=999;far[2]/=999;

        float t = cam[2]/far[2];
        ray[0] = (far[0] - (near[0]-cam[0])*t)-far[0]*2;
        ray[1] = (far[1] - (near[1]-cam[1])*t)-far[1]*2;
        ray[2] = (far[2] - (near[2]-cam[2])*t)-far[2]*2;

        float[] endResult = {ray[0],ray[1],render.zoom};
        if(mode==0){
            endResult[0] = near[0]+far[0];
            endResult[1] = near[1]+far[1];
            endResult[2] = near[2]+far[2];
        }


        return endResult;
    }
    public float[] perspectiveDevision(float[] vector3)
    {
        float[] normalizedVector = new float[3];
        float magnitude = (float) Math.sqrt((vector3[0] * vector3[0]) + (vector3[1] * vector3[1]) + (vector3[2] * vector3[2]));
        normalizedVector[0] = vector3[0] / vector3[3];
        normalizedVector[1] = vector3[1] / vector3[3];
        normalizedVector[2] = vector3[2] / vector3[3];
        return normalizedVector;
    }
    public float[] normalizeVector3(float[] vector3)
    {
        float[] normalizedVector = new float[3];
        float magnitude = (float) Math.sqrt((vector3[0] * vector3[0]) + (vector3[1] * vector3[1]) + (vector3[2] * vector3[2]));
        normalizedVector[0] = vector3[0] / magnitude;
        normalizedVector[1] = vector3[1] / magnitude;
        normalizedVector[2] = vector3[2] / magnitude;
        return normalizedVector;
    }
    public float length(float[] vector3)
    {
        float magnitude = (float) Math.sqrt((vector3[0] * vector3[0]) + (vector3[1] * vector3[1]) + (vector3[2] * vector3[2]));
        return magnitude;
    }
    public float[] normalizeQuat(float[] quat)
    {
        float[] normalizedVector = new float[4];
        float magnitude = (float) Math.sqrt((quat[1] * quat[1]) + (quat[2] * quat[2]) + (quat[3] * quat[3]));
        normalizedVector[1] = quat[1] / magnitude;
        normalizedVector[2] = quat[2] / magnitude;
        normalizedVector[3] = quat[3] / magnitude;
        return normalizedVector;
    }
    public float[] crossProduct(float[] v1, float[] v2)
    {
        float[] cross = {v1[1]*v2[2] - v1[2]*v2[1], v1[2]*v2[0] - v1[0]*v2[2], v1[0]*v2[1] - v1[1]*v2[0]};
        return cross;
    }
    public float dotProduct(float[] v1, float[] v2)
    {
        float dot = v1[0]*v2[0] + v1[1]*v2[1]  + v1[2]*v2[2];
        return dot;
    }
}
