package designer.project.a3dvectordesigner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {

    LoadModel loader;
    ImageView img;
    int a=10;
    Thread th;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        img = (ImageView) findViewById(R.id.imageView);
        loader = new LoadModel();

        th = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    img.setRotation(a);
                    a++;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        th.start();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
//        if (loader.load(this, "plane.xml", 0.05f,0)) {
//            Model.textures[Model.br - 1] = BitmapFactory.decodeResource(getResources(), R.drawable.water, options);
//        }
//        if (loader.load(this, "cube.xml", 0.05f,0)) {
//            Model.textures[Model.br - 1] = BitmapFactory.decodeResource(getResources(), R.drawable.cubtex, options);
//        }
//        if (loader.load(this, "arrow.xml", 0.05f,0)) {
//            Model.textures[Model.br - 1] = BitmapFactory.decodeResource(getResources(), R.drawable.cubtex, options);
//        }
        if (loader.load(this, "button.xml", 0.1f,1)) {
            Model2d.textures[0] = BitmapFactory.decodeResource(getResources(), R.drawable.plus, options);
        }
        loader.load(this, "disk.xml", 0.1f,2);
        Model2d.textures[1] = BitmapFactory.decodeResource(getResources(), R.drawable.options, options);
        Model2d.textures[2] = BitmapFactory.decodeResource(getResources(), R.drawable.white, options);
        Model2d.textures[3] = BitmapFactory.decodeResource(getResources(), R.drawable.draw, options);
        Model2d.textures[4] = BitmapFactory.decodeResource(getResources(), R.drawable.emitter, options);
        Model2d.textures[5] = BitmapFactory.decodeResource(getResources(), R.drawable.save, options);
        Vector.texture = BitmapFactory.decodeResource(getResources(), R.drawable.plus, options);
        Text.texture = BitmapFactory.decodeResource(getResources(), R.drawable.font, options);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
