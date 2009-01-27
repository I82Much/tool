package TOOL.GUI;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;

import java.awt.Color;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.Point;


/**
 * @author Nicholas Dunn
 * @date   December 8, 2008
 */
public class Polygon extends AbstractDefinableShape {

    // We only draw the polygon when it's completed; until then we draw the
    // (open) path so far
    private java.awt.geom.GeneralPath gp;
    private java.awt.Polygon polygon;
    private boolean finished;

    public void draw(Graphics g) {
        if (polygon == null) { return; }
        saveOldSettings(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(getStroke());
        g2.setColor(getOutlineColor());

        if (finished) {
            if (isFilled()) {
                g2.fill(polygon);
            }
            else {
                g2.draw(polygon);
            }
        }
        else {
            g2.draw(gp);
        }
        restoreOldSettings(g);
    }

    public void mousePressed(MouseEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        if (polygon == null) {
            polygon = new java.awt.Polygon();
            gp = new java.awt.geom.GeneralPath();
        }
        if (polygon.npoints == 0) {
            gp.moveTo(x, y);
        }
        polygon.addPoint(x, y);
        gp.lineTo(x, y);

        if (e.isPopupTrigger()) {
            System.out.println("Finished.");
            finished = true;
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            System.out.println("Finished.");
            finished = true;
        }

    }

    public void reset() {
        if (polygon != null) {
            polygon.reset();
            gp.reset();
            finished = false;
        }
    }

    public void mouseDragged(MouseEvent e) {}


    public Point[] getSignificantPoints() {
        return null;
    }

    public boolean isFinished() { return finished; }

    public java.awt.Rectangle getRefreshArea() { return polygon.getBounds(); }

    public Shape getShape() { return polygon; }


}