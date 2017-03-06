package designer.project.a3dvectordesigner;

import android.util.Log;

public class TextObj extends Object{
    private static final float LETTER_WID = 0.125f;
    public float RI_TEXT_WIDTH = 0.05f;
    public String text;
    public float x;
    public float y;
    public float[] color;
    float[] vecs = new float[300];
    float[] colors = new float[400];
    float[] uvs = new float[300];
    short[] indices = new short[100];
    int index_vecs=0, index_colors=0, index_uvs=0, index_indices=0;

    TextObj(String txt, float posX, float posY, float size) {
        super(null, posX, posY);
            text = txt;
            RI_TEXT_WIDTH = size;
            init();
            x = posX;
            y = posY;
            color = new float[] {1, 1, 1, 1.0f};
    }
    //a - 97

    private void init(){
        char[] letters = new char[30];
        int[] index = new int[30];


        for(int i=0;i<text.length();i++) {
            letters[i] = text.charAt(i);
            index[i] = (int)letters[i]-97;
        }

        for(int i=0;i<text.length();i++){
            float[] uv = new float[8], vec = new float[12];
            int row = index[i] / 8;
            int col = index[i] % 8;
            Log.e("Str", String.valueOf(row + " " + col));

            vec[0] = x;
            vec[1] = y + RI_TEXT_WIDTH;
            vec[2] = 0.99f;
            vec[3] = x;
            vec[4] = y;
            vec[5] = 0.99f;
            vec[6] = x + RI_TEXT_WIDTH;
            vec[7] = y;
            vec[8] = 0.99f;
            vec[9] = x + RI_TEXT_WIDTH;
            vec[10] = y + RI_TEXT_WIDTH;
            vec[11] = 0.99f;

            float v = row * LETTER_WID;
            float v2 = v + LETTER_WID;
            float u = col * LETTER_WID;
            float u2 = u + LETTER_WID;
            uv[0] = u+0.001f;
            uv[1] = v+0.001f;
            uv[2] = u+0.001f;
            uv[3] = v2-0.001f;
            uv[4] = u2-0.001f;
            uv[5] = v2-0.001f;
            uv[6] = u2-0.001f;
            uv[7] = v+0.001f;
            short[] inds = {0, 1, 2, 0, 2, 3};
            float[] colr = {1,1,1,1};
            AddCharRenderInformation(vec, colr, uv, inds);
            x += RI_TEXT_WIDTH;
        }
        Text.vcount = text.length()*6;
        Text.loadVertBuff(vecs);
        Text.loadIndBuff(indices);
        Text.loadTexBuff(uvs);
    }
    public void AddCharRenderInformation(float[] vec, float[] cs, float[] uv, short[] indi)
    {
        // We need a base value because the object has indices related to
        // that object and not to this collection so basicly we need to
        // translate the indices to align with the vertexlocation in ou
        // vecs array of vectors.
        short base = (short) (index_vecs / 3);

        // We should add the vec, translating the indices to our saved vector
        for(int i=0;i<vec.length;i++)
        {
            vecs[index_vecs] = vec[i];
            index_vecs++;
        }

        for(int i=0;i<cs.length;i++)
        {
            colors[index_colors] = cs[i];
            index_colors++;
        }

        for(int i=0;i<uv.length;i++)
        {
            uvs[index_uvs] = uv[i];
            index_uvs++;
        }

        for(int j=0;j<indi.length;j++)
        {
            indices[index_indices] = (short) (base + indi[j]);
            index_indices++;
        }
    }

    @Override
    public String Type() {
        return "Text";
    }
}
