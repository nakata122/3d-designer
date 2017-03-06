package designer.project.a3dvectordesigner;

public abstract class Object{
    float posX=0,posY=0,posZ=3,width=0.2f,height=0.2f, alpha=1.0f;
    final float[] PosMatrix = new float[16];
    boolean open=false;
    MyGLRenderer.btnTex tex = MyGLRenderer.btnTex.add;

    Object(MyGLRenderer.btnTex tex, float posX, float posY) {
        this.posX = posX;
        this.posY = posY;
        this.tex = tex;
    }
    boolean isClicked(float x, float y){
        if(x>=posX && y>=posY && x<=posX+width && y<=posY+height)
            return true;
        else
            return false;

    }
    public abstract String Type();
}
