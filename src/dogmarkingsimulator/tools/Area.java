package dogmarkingsimulator.tools;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Curve;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import dogmarkingsimulator.DogControl;

/**
 *
 * @author Volker Schuller
 */
public class Area {

    private List<Vector3f> points = new ArrayList<Vector3f>();
    private Vector3f diff = new Vector3f();
    private Vector3f diff2 = new Vector3f();
    private Node node;
    private Geometry curvy;
    private Material lineMaterial;
    private Path2D.Float path;

    public Area(AssetManager assetManager) {
        lineMaterial = new Material(
                assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterial.setColor("Color", new ColorRGBA(
                (float) Math.random(), (float) Math.random(),
                (float) Math.random(), 1));
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void addPoint(Vector3f p) {
        switch (points.size()) {
            case 2:
                if (isClockwise(points.get(0), points.get(1), p)) {
                    points.add(1, p);
                    break;
                }
            case 1:
            case 0:
                points.add(p);
                break;
            default:
                addPointInRightPosition(p);
        }
        if (points.size() > 2) {
            createPath2D();
            displayAreaCurve();
        }
    }

    private void createPath2D() {
        path = new Path2D.Float(Path2D.WIND_EVEN_ODD, points.size());
        Vector3f firstpoint = points.get(0);
        path.moveTo(firstpoint.x, firstpoint.z);
        for (int i = 1; i < points.size(); i++) {
            Vector3f point = points.get(i);
            path.lineTo(point.x, point.z);
        }
        path.lineTo(firstpoint.x, firstpoint.z);
        path.closePath();
    }

    private void displayAreaCurve() {
        Vector3f[] vectors = new Vector3f[points.size() + 1];
        points.toArray(vectors);
        vectors[points.size()] = points.get(0);
        Curve curve = new Curve(new Spline(
                Spline.SplineType.Linear, vectors, 0, true), points.size() + 1);
        if (curvy != null) {
            curvy.removeFromParent();
        }
        curvy = new Geometry("curve", curve);
        curvy.setMaterial(lineMaterial);
        node.attachChild(curvy);
    }

    private boolean isClockwise(Vector3f p1, Vector3f p2, Vector3f newPoint) {
        diff.set(p2);
        diff.subtractLocal(p1);
        diff2.set(newPoint);
        diff2.subtractLocal(p1);
        return diff2.z * diff.x - diff.z * diff2.x > 0;
    }

    private void addPointInRightPosition(Vector3f p) {
        for (Spatial spatial : node.getChildren()) {
            DogControl dog = spatial.getControl(DogControl.class);
            if (dog != null && dog.getArea().getPath() != null
                    && dog.getArea().getPath().contains(p.x, p.z)) {
                return;
            }
        }
        int nearest = getNearestPoint(p);
        int prev = getFirstAffectedPoint(nearest, p);
        int next = getLastAffectedPoint(nearest, p);
        boolean clockwise = isClockwise(
                getPointWrap(nearest - 1), getPointWrap(nearest), p);
        int delfrom = nearest - prev + 1;
        int newpos = delfrom < 0 ? points.size() + delfrom : delfrom;
        int delto = nearest + next;
        for (int i = delfrom; i < delto; i++) {
            if (newpos < 0) {
                points.remove(points.size() - 1);
            } else if (newpos >= points.size()) {
                points.remove(0);
            } else {
                points.remove(newpos);
            }

        }
        if (newpos > points.size()) {
            newpos = points.size();
        }
        if (clockwise || newpos != nearest) {
            addPointWrap(newpos, p);
        } else {
            addPointWrap(newpos + 1, p);
        }
    }

    private int getNearestPoint(Vector3f p) {
        float dist = 100000;
        int min = 0;
        for (int i = 0; i < points.size(); i++) {
            float newdist = points.get(i).distance(p);
            if (newdist < dist) {
                dist = newdist;
                min = i;
            }
        }
        return min;
    }

    private int getFirstAffectedPoint(int nearest, Vector3f p) {
        int prev = 0;
        boolean stop = false;
        for (int i = nearest - 1; i >= 0; i--) {
            if (isClockwise(getPointWrap(i + 1), getPointWrap(i), p)) {
                stop = true;
                break;
            }
            prev++;
        }
        if (!stop) {
            for (int i = points.size() - 1; i > nearest; i--) {
                if (isClockwise(getPointWrap(i + 1), getPointWrap(i), p)) {
                    break;
                }
                prev++;
            }
        }
        return prev;
    }

    private int getLastAffectedPoint(int nearest, Vector3f p) {
        int next = 0;
        boolean stop = false;
        for (int i = nearest + 1; i < points.size(); i++) {
            if (!isClockwise(getPointWrap(i - 1), getPointWrap(i), p)) {
                stop = true;
                break;
            }
            next++;
        }
        if (!stop) {
            for (int i = 0; i < nearest; i++) {
                if (!isClockwise(getPointWrap(i - 1), getPointWrap(i), p)) {
                    break;
                }
                next++;
            }
        }
        return next;
    }

    private Vector3f getPointWrap(int i) {
        if (i < 0) {
            return points.get(points.size() + i);
        }
        if (i >= points.size()) {
            return points.get(i - points.size());
        }
        return points.get(i);
    }

    private void addPointWrap(int i, Vector3f p) {
        if (i < 0) {
            points.add(points.size() + i, p);
        } else if (i >= points.size()) {
            points.add(i - points.size(), p);
        } else {
            points.add(i, p);
        }
    }

    public Path2D.Float getPath() {
        return path;
    }
}
