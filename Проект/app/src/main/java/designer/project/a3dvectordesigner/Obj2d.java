package designer.project.a3dvectordesigner;

/**
 * Created by User on 5.3.2017 г..
 */

public class Obj2d extends Object{
    Obj2d(MyGLRenderer.btnTex tex, float posX, float posY) {
        super(tex, posX, posY);
    }

    @Override
    public String Type() {
        return "2d";
    }
}
