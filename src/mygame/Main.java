package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author Volker Schuller
 */
public class Main extends SimpleApplication implements ActionListener {

    private Node dog;
    private BulletAppState bulletAppState = new BulletAppState();
    private DogControl dogControl;
    private boolean skinchange;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        this.stateManager.attach(bulletAppState);
        dogControl = new DogControl(bulletAppState, assetManager);
        Node node = (Node) assetManager.loadModel("Scenes/HouseScene2.j3o");
        rootNode.attachChild(node);
        CollisionShape sceneShape =
                CollisionShapeFactory.createMeshShape((Node) node);
        RigidBodyControl sceneBody = new RigidBodyControl(sceneShape, 0);
        node.addControl(sceneBody);
        bulletAppState.getPhysicsSpace().addCollisionObject(sceneBody);

        dog = (Node) assetManager.loadModel("Models/dog.j3o");
        dog.setLocalTranslation(-10, 2f, 0);
        rootNode.attachChild(dog);
        dog.addControl(dogControl);

        getFlyByCamera().setEnabled(false);
        ChaseCamera chaseCam = new ChaseCamera(getCamera(), dog, getInputManager());
        chaseCam.setSmoothMotion(true);
        chaseCam.setMinDistance(3);
        chaseCam.setDefaultDistance(5);
        chaseCam.setMaxDistance(10);

        /**
         * A white, directional light source
         */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        DirectionalLight sun2 = new DirectionalLight();
        sun2.setDirection((new Vector3f(0.5f, 0.5f, 0.5f)).normalizeLocal());
        sun2.setColor(ColorRGBA.White);
        rootNode.addLight(sun2);
        setupKeys(inputManager);
    }

    private void setupKeys(InputManager inputManager) {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Mark", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Change Skin", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Forward");
        inputManager.addListener(this, "Backward");
        inputManager.addListener(this, "Mark");
        inputManager.addListener(this, "Change Skin");
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
            dogControl.setLeftRotate(value);
        } else if (binding.equals("Right")) {
            dogControl.setRightRotate(value);
        } else if (binding.equals("Forward")) {
            dogControl.setForward(value);
        } else if (binding.equals("Backward")) {
            dogControl.setBackward(value);
        } else if (binding.equals("Mark")) {
            dogControl.mark();
        } else if (binding.equals("Change Skin")) {
            if (value) {
                ((Node) dog.getChild("Dog")).getChild(0).setMaterial(assetManager.loadMaterial(skinchange ? "Materials/Generated/dog-skin.j3m" : "Materials/Generated/dog-skin2.j3m"));
                skinchange = !skinchange;
            }
        }
    }
}
