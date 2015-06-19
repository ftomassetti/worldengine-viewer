package me.tomassetti;

import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Image;
import me.tomassetti.worldengine.WorldFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by ftomassetti on 19/06/15.
 */
public class WorldEngineTexture extends Texture2D {

    public WorldEngineTexture(String fileName){
        final int width = 512;
        final int height = 512;
        try {
            
            WorldFile.World worldFile = WorldFile.World.parseFrom(new FileInputStream(new File(fileName)));
            ByteBuffer data = ByteBuffer.allocateDirect(width * height * 3);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (worldFile.getOcean().getRows(y).getCells(x)) {
                        data.put((y * width + x) * 3 + 0, (byte) 0);
                        data.put((y * width + x) * 3 + 1, (byte) 255);
                        data.put((y * width + x) * 3 + 2, (byte) 0);
                    } else {
                        data.put((y * width + x) * 3 + 0, (byte) 255);
                        data.put((y * width + x) * 3 + 1, (byte) 0);
                        data.put((y * width + x) * 3 + 2, (byte) 0);
                    }
                }
            }
            Image img = new Image(Image.Format.RGB8, width, height, data);
            this.setImage(img);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
}