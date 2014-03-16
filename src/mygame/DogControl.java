package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Cylinder;
import mygame.newpackage.tools.Area;

/**
 *
 * @author Volker Schuller
 */
public class DogControl extends AbstractControl implements AnimEventListener {

    private BulletAppState bulletAppState;
    private BetterCharacterControl physicsCharacter;
    private AssetManager assetManager;
    private Node dog;
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private Vector3f viewDirection = new Vector3f(0, 0, 1);
    boolean forward = false, backward = false,
            leftRotate = false, rightRotate = false;
    private AnimChannel channel;
    private ParticleEmitter pee;
    private Material puddleMaterial;
    private Area area;

    public DogControl(BulletAppState bulletAppState, AssetManager assetManager) {
        this.bulletAppState = bulletAppState;
        this.assetManager = assetManager;
        this.puddleMaterial = assetManager.loadMaterial("Materials/puddle.j3m");
        area = new Area(assetManager);
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        DogControl dogControl = new DogControl(bulletAppState, assetManager);
        dogControl.setSpatial(spatial);
        return dogControl;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        dog = (Node) spatial;
        Node dognode = (Node) dog.getChild("Dog");
        AnimControl anim = dognode.getControl(AnimControl.class);
        pee = (ParticleEmitter) dog.getChild("Emitter");
        anim.addListener(this);
        channel = anim.createChannel();
        channel.setAnim("idle");
        physicsCharacter = dog.getControl(BetterCharacterControl.class);
        if (physicsCharacter == null) {
            physicsCharacter = new BetterCharacterControl(1f, 2f, 30f);
            dog.addControl(physicsCharacter);
        }
        if (bulletAppState != null) {
            bulletAppState.getPhysicsSpace().add(physicsCharacter);
        }
        area.setNode(spatial.getParent());
    }

    @Override
    protected void controlUpdate(float tpf) {
        // Get current forward and left vectors of model by using its rotation
        // to rotate the unit vectors
        Vector3f modelForwardDir = dog.getWorldRotation().mult(Vector3f.UNIT_Z);

        // WalkDirection is global!
        // You *can* make your character fly with this.
        walkDirection.set(0, 0, 0);
        if (forward) {
            walkDirection.addLocal(modelForwardDir.mult(3));
        } else if (backward) {
            walkDirection.addLocal(modelForwardDir.negate().multLocal(3));
        }
        physicsCharacter.setWalkDirection(walkDirection);

        // ViewDirection is local to characters physics system!
        // The final world rotation depends on the gravity and on the state of
        if (leftRotate) {
            Quaternion rotateL = new Quaternion().fromAngleAxis(FastMath.PI * tpf, Vector3f.UNIT_Y);
            rotateL.multLocal(viewDirection);
        } else if (rightRotate) {
            Quaternion rotateR = new Quaternion().fromAngleAxis(-FastMath.PI * tpf, Vector3f.UNIT_Y);
            rotateR.multLocal(viewDirection);
        }
        physicsCharacter.setViewDirection(viewDirection);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void setForward(boolean forward) {
        this.forward = forward;
        if (forward) {
            channel.setAnim("walk");
            channel.setSpeed(2);
        } else {
            channel.setAnim("idle");
        }
    }

    public void setBackward(boolean backward) {
        this.backward = backward;
        if (backward) {
            channel.setAnim("walk");
            channel.setSpeed(-2);
        } else {
            channel.setAnim("idle");
        }
    }

    public void setLeftRotate(boolean leftRotate) {
        this.leftRotate = leftRotate;
    }

    public void setRightRotate(boolean rightRotate) {
        this.rightRotate = rightRotate;
    }

    public void mark() {
        forward = false;
        backward = false;
        channel.setAnim("mark");
        dog.attachChild(pee);
        pee.setEnabled(true);
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (animName.equals("mark")) {
            channel.setAnim("idle");
            pee.setEnabled(false);
            pee.killAllParticles();
            createPuddle();
        }
    }

    private void createPuddle() {
        Cylinder puddle = new Cylinder(4, 24, 0.3f, 0.025f, true);
        Geometry puddleGeom = new Geometry("puddle", puddle);
        puddleGeom.setLocalRotation(new Quaternion(new float[]{FastMath.PI / 2, 0, 0}));
        Vector3f pos = dog.getLocalTranslation();
        Vector3f puddlePos = new Vector3f(0.8f, 0, -0.25f);
        dog.getLocalRotation().multLocal(puddlePos);
        puddleGeom.setLocalTranslation(pos.x + puddlePos.x, pos.y + 0.0125f, pos.z + puddlePos.z);
        area.addPoint(puddleGeom.getLocalTranslation());
        puddleGeom.setMaterial(puddleMaterial);
        dog.getParent().attachChild(puddleGeom);
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        if (!animName.equals("mark")) {
            pee.setEnabled(false);
            pee.killAllParticles();
        }
    }
}
