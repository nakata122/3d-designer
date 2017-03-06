package designer.project.a3dvectordesigner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    RelativeLayout screen;
    MyGLSurfaceView GLView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        screen = (RelativeLayout) findViewById(R.id.screen);
        GLView = new MyGLSurfaceView(MainActivity.this);
        screen.addView(GLView);
    }
}
