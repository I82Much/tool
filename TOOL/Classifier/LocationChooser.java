
package TOOL.Classifier;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;

import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;


import TOOL.Data.Field;
import TOOL.Data.NaoField2008;



/**
 * Class lets user place where the robot is on the field.
 * @author Nick Dunn
 * @date   October 24, 2008
 */
public class LocationChooser extends JPanel implements KeyListener,
                                                       MouseMotionListener, MouseListener, Observer {

    private Location loc;

    private static final int ROTATE_INCREMENT = 2;

    public static final int X_BORDER = 20;
    public static final int Y_BORDER = 20;

    private boolean atEdge;
    private boolean moving;
    private boolean resizing;

    private boolean dragTarget1;
    private boolean dragTarget2;
    private boolean dragTarget3;

    private Point lastPoint;


    public enum Axis {
        X_AXIS,
        Y_AXIS,
        BOTH
    }

    private Field field;
    private BufferedImage fieldGraphics;

    private ClassifierModel model;

    public LocationChooser(ClassifierModel model) {
        super();
        this.model = model;
        model.addObserver(this);

        //addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);

        field = new NaoField2008();

        fieldGraphics = field.getFieldImage();

        loc = model.getLocation();
    }

    private void reset() {
        atEdge = false;
        moving = false;
        resizing = false;

        dragTarget1 = false;
        dragTarget2 = false;
        dragTarget3 = false;

        lastPoint = null;
    }


    /**
     * Fulfills the Observer interface.  Called whenever our model changes and
     * we must refresh.
     */
    public void update(Observable o, Object arg) {
        // Update the currently displayed location to match that of the model
        loc = model.getLocation();
        reset();
        repaint();
    }

    public Dimension getPreferredSize() {
        return new Dimension((int) fieldGraphics.getWidth() + 2 * X_BORDER,
        					(int) fieldGraphics.getHeight() + 2 * Y_BORDER);
    }

    public Dimension getMinimumSize() {
        return new Dimension((int) fieldGraphics.getWidth() + 2 * X_BORDER,
        					(int) fieldGraphics.getHeight() + 2 * Y_BORDER);
    }

    public void paintComponent(Graphics g) {
        // paint field
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // Leave some extra space around the borders
        g2.drawImage(fieldGraphics, null, X_BORDER, Y_BORDER);


        if (loc != null) {
            // paint the location
            loc.draw(g);

            g2.drawString(loc.toString(), 0, Y_BORDER / 2);
        }
    }

 // Mirror across the x and y axis
    public Location getMirrorImage(Location oldLoc, Axis axis) {
        // TODO: fix this hack
        int width = 600;
        int height = 480;

        int newX = oldLoc.getX();
        int newY = oldLoc.getY();

        // Flip about the x axis, in our case half way across the field
        if (axis == Axis.X_AXIS || axis == Axis.BOTH) {
            newX = width/2 + (width/2 - oldLoc.getX());
        }

        // Flip about the y axis, in our case half way down the field
        if (axis == Axis.Y_AXIS || axis == Axis.BOTH) {
            newY = height/2 + (height/2 - oldLoc.getY());
        }

        Location newLoc = new Location(newX, newY);
        return newLoc;

    }

    public void keyReleased(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_J) {
            loc.rotate(ROTATE_INCREMENT);
        }
        else if (e.getKeyCode() == KeyEvent.VK_K) {
            loc.rotate(-ROTATE_INCREMENT);
        }


        else if (e.getKeyCode() == KeyEvent.VK_R) {
        	loc.reset();
        	repaint();
        }

        // Move up
        else if (e.getKeyCode() == KeyEvent.VK_UP) {
            loc.move(0, -10, e.isShiftDown());
        }
        // Move down
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            loc.move(0, 10, e.isShiftDown());
        }
        // Move left
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            loc.move(-10, 0, e.isShiftDown());

        }
        // Move right
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            loc.move(10, 0, e.isShiftDown());
        }

        // Grow
        else if (e.getKeyCode() == KeyEvent.VK_A) {
            loc.grow(10);
        }

        else if (e.getKeyCode() == KeyEvent.VK_S) {
            loc.grow(-10);
        }


        repaint();


    }

    public void keyTyped(KeyEvent e) {}


    public void mouseDragged(MouseEvent e) {
         if (moving) {
            int dx = e.getX() - (int) lastPoint.getX();
            int dy = e.getY() - (int) lastPoint.getY();
            lastPoint = e.getPoint();
            loc.move(dx, dy, !e.isShiftDown());
            loc.getView().setTarget1Pivot(!e.isShiftDown());
        }
        else if (resizing) {
            lastPoint = e.getPoint();
            loc.growToPoint(e.getPoint());
        }
        else if (dragTarget1) {
            loc.lookAt(e.getX(), e.getY());
        }
        else if (dragTarget2) {
            Point p = loc.getView().setTarget2(e.getX(), e.getY(), !e.isShiftDown());

            loc.moveTo((int) p.getX(), (int) p.getY(), false);

            loc.getView().setTarget3Pivot(e.isShiftDown());
        }
        else if (dragTarget3) {

            Point p = loc.getView().setTarget3(e.getX(), e.getY(), !e.isShiftDown());
            loc.moveTo((int) p.getX(), (int) p.getY(), false);

            loc.getView().setTarget2Pivot(e.isShiftDown());
        }
        else {
            // Do nothing


        }
        repaint();
    }

    public void mouseMoved(MouseEvent e) {

        if (loc.atEdge(e.getPoint())) {
            atEdge = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
        else if (loc.contains(e.getPoint())) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            atEdge = false;

            loc.getView().setTarget1Pivot(!e.isShiftDown());
            repaint();

        }

        else if (loc.getView().target1Contains(e.getPoint())) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        else if (loc.getView().target2Contains(e.getPoint())) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (e.isShiftDown()) {
                loc.getView().setTarget3Pivot(true);
            }
            else {
                loc.getView().setTarget1Pivot(true);
            }

            repaint();
        }

        else if (loc.getView().target3Contains(e.getPoint())) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (e.isShiftDown()) {

                loc.getView().setTarget2Pivot(true);
            }
            else {
                loc.getView().setTarget1Pivot(true);
            }
            repaint();
        }

        else {
            atEdge = false;
            setCursor(Cursor.getDefaultCursor());

            if (loc.getView().getTarget1Pivot()) {
                loc.getView().setTarget1Pivot(false);
                repaint();
            }

            // Target 2 was entered earlier
            if (loc.getView().getTarget2Pivot()) {
                loc.getView().setTarget2Pivot(false);
                repaint();
            }
            else if (loc.getView().getTarget3Pivot()) {
                loc.getView().setTarget3Pivot(false);
                repaint();
            }


        }

    }

    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        if (atEdge) {
            resizing = true;
            lastPoint = p;
        }
        else if (loc.contains(p)) {
            moving = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            lastPoint = p;
        }

        else {
            FieldOfView v = loc.getView();

            if (v.target1Contains(p)) {
                dragTarget1 = true;
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
            else if (v.target2Contains(p)) {
                dragTarget2 = true;
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
            else if (v.target3Contains(p)) {
                dragTarget3 = true;
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            // Did not click in anything
            else {
                loc.reset();
            }

            repaint();
        }
    }

    public void mouseClicked(MouseEvent e ) {}


    public void mouseReleased(MouseEvent e) {
        moving = false;
        resizing = false;
        dragTarget1 = dragTarget2 = dragTarget3 = false;

        if (inTarget(e.getPoint())) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public boolean inTarget(Point p) {
        return loc.contains(p) ||
               loc.getView().target1Contains(p) ||
               loc.getView().target2Contains(p) ||
               loc.getView().target3Contains(p);

    }

    public void mouseExited(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}


}


