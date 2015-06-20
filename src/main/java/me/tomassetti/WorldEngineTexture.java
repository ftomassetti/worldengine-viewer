package me.tomassetti;

import com.google.protobuf.CodedInputStream;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Image;
import me.tomassetti.worldengine.SimpleTexture;
import me.tomassetti.worldengine.WorldFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by ftomassetti on 19/06/15.
 */
public class WorldEngineTexture {
    
    public static float MOUNTAIN_LEVEL = 2.5f;
    public static float HIGH_MOUNTAIN_LEVEL = 5.0f;

    public SimpleTexture getAlpha1() {
        return alpha1;
    }

    public SimpleTexture getAlpha2() {
        return alpha2;
    }

    public SimpleTexture getAlpha3() {
        return alpha3;
    }

    private SimpleTexture alpha1;
    private SimpleTexture alpha2;
    private SimpleTexture alpha3;
    
    private boolean isBeach(WorldFile.World worldFile, int x, int y){
        int delta = 2;
        for (int dx=-delta;dx<=delta && (dx+x)>=0 && (dx+x)<worldFile.getWidth();dx++){
            for (int dy=-delta;dy<=delta && (dy+y)>=0 && (dy+y)<worldFile.getHeight();dy++){
                if (!worldFile.getOcean().getRows(y+dy).getCells(x+dx)){
                    return true;
                }
            }
        }
        return false;
    }
    
    private void setRed(ByteBuffer bb, int x, int baseY){
        bb.put((baseY + x) * 3 + 0, (byte) 255);
    }

    private void setGreen(ByteBuffer bb, int x, int baseY){
        bb.put((baseY + x) * 3 + 1, (byte) 255);
    }

    private void setBlue(ByteBuffer bb, int x, int baseY){
        bb.put((baseY + x) * 3 + 2, (byte) 255);
    }    
    
    private boolean isSandDesert(int biomeIndex){
        switch (biomeIndex){
            case 19:case 20:case 26:case 27:
                return true;
            default:
                return false;
        }
    }

    private boolean isRockDesert(int biomeIndex){
        switch (biomeIndex){
            case 34:case 35:
                return true;
            default:
                return false;
        }
    }

    private boolean isForest(int biomeIndex){
        switch (biomeIndex){
            case 7:case 8:case 10:case 21: case 28: case 32:
                return true;
            default:
                return false;
        }
    }

    private boolean isJungle(int biomeIndex){
        switch (biomeIndex){
            case 22:case 23:case 29:case 30:
                return true;
            default:
                return false;
        }
    }

    public WorldEngineTexture(String fileName){
        try {
            CodedInputStream codedInputStream = CodedInputStream.newInstance(new FileInputStream(new File(fileName)));
            codedInputStream.setSizeLimit(Integer.MAX_VALUE);
            WorldFile.World worldFile = WorldFile.World.parseFrom(codedInputStream);
            final int width = worldFile.getWidth();
            final int height = worldFile.getHeight();
            ByteBuffer data1 = ByteBuffer.allocateDirect(width * height * 3);
            ByteBuffer data2 = ByteBuffer.allocateDirect(width * height * 3);
            ByteBuffer data3 = ByteBuffer.allocateDirect(width * height * 3);
            for (int i=0;i<width*height*3;i++){
                data1.put(0, (byte)0);
                data2.put(0, (byte)0);
                data3.put(0, (byte)0);
            }
            
            for (int y = 0; y < height; y++) {
                int baseY = y * width;
                for (int x = 0; x < width; x++) {
                    double elev = worldFile.getHeightMapData().getRows(y).getCells(x);
                    if (worldFile.getOcean().getRows(y).getCells(x)) {
                        if (isBeach(worldFile, x, y)){
                            setBlue(data1, x, baseY);
                        } else {
                            setGreen(data1, x, baseY);
                        }
                    } else {
//                        sand desert
//                        19 20
//                        26 27
//
//                        rock desert
//                        34 35
//
//                        forest
//                        21 28 32 7 8 10
//
//                        jungle
//                        22 23 29 30

                        int biomeIndex = worldFile.getBiome().getRows(y).getCells(x);
                        if (isSandDesert(biomeIndex)) {
                            setBlue(data2, x, baseY);
                        } else if (isRockDesert(biomeIndex)) {
                            setRed(data3, x, baseY);
                        } else if (isForest(biomeIndex)) {
                            setGreen(data3, x, baseY);
                        } else if (isJungle(biomeIndex)) {
                            setBlue(data3, x, baseY);
                        } else if (elev >= HIGH_MOUNTAIN_LEVEL) {
                            setRed(data2, x, baseY);
                        } else if (elev >= MOUNTAIN_LEVEL) {
                                setGreen(data2, x, baseY);
                        } else {
                            setRed(data1, x, baseY);
                        }
                    }
                }
            }
            alpha1 = new SimpleTexture(worldFile.getWidth(), worldFile.getHeight(), data1);
            alpha2 = new SimpleTexture(worldFile.getWidth(), worldFile.getHeight(), data2);
            alpha3 = new SimpleTexture(worldFile.getWidth(), worldFile.getHeight(), data3);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
}
