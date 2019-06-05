package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

public class Main extends SimpleApplication implements ActionListener {
    
    private BulletAppState bulletAppState;
    
    // Terrain
    private Spatial terrain;
    private RigidBodyControl terrainControl;
    private CollisionShape terrainShape;
    
    // Player
    private CapsuleCollisionShape playerShape;
    private CharacterControl player;
    private AudioNode footsteps;
    private boolean forward = false, backward = false, right = false, left = false;
    
    // Pick objects
    private Node pickObjects;
    private int posOrNeg;
    private Geometry[] cubes;
    private int cubeNumber;
    private BitmapText cubeText = null;
    private AudioNode pickAudio;
    
    // Cross hair
    private BitmapText cross;
    
    // Endgame
    private BitmapText end;
    
    // Music
    private AudioNode music;
    
    // camLoc and camDir
    private Vector3f camLoc = new Vector3f();
    private Vector3f camDir = new Vector3f();
    private BitmapText show;
    
    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        guiNode.detachAllChildren();
        
        flyCam.setMoveSpeed(100);
        
        initTerrain();
        initLight();
        initPlayer();
        initKeys();
        initCrossHair();
        initPickObjects();
        initShowCubeNumber();
        initFootsteps();
        initPickup();
        initMusic();
        
        camLoc.set(cam.getLocation());
        camDir.set(cam.getDirection());
        
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        show = new BitmapText(guiFont, false);
        show.setSize(guiFont.getCharSet().getRenderedSize());
        show.setText("camLoc: " + camLoc + "\n camDir: " + camDir);
        show.setLocalTranslation(settings.getWidth() - show.getLineWidth() - 200, settings.getHeight()/4 + show.getLineHeight(), 0);
        guiNode.attachChild(show);
    }
    
    public void initMusic() {
        music = new AudioNode(assetManager, "Sounds/soundtrack.ogg", DataType.Stream);
        music.setPositional(false);
        music.setLooping(true);
        music.setVolume(1);
        rootNode.attachChild(music);
        music.play();
    }
    
    public void initPickup() {
        pickAudio = new AudioNode(assetManager, "Sounds/pickup.ogg", DataType.Buffer);
        pickAudio.setPositional(false);
        pickAudio.setLooping(false);
        pickAudio.setVolume(3);
        rootNode.attachChild(pickAudio);
    }
    
    public void initFootsteps() {
        footsteps = new AudioNode(assetManager, "Sounds/footsteps.ogg", DataType.Stream);
        footsteps.setPositional(false);
        footsteps.setLooping(false);
        footsteps.setVolume(5);
        rootNode.attachChild(footsteps);
    }
    
    public void initShowCubeNumber() {
        
        if(cubeText != null) {
            guiNode.detachChild(cubeText);
        }
        
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        cubeText = new BitmapText(guiFont, false);
        cubeText.setSize(guiFont.getCharSet().getRenderedSize());
        cubeText.setText("Kisten: " + cubeNumber);
        cubeText.setLocalTranslation(settings.getWidth()/4 + settings.getWidth()/2, settings.getHeight(), 0);
        guiNode.attachChild(cubeText);
 
    }
    
    public void initCrossHair() {
        
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        cross = new BitmapText(guiFont, false);
        cross.setSize(30);
        cross.setColor(ColorRGBA.Gray);
        cross.setText(".");
        cross.setLocalTranslation(settings.getWidth()/2 - cross.getLineWidth()/2, settings.getHeight()/2 + cross.getLineHeight()/2, 0);
        guiNode.attachChild(cross);
 
    }
    
    public void initPickObjects() {
        Box b = new Box(1, 1, 1);
        cubes = new Geometry[100];
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/wood.png"));
        CollisionShape cubeShape;
        RigidBodyControl cubeControl;
        float x;
        float y;
        float z;        
        
        pickObjects = new Node();
        
        posOrNeg = (int) Math.round(Math.random());
        
        if(posOrNeg == 1) {
            x = (float) Math.random() * 301 * (-1);
            y = 200;
            z = (float) Math.random() * 301 * (-1);
        }
        else {
            x = (float) Math.random() * 301;
            y = 200;
            z = (float) Math.random() * 301;
        }
        
        for(int i = 0; i < cubes.length; i++) {
            cubes[i] = new Geometry("cube" + i+1, b);
            cubes[i].setMaterial(mat);
            cubeShape = CollisionShapeFactory.createBoxShape(cubes[i]);
            cubeControl = new RigidBodyControl(cubeShape, 10);
            cubes[i].addControl(cubeControl);
            cubeControl.setPhysicsLocation(new Vector3f(x, y, z));
            pickObjects.attachChild(cubes[i]);
            bulletAppState.getPhysicsSpace().add(cubes[i]);
            posOrNeg = (int) Math.round(Math.random());
            if(posOrNeg == 1) {
                x = (float) Math.random() * 301 * (-1);
                z = (float) Math.random() * 301 * (-1);
            }
            else {
                x = (float) Math.random() * 301;
                z = (float) Math.random() * 301;
            }
        }
        
        cubeNumber = 0;
        
        rootNode.attachChild(pickObjects);
    }
    
    public void initKeys() {
        inputManager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("new", new KeyTrigger(KeyInput.KEY_N));
        inputManager.addMapping("pick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        
        inputManager.addListener(this, "forward", "backward", "left", "right", "jump", "pick", "new");
    }
    
    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
        
        if(name.equals("forward")) {
            forward = keyPressed;
        }
        
        if(name.equals("backward")) {
            backward = keyPressed;
        }
        
        if(name.equals("left")) {
            left = keyPressed;
        }
        
        if(name.equals("right")) {
            right = keyPressed;
        }
        
        if(name.equals("jump") && !keyPressed) {
            player.jump(new Vector3f(0, 20, 0));
        }
        
        if(name.equals("pick") && !keyPressed) {
            CollisionResults results = new CollisionResults();
            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
            pickObjects.collideWith(ray, results);
            
            CollisionResult result = results.getClosestCollision();
            
            if(result != null) {
                
                if(cam.getLocation().distance(result.getContactPoint()) <= 10f) {
                    
                    if(cubeText != null) {
                        guiNode.detachChild(cubeText);
                    }
                    
                    
                    pickObjects.detachChild(result.getGeometry());
                    pickAudio.playInstance();
                    cubeNumber++;
                    cubeText.setText("Kisten: " + cubeNumber);
                    guiNode.attachChild(cubeText);
                    
                }
                
            }
            
        }
        
        if(name.equals("new") && !keyPressed) {
            if(cubeNumber == 100) {
                cubeNumber = 0;
                guiNode.detachChild(cubeText);
                cubeText.setText("Kisten: " + cubeNumber);
                guiNode.attachChild(cubeText);
                initPickObjects();
            }
        }
        
    }
    
    public void initPlayer() {
        playerShape = new CapsuleCollisionShape(1.5f, 10f, 1);
        player = new CharacterControl(playerShape, 0.5f);
        player.setFallSpeed(1000);
        player.setJumpSpeed(20);
        player.setPhysicsLocation(new Vector3f(0, 15f, 0));
        cam.setLocation(player.getPhysicsLocation().add(new Vector3f(0, 1.8f, 0)));
        bulletAppState.getPhysicsSpace().add(player);
    }
    
    public void initTerrain() {
        terrain = assetManager.loadModel("Scenes/Terrain2.j3o");
        terrainShape = CollisionShapeFactory.createMeshShape(terrain);
        terrainControl = new RigidBodyControl(terrainShape, 0);
        terrain.addControl(terrainControl);
        terrainControl.setPhysicsLocation(new Vector3f(0, -10, 0));
        rootNode.attachChild(terrain);
        bulletAppState.getPhysicsSpace().add(terrain);
    }
    
    public void initLight() {
        DirectionalLight sun = new DirectionalLight(new Vector3f(600, 500, 500));
        sun.setColor(ColorRGBA.Gray);
        sun.setDirection(terrainControl.getPhysicsLocation());
        rootNode.addLight(sun);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        
        setListener();
        
        go();
        
        gratulations();
        
        camLoc.set(cam.getLocation());
        camDir.set(cam.getDirection());
        
        //guiNode.detachChild(show);
        show.setText("camLoc: " + camLoc + "\n camDir: " + camDir);
        guiNode.attachChild(show);
        //guiNode.detachChild(show);
    }
    
    public void setListener() {
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
    }
    
    public void gratulations() {
        if(cubeNumber == 100) {
            
            guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
            end = new BitmapText(guiFont, false);
            end.setSize(20);
            end.setText("Congratulations! You collected all cubes!\n (Press \"N\" to start a new game!)");
            end.setColor(new ColorRGBA(205, 127, 50, 1));
            end.setLocalTranslation(0, settings.getHeight(), 0);
            guiNode.attachChild(end);
 
        }
    }
    
    public void go() {
        if(forward) {
            player.setWalkDirection(cam.getDirection().mult(1.2f));
            player.setWalkDirection(new Vector3f(cam.getDirection().x, 0, cam.getDirection().z));
            footsteps.play();
        }
        else if(backward) {
            player.setWalkDirection(cam.getDirection().negate().mult(1.2f));
            player.setWalkDirection(new Vector3f(cam.getDirection().negate().x, 0, cam.getDirection().negate().z));
            footsteps.play();
        }
        else if(left) {
            player.setWalkDirection(cam.getLeft().mult(1.2f));
            player.setWalkDirection(new Vector3f(cam.getLeft().x, 0, cam.getLeft().z));
            footsteps.play();
        }
        else if(right) {
            player.setWalkDirection(cam.getLeft().negate().mult(1.2f));
            player.setWalkDirection(new Vector3f(cam.getLeft().negate().x, 0, cam.getLeft().negate().z));
            footsteps.play();
        }
        else {
            player.setWalkDirection(new Vector3f(0, 0, 0));
            footsteps.stop();
        }
        
        
        cam.setLocation(player.getPhysicsLocation().add(new Vector3f(0, 1.8f, 0)));
        
    }
    
    public static void main (String[] args) {
        Main app = new Main();
        app.start();
    }
}