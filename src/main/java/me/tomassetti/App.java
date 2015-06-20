package me.tomassetti;


import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.water.SimpleWaterProcessor;
import com.jme3.water.WaterFilter;
import com.sun.scenario.effect.AbstractShadow;

import java.io.File;

/**
 * Sample 10 - How to create fast-rendering terrains from heightmaps,
 * and how to use texture splatting to make the terrain look good.
 */
public class App extends SimpleApplication implements ActionListener {

    //private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;

    //Temporary vectors used on each frame.
    //They here to avoid instanciating new vectors on each frame
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    
    private TerrainQuad terrain;
    Material mat_terrain;
    private FilterPostProcessor fpp;
    private WaterFilter water;
    private Vector3f lightDir = new Vector3f(-4.9f, -1.3f, 5.9f); // same as light source
    private float initialWaterHeight = -100.5f; // choose a value for your scene

    public static void main(String[] args) {
        App app = new App();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        flyCam.setMoveSpeed(10000);
        flyCam.setZoomSpeed(100000.f);

        setUpKeys();
        setUpLight();

        

        assetManager.registerLocator(".", FileLocator.class);

        /** 1. Create terrain material and load four textures into it. */
        mat_terrain = new Material(assetManager,
                "MatDefs/Terrain.j3md");
        
        //String worldEngineFile = "seed_64513.world";
        String worldEngineFile = "seed_59416.world";

        /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
        /*mat_terrain.setTexture("Alpha", assetManager.loadTexture(
                "Textures/Terrain/splat/alphamap.png"));*/
        WorldEngineTexture worldEngineTexture = new WorldEngineTexture(worldEngineFile);
        mat_terrain.setTexture("Alpha", worldEngineTexture.getAlpha1());
        mat_terrain.setTexture("Alpha2", worldEngineTexture.getAlpha2());
        mat_terrain.setTexture("Alpha3", worldEngineTexture.getAlpha3());

        /** 1.2) Add GRASS texture into the red layer (Tex1). */
        Texture grass = assetManager.loadTexture(
                "Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex1", grass);
        mat_terrain.setFloat("Tex1Scale", 64f);

        /** 1.3) Add DIRT texture into the green layer (Tex2) */
        Texture dirt = assetManager.loadTexture(
                "Textures/Terrain/splat/water.jpg");
        dirt.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex2", dirt);
        mat_terrain.setFloat("Tex2Scale", 32f);

        /** 1.4) Add ROAD texture into the blue layer (Tex3) */
        Texture rock = assetManager.loadTexture(
                "Textures/Terrain/splat/dirt.jpg");
        rock.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex3", rock);
        mat_terrain.setFloat("Tex3Scale", 128f);

        Texture ice = assetManager.loadTexture(
                "Textures/Terrain/splat/ice.jpg");
        ice.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex4", ice);
        mat_terrain.setFloat("Tex4Scale", 128f);

        Texture mountain = assetManager.loadTexture(
                "Textures/Terrain/splat/mountain.jpg");
        mountain.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex5", mountain);
        mat_terrain.setFloat("Tex5Scale", 128f);

        Texture sandDesert = assetManager.loadTexture(
                "Textures/Terrain/splat/sanddesert.jpg");
        sandDesert.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex6", sandDesert);
        mat_terrain.setFloat("Tex6Scale", 128f);

        Texture rockDesert = assetManager.loadTexture(
                "Textures/Terrain/splat/rockdesert.jpg");
        rockDesert.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex7", rockDesert);
        mat_terrain.setFloat("Tex7Scale", 128f);

        Texture forest = assetManager.loadTexture(
                "Textures/Terrain/splat/forest.jpg");
        forest.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex8", forest);
        mat_terrain.setFloat("Tex8Scale", 128f);

        Texture jungle = assetManager.loadTexture(
                "Textures/Terrain/splat/jungle.jpg");
        jungle.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex9", jungle);
        mat_terrain.setFloat("Tex9Scale", 128f);

        /** 2. Create the height map */
        AbstractHeightMap heightmap = null;
        /*Texture heightMapImage = assetManager.loadTexture(
                "Textures/Terrain/splat/mountains512.png");
        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());*/
        heightmap = new WorldEngineHeightMap(worldEngineFile);
        heightmap.load();
        heightmap.erodeTerrain();

        /** 3. We have prepared material and heightmap.
         * Now we create the actual terrain:
         * 3.1) Create a TerrainQuad and name it "my terrain".
         * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
         * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
         * 3.4) As LOD step scale we supply Vector3f(1,1,1).
         * 3.5) We supply the prepared heightmap itself.
         */
        int patchSize = 65;
        int size = 1025;
        terrain = new TerrainQuad("my terrain", patchSize, size, heightmap.getHeightMap());

        /** 4. We give the terrain its material, position & scale it, and attach it. */
        terrain.setMaterial(mat_terrain);
        terrain.setLocalTranslation(0, -100, 0);
        terrain.setLocalScale(2f, 1f, 2f);
        //rootNode.attachChild(terrain);

        /** 5. The LOD (level of detail) depends on were the camera is: */
        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
        terrain.addControl(control);
        
        //Node node = terrain;

        // we create a water processor
        /*SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
        waterProcessor.setReflectionScene(terrain);

        // we set the water plane
        Vector3f waterLocation=new Vector3f(0,-6,0);
        waterProcessor.setPlane(new Plane(Vector3f.UNIT_Y, waterLocation.dot(Vector3f.UNIT_Y)));
        viewPort.addProcessor(waterProcessor);

        // we set wave properties
        waterProcessor.setWaterDepth(40);         // transparency of water
        waterProcessor.setDistortionScale(0.05f); // strength of waves
        waterProcessor.setWaveSpeed(0.05f);       // speed of waves

        // we define the wave size by setting the size of the texture coordinates
        Quad quad = new Quad(400,400);
        quad.scaleTextureCoordinates(new Vector2f(6f,6f));

        // we create the water geometry from the quad
        Geometry water=new Geometry("water", quad);
        water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        water.setLocalTranslation(-200, -6, 2);
        //water.setShadowMode(AbstractShadow.ShadowMode.Receive);
        water.setMaterial(waterProcessor.getMaterial());
        rootNode.attachChild(water);*/

        CollisionShape sceneShape =
                CollisionShapeFactory.createMeshShape((Node) terrain);
        landscape = new RigidBodyControl(sceneShape, 0);
        terrain.addControl(landscape);

        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(200000);
        player.setFallSpeed(300000);
        player.setGravity(0);
        player.setPhysicsLocation(new Vector3f(0, 2, 0));

        // We attach the scene and the player to the rootnode and the physics space,
        // to make them appear in the game world.
        rootNode.attachChild(terrain);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);

        /*fpp = new FilterPostProcessor(assetManager);
        water = new WaterFilter(rootNode, lightDir);
        water.setWaterHeight(initialWaterHeight);
        fpp.addFilter(water);
        viewPort.addProcessor(fpp);*/
    }

    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }

    /** We over-write some navigational key mappings here, so we can
     * add physics-controlled walking and jumping: */
    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
    }

    /**
     * This is the main event loop--walking happens here.
     * We check in which direction the player is walking by interpreting
     * the camera direction forward (camDir) and to the side (camLeft).
     * The setWalkDirection() command is what lets a physics-controlled player walk.
     * We also make sure here that the camera moves with player.
     */
    @Override
    public void simpleUpdate(float tpf) {
        camDir.set(cam.getDirection()).multLocal(10.f*0.6f);
        camLeft.set(cam.getLeft()).multLocal(10.f*0.4f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }

    /** These are our custom actions triggered by key presses.
     * We do not walk yet, we just keep track of the direction the user pressed. */
    public void onAction(String binding, boolean isPressed, float tpf) {
        if (binding.equals("Left")) {
            left = isPressed;
        } else if (binding.equals("Right")) {
            right= isPressed;
        } else if (binding.equals("Up")) {
            up = isPressed;
        } else if (binding.equals("Down")) {
            down = isPressed;
        } else if (binding.equals("Jump")) {
            if (isPressed) { player.jump(); }
        }
    }
}
