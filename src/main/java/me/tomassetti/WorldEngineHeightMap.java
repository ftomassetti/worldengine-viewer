package me.tomassetti;

import com.jme3.terrain.heightmap.AbstractHeightMap;
import me.tomassetti.worldengine.WorldFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.server.ExportException;

/**
 * Created by ftomassetti on 19/06/15.
 */
public class WorldEngineHeightMap extends AbstractHeightMap {
    
    private String worldFilename;

    public WorldEngineHeightMap(String worldFilename) {
        this.worldFilename = worldFilename;
    }

    @Override
    public boolean load() {
        try {
            WorldFile.World worldFile = WorldFile.World.parseFrom(new FileInputStream(new File(this.worldFilename)));
            this.size = Math.max(worldFile.getWidth(), worldFile.getHeight());
            this.heightData = new float[this.getSize() * this.getSize()];
            for (int y=0;y<worldFile.getHeight();y++) {
                WorldFile.World.DoubleRow row = worldFile.getHeightMapData().getRows(y % worldFile.getHeight());
                for (int x = 0; x < worldFile.getWidth(); x++) {
                    float value = (float)(5.0f*row.getCells(x));
                    if (!worldFile.getOcean().getRows(y).getCells(x)){
                        value += 1.0f;
                    }
                    this.heightData[y * worldFile.getWidth() + x] = value;
                }
            }
            return true;
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
    
}