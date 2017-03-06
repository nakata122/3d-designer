package designer.project.a3dvectordesigner;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by User on 3.8.2016 г..
 */
public class LoadModel {
    private GLSurfaceView mGLView;
    XmlPullParserFactory pullParserFactory;
    boolean done=false;
    int mode=0;
    float size=1;


    public boolean load(Context ctx, String name, float s, int mod){
        mode=mod;
        size=s;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            InputStream in_s = ctx.getAssets().open(name);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);

            parseXML(parser);

        } catch (XmlPullParserException e) {

            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(done) {
            Log.e("Loader", "True");
            return true;
        }
        Log.e("Loader", "False");
        return false;
    }
        private void parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
            int eventType = parser.getEventType();
            float[] vertices = new float[30000], vertsOrdered = new float[100000], textureCoords = new float[50000], normalCoords = new float[50000];
            short[] f = new short[50000];
            float[] tex = new float[100000];
            float[] norm = new float[100000];
            boolean[][] bones = new boolean[3][10000];
            short[] v = new short[50000];
            short[] vcount = new short[10000];
            String vertext,count,textext;
            String[] items, order, counts = new String[10000], vTex, textureId,normals;
            float minX=100,maxX=-100,minY=100,maxY=-100,minZ=100,maxZ=-100;
            int temp,texTemp=0;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = null;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.matches("verts")) {
                            vertext = parser.nextText();
                            items = vertext.split(" ");

                            int point=0;
                            for(int i=0;i<items.length;i++){
                                if(point==3) point=0;
                                vertices[i] = Float.parseFloat(items[i])*size;
                                if(point==0) {
                                    if (vertices[i] < minX) minX = vertices[i];
                                    if (vertices[i] > maxX) maxX = vertices[i];
                                }
                                if(point==1) {
                                    if (vertices[i] < minY) minY = vertices[i];
                                    if (vertices[i] > maxY) maxY = vertices[i];
                                }
                                if(point==2) {
                                    if (vertices[i] < minZ) minZ = vertices[i];
                                    if (vertices[i] > maxZ) maxZ = vertices[i];
                                }
                                point++;
                            }

                            if(mode==2) {
                                Pen.loadVerts(vertices,items.length);
                            }
                        } else if (name.matches("normal")){
                            textext = parser.nextText();
                            normals = textext.split(" ");

                            for(int i=0;i<normals.length;i++){
                                normalCoords[i] = Float.parseFloat(normals[i]);
                            }
                        } else if (name.matches("tex")){
                            textext = parser.nextText();
                            textureId = textext.split(" ");

                            for(int i=0;i<textureId.length;i++){
                                textureCoords[i] = Float.parseFloat(textureId[i]);
                            }

                        } else if (name.matches("p")) {
                            vertext = parser.nextText();
                            order = vertext.split(" ");

                            temp=0;
                            texTemp=0;
                            for(int i=0;i<order.length;i+=3){
                                f[temp] = Short.parseShort(order[i]);
                                tex[texTemp] = textureCoords[Integer.parseInt(order[i + 2]) * 2];
                                tex[texTemp+1] = 1-textureCoords[Integer.parseInt(order[i + 2]) * 2 + 1];
                                norm[i] = normalCoords[Integer.parseInt(order[i + 1])*3];
                                norm[i+1] = normalCoords[Integer.parseInt(order[i + 1])*3+1];
                                norm[i+2] = normalCoords[Integer.parseInt(order[i + 1])*3+2];
                                vertsOrdered[i] = vertices[f[temp]*3];
                                vertsOrdered[i+1] = vertices[f[temp]*3+1];
                                vertsOrdered[i+2] = vertices[f[temp]*3+2];

                                temp++;
                                texTemp+=2;
                            }
                            if(mode==0) {
                                Model.loadVertBuff(vertsOrdered);
                                Model.loadTexBuff(tex);
                                Model.loadNormBuff(norm);
                            }else if(mode==1) {
                                Model2d.loadVertBuff(vertsOrdered);
                                Model2d.loadTexBuff(tex);
                                Model2d.loadNormBuff(norm);
                            }
                        } else if (name.matches("vcount")) {
                            count = parser.nextText();
                            counts = count.split(" ");
                            for(int i=0;i<counts.length;i++) {
                                vcount[i] = Short.parseShort(counts[i]);
                            }

                            Model.vcount = counts.length;
                        } else if (name.matches("v")) {
                            count = parser.nextText();
                            vTex = count.split(" ");
                            temp=0;
                            for(int i=0;i<vTex.length;i++) {
                                v[i] = Short.parseShort(vTex[i]);
                            }
                            for(int i=0;i<counts.length;i++){
                                for(int j=0;j<vcount[i];j++){
                                    if(v[temp]!=-1)
                                        bones[v[temp]][i]=true;
                                    temp+=2;
                                }
                            }

                            Model.bones = bones;
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        done = true;
                        break;
                }
                eventType = parser.next();
            }
        }
    }

