

package TOOL.Classifier;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.text.DecimalFormat;


/**
 * A Location represents where on the field a robot could be given its visual
 * information.  We represent the location as a circle centered at the estimated
 * point, with a radius of uncertainty.
 * The location also includes information as to what bearing we
 * think we have from the objects visible in the scene.
 * @author Nicholas Dunn
 * @date   October 25, 2008
 */

public class Location {


	private int initialX;
	private int initialY;


    private int x;
    private int y;

    // Uncertainty of location; determines how large the circle
    private int radius;
    private FieldOfView viewExtent;

    public static final int DEFAULT_RADIUS = 25;
    public static final int MINIMUM_RADIUS = 5;

    public static final int NUM_PIXELS_TO_EDGE_FOR_RESIZE = 3;


    private static final DecimalFormat degreeFormat = new DecimalFormat("#0.00¡");

    public Location(int x, int y) {
        this(x,y,DEFAULT_RADIUS);
    }

    public Location(int x, int y, int r) {
        this.x = x;
        this.y = y;
        this.radius = r;
        viewExtent = new FieldOfView(this, x, y);

        this.initialX = x;
        this.initialY = y;
    }


    public int getX() { return x; }

    public int getY() { return y; }

    public void lookAt(int x, int y) {
        viewExtent.lookAt(x, y);
    }




    // Resets back to initial starting point
    public void reset() {
    	x = initialX;
    	y = initialY;
    	viewExtent.reset();
    }


    public FieldOfView getView() {
        return viewExtent;
    }


    public void rotate(double theta) {
        viewExtent.rotate(theta);
    }

    public boolean contains(Point p) {
        // If the distance from the given point to the center of the circle
        // is less than the radius, then it's inside of the circle.
        return p.distance(x, y) <= radius;
    }

    public boolean atEdge(Point p) {
        return Math.abs(radius - p.distance(x, y)) < NUM_PIXELS_TO_EDGE_FOR_RESIZE;
    }

    public String toString() {
    	StringBuffer f = new StringBuffer();
    	f.append("Location: (");
    	f.append(x);
    	f.append(", ");
    	f.append(y);
    	f.append(") Radius: ");
    	f.append(radius);
    	f.append(" Angle: ");
    	// Round to two decimal places, add angle sign
    	f.append(degreeFormat.format(viewExtent.getTheta()));
    	return f.toString();
    }

    public void draw(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        // Draw the circle
        g.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);

        Composite oldComposite = g2.getComposite();


        g2.setColor(Color.BLACK);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
						    0.3f));
        g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);

        g2.setComposite(oldComposite);

        viewExtent.draw(g);
    }


    public void grow(int r) {
        this.setRadius(radius + r);
    }

    public void growToPoint(Point p) {
        setRadius((int) p.distance(x, y));
    }

    public void setRadius(int r) {
        if (r >= MINIMUM_RADIUS) {
            radius = r;
        }
    }

    public int getRadius() { return radius; }

    public void move(int dx, int dy, boolean trackTarget) {
        x += dx;
        y += dy;
        viewExtent.move(dx, dy, trackTarget);
    }

    public void moveTo(int x, int y, boolean trackTarget) {
        this.x = x;
        this.y = y;
        viewExtent.moveTo(x,y, trackTarget);
    }

    public void moveTo(Point p, boolean trackTarget) {
        moveTo((int) p.getX(), (int) p.getY(), trackTarget);
    }






}

