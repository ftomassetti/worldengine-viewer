package me.tomassetti.worldengine;

import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;

import java.nio.ByteBuffer;

/**
 * Created by ftomassetti on 20/06/15.
 */
public class SimpleTexture extends Texture2D {
    
    public SimpleTexture(int width, int height, ByteBuffer data){
        Image img = new Image(Image.Format.RGB8, width, height, data);
        this.setImage(img);
    }
    
}
