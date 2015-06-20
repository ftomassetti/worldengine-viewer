package me.tomassetti;

import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import me.tomassetti.worldengine.WorldFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by ftomassetti on 19/06/15.
 */
public class WorldEngineTexture2 extends Texture2D {

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

    public WorldEngineTexture2(String fileName){
        final int scale = 1;
        try {
            
            WorldFile.World worldFile = WorldFile.World.parseFrom(new FileInputStream(new File(fileName)));
            final int width = worldFile.getWidth();
            final int height = worldFile.getHeight();
            ByteBuffer data = ByteBuffer.allocateDirect(width * height * 3 * scale * scale);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!worldFile.getOcean().getRows(y).getCells(x)) {
                        double elev = worldFile.getHeightMapData().getRows(y).getCells(x);
                        if (elev >= WorldEngineTexture.HIGH_MOUNTAIN_LEVEL) {
                            int baseY = y * width * scale * scale;
                            data.put((baseY + (x * scale)) * 3 + 0, (byte) 255);
                            data.put((baseY + (x * scale)) * 3 + 1, (byte) 0);
                            data.put((baseY + (x * scale)) * 3 + 2, (byte) 0);
                        } else if (elev >= WorldEngineTexture.MOUNTAIN_LEVEL) {
                            int baseY = y * width * scale * scale;
                            data.put((baseY + (x * scale)) * 3 + 0, (byte) 0);
                            data.put((baseY + (x * scale)) * 3 + 1, (byte) 255);
                            data.put((baseY + (x * scale)) * 3 + 2, (byte) 0);
                        } else {
                            int baseY = y * width * scale * scale;
                            data.put((baseY + (x * scale)) * 3 + 0, (byte) 0);
                            data.put((baseY + (x * scale)) * 3 + 1, (byte) 0);
                            data.put((baseY + (x * scale)) * 3 + 2, (byte) 0);
                        }
                    } else {
                        int baseY = y * width * scale * scale;
                        data.put((baseY + (x * scale)) * 3 + 0, (byte) 0);
                        data.put((baseY + (x * scale)) * 3 + 1, (byte) 0);
                        data.put((baseY + (x * scale)) * 3 + 2, (byte) 0);
                    }
                }
            }
            Image img = new Image(Image.Format.RGB8, width * scale, height * scale, data);
            this.setImage(img);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
}
