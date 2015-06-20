package me.tomassetti;

import com.google.protobuf.CodedInputStream;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import me.tomassetti.worldengine.WorldFile;
import org.lwjgl.Sys;

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
            CodedInputStream codedInputStream = CodedInputStream.newInstance(new FileInputStream(new File(worldFilename)));
            codedInputStream.setSizeLimit(Integer.MAX_VALUE);
            WorldFile.World worldFile = WorldFile.World.parseFrom(codedInputStream);
            this.size = Math.max(worldFile.getWidth(), worldFile.getHeight());
            this.heightData = new float[this.getSize() * this.getSize()];
            float minValue = Float.MAX_VALUE;
            float maxValue = Float.MIN_VALUE;
            for (int y=0;y<worldFile.getHeight();y++) {
                WorldFile.World.DoubleRow row = worldFile.getHeightMapData().getRows(y);
                for (int x = 0; x < worldFile.getWidth(); x++) {
                    float value;
                    if (worldFile.getOcean().getRows(y).getCells(x)){
                        value = 0.0f + (float)(0.0f*row.getCells(x));
                        //value = 10.0f;
                    } else {
                        if (row.getCells(x) < minValue){
                            minValue = (float)row.getCells(x);
                        }
                        if (row.getCells(x) > maxValue){
                            maxValue = (float)row.getCells(x);
                        }
                        value = 0.0f + (float)(5.0f*row.getCells(x));
                    }
                    this.heightData[(worldFile.getHeight() - y - 1) * worldFile.getWidth() + x] = value;
                }
            }
            System.out.println("MIN "+minValue);
            System.out.println("MAX "+maxValue);
            return true;
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
    
}
