package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author Volker Schuller
 */
public class DogNpcControl extends AbstractControl {

    private AnimChannel channel;
    DogControl dogControl;

    @Override
    public void setSpatial(Spatial spatial) {
        Node dognode = (Node) ((Node) spatial).getChild("Dog");
        AnimControl anim = dognode.getControl(AnimControl.class);
        channel = anim.getChannel(0);
        dogControl = spatial.getControl(DogControl.class);
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        DogNpcControl newcontrol = new DogNpcControl();
        newcontrol.setSpatial(spatial);
        return newcontrol;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (Math.random() < 0.001) {
            dogControl.mark();
        } else if (channel.getAnimationName().equals("mark")) {
        } else if (dogControl.isForward()) {
            if (Math.random() < 0.01) {
                dogControl.setForward(false);
            }
        } else if (Math.random() < 0.01) {
            dogControl.setForward(true);
        }
        if (dogControl.isLeftRotate()) {
            if (Math.random() < 0.1) {
                dogControl.setLeftRotate(false);
            }
        } else if (dogControl.isRightRotate()) {
            if (Math.random() < 0.1) {
                dogControl.setRightRotate(false);
            }
        } else if (Math.random() < 0.005) {
            dogControl.setLeftRotate(true);
        } else if (Math.random() < 0.005) {
            dogControl.setRightRotate(true);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
