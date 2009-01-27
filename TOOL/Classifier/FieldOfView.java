package TOOL.Classifier;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * The field of view of a camera represents the anglular extent visible.  For
 * our Nao robot the camera has a FOV of 46.4 degrees in the X coordinate.
 * Humans, by means of comparison, have almost 180 degree vision in that plane.
 * We use this class to represent what portion of the field is visible by a
 * robot at a given point on the field.  Note that we are viewing from overhead
 * so the only measure we care about is the FOV in the X plane, not the Y plane.
 * When viewed overheard, the extent of the field visible by the robot is a
 * triangle.  That is what the view frustum of the camera looks like when
 * projected onto the screen.
 *
 * We keep track of both the robot's position and where its gaze is centered.
 * From this, we can use polar coordinates to calculate what part of the field
 * is visible.
 *
 * The end use of this class is to allow a user to place where the robot must be
 * on the field, and where its gaze must be centered, given a picture of what
 * the robot sees.  This will allow us to determine the range of distances to
 * various objects on the field and compare that with what the visual system
 * estimates for said objects.
 * @author Nicholas Dunn
 * @date   October 24, 2008
 */

public class FieldOfView {

    // Degrees above the x axis the center of the field of view is raised
    // (positive degrees)
    private double theta;

    private int initialX;
    private int initialY;

    // The origin of the ray
    private int x;
    private int y;

    // The location to which the ray is drawn
    private int targetX;
    private int targetY;

    private int target2X;
    private int target2Y;

    private int target3X;
    private int target3Y;

    // Are we pivoting around the second target?
    private boolean target1Pivot;

    // Are we pivoting around the second target?
    private boolean target2Pivot;

    // Are we pivoting around the third target?
    private boolean target3Pivot;

    // Angular extent (Camera
    private double cameraFOV_X;

    private double cameraFOV_Y;

    // Horizontal field of view for the cameras
    public static final double NAO_FOV_X_DEG = 46.4;
    public static final double AIBO_FOV_X_DEG = 56.9;

    // Vertical field of view for the cameras
    public static final double NAO_FOV_Y_DEG = 34.8;
    public static final double AIBO_FOV_Y_DEG = 45.2;


    public static final int TARGET_RADIUS = 5;

    public static final int DEFAULT_X_OFFSET = 0;
    public static final int DEFAULT_Y_OFFSET = -100;



    private Location loc;




    public Point getTarget() {
        return new Point(targetX, targetY);
    }

    public Point getTarget2() {
        return new Point(target2X, target2Y);
    }

    public Point getTarget3() {
        return new Point(target3X, target3Y);
    }


    // The Nao cannot see the field right at its feet if it's
    // looking straight ahead.  This calculates how many
    // cm away the first bit of ground that's visible is
    public int calculateDistanceToField() {
    	// CHANGE ME:  where exactly in the camera?
    	final double HEIGHT = 63;

    	// Assume the camera is looking dead straight ahead

    	double alpha = (180.0 - cameraFOV_Y) / 2;

    	double dist = HEIGHT * Math.atan(Math.toRadians(alpha));

    	return (int) dist;

    }


    public FieldOfView(Location loc, int x, int y) {
        this.loc = loc;
        this.x = x;
        this.y = y;
        cameraFOV_X = NAO_FOV_X_DEG;
        cameraFOV_Y = NAO_FOV_Y_DEG;

        initialX = x;
        initialY = y;

        lookAt(x, y + DEFAULT_Y_OFFSET);
    }


    public void reset() {
    	x = initialX;
    	y = initialY;
    	lookAt(x, y + DEFAULT_Y_OFFSET);
    }


    public void lookAt(Point p) {
        lookAt((int) p.getX(), (int) p.getY());
    }

    // Manipulate the angle so to look at the Point in question
    public void lookAt(int x, int y) {
        targetX = x;
        targetY = y;
        double radians = Math.atan2(y - this.y, x - this.x);
        setTheta(Math.toDegrees(radians));


        // How long is it from our center of circle to target point?
        int centralLength = (int) Point2D.distance(this.x, this.y, targetX, targetY);

        // Cos = adjacent over hypotenuse, flip it around to find the hypotenuse
        // length
        int hypotLength = (int) (centralLength / Math.cos(Math.toRadians(cameraFOV_X/2)));

        Point bottomLineOffset = polarToCartesian(hypotLength, theta - cameraFOV_X/2);
        Point topLineOffset = polarToCartesian(hypotLength, theta + cameraFOV_X/2);

        target2X = (int) (this.x + topLineOffset.getX());
        target2Y = (int) (this.y + topLineOffset.getY());

        target3X = (int) (this.x + bottomLineOffset.getX());
        target3Y = (int) (this.y + bottomLineOffset.getY());

    }


    public boolean target1Contains(Point p) {
        return p.distance(targetX, targetY) <= TARGET_RADIUS;

    }

    public boolean target2Contains(Point p) {
        return p.distance(target2X, target2Y) <= TARGET_RADIUS;

    }

    public boolean target3Contains(Point p) {
        return p.distance(target3X, target3Y) <= TARGET_RADIUS;
    }

    public void setTarget1Pivot(boolean b) {
        target1Pivot = b;
        if (b) {
            target2Pivot = false;
            target3Pivot = false;
        }
    }


    public void setTarget2Pivot(boolean b) {
        target1Pivot = !b;
        target2Pivot = b;
        target3Pivot = false;
    }

    public void setTarget3Pivot(boolean b) {
        // They can only pivot around one point at once; make sure that this
        // is the case
        target1Pivot = !b;
        target2Pivot = false;
        target3Pivot = b;
   }

    public boolean getTarget1Pivot() {
        return target1Pivot;
    }

    public boolean getTarget2Pivot() {
        return target2Pivot;
    }

    public boolean getTarget3Pivot() {
        return target3Pivot;
    }





    /**
     *
     * @param x
     * @param y
     * @param centralPivot if true, pivot around targetX, targetY; else pivot
     *                     around target2X, target2Y
     * @return
     */
    public Point setTarget2(int x, int y, boolean centralPivot) {
        target2X = x;
        target2Y = y;

        if (centralPivot) {
            target3X = targetX + (targetX - target2X);
            target3Y = targetY + (targetY - target2Y);
        }
        else {
            targetX = (target2X + target3X) / 2;
            targetY = (target2Y + target3Y) / 2;
        }

        // We keep traget3X and Y to be the same

        // See http://mathworld.wolfram.com/images/eps-gif/IsoscelesTriangle_800.gif
        // Calculate how far apart the two end targets are; this will allow us
        // to determine the length of the equal sides of the isosceles triangle
        double base = Point2D.distance(target2X, target2Y, target3X, target3Y);

        // sin FOV/2 = base/2 / sideLength
        double sideLength = base / (2 * Math.sin(Math.toRadians(cameraFOV_X/2)));


        // Imagine two circles radiating at

        // Find out where the central location must be for the two targets to be
        // where they are
        Point intersection1 = new Point();
        Point intersection2 = new Point();

        circleCircleIntersection(target2X, target2Y, sideLength,
                                target3X, target3Y, sideLength,
                                intersection1, intersection2);

        fixTheta();


        if (intersection1.distance(this.x, this.y) < intersection2.distance(this.x, this.y)) {
            return intersection1;
        }
        else {
            return intersection2;
        }


    }




    /**
     *
     * @param x
     * @param y
     * @param centralPivot if true, pivot around targetX, targetY; else pivot
     *                     around target2X, target2Y
     * @return
     */
    public Point setTarget3(int x, int y, boolean centralPivot) {
        target3X = x;
        target3Y = y;

        if (centralPivot) {
            target2X = targetX + (targetX - target3X);
            target2Y = targetY + (targetY - target3Y);
        }
        else {
            targetX = (target2X + target3X) / 2;
            targetY = (target2Y + target3Y) / 2;
        }


        double base = Point2D.distance(target2X, target2Y, target3X, target3Y);

        // sin FOV/2 = base/2 / sideLength
        double sideLength = base / (2 * Math.sin(Math.toRadians(cameraFOV_X/2)));


        // Imagine two circles radiating at

        // Find out where the central location must be for the two targets to be
        // where they are
        Point intersection1 = new Point();
        Point intersection2 = new Point();

        circleCircleIntersection(target2X, target2Y, sideLength,
                                target3X, target3Y, sideLength,
                                intersection1, intersection2);


        fixTheta();

        if (intersection1.distance(this.x, this.y) < intersection2.distance(this.x, this.y)) {
            return intersection1;
        }
        else {
            return intersection2;
        }



    }



    public Point getOriginFromTargets(Point target1, Point target2) {
        return getOriginFromTargets((int) target1.getX(), (int) target1.getY(),
                                    (int) target2.getX(), (int) target2.getY());
    }

    /**
     * Given two locations at the end ("base") of the triangle segment, returns where
     * the origin ("tip") must be.
     *
     * @param x0 The x coordinate of the first target
     * @param y0 The y coordinate of the first target
     * @param x1 The x coordinate of the second target
     * @param y1 The y coordinate of the second target
     * @return The point where the top of the triangle must be
     */
    public Point getOriginFromTargets(int x0, int y0, int x1, int y1) {
        return null;
    }


    public void fixTheta() {
        setTheta(Math.toDegrees(Math.atan2(targetY - y, targetX - x)));
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getTheta() {
        return theta;
    }

    public void rotate(double alpha) {
        theta += alpha;
    }

    public void move(int dx, int dy, boolean trackTarget) {
        this.x += dx;
        this.y += dy;
        if (trackTarget) {
            lookAt(targetX, targetY);
        }
        else {
            targetX += dx;
            targetY += dy;
            target2X += dx;
            target2Y += dy;
            target3X += dx;
            target3Y += dy;
        }
    }

    public void moveTo(int x, int y, boolean trackTarget) {
        this.x = x;
        this.y = y;
        if (trackTarget) {
            lookAt(targetX, targetY);
        }
    }







    // To draw the field of view we draw the triangular shaped region we see
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Are we pivoting around a point?
        int pivotX = 0;
        int pivotY = 0;
        boolean pivoting = true;
        if (target1Pivot) {
            pivotX = targetX;
            pivotY = targetY;
        }
        else if (target2Pivot) {
            pivotX = target2X;
            pivotY = target2Y;
        }
        else if (target3Pivot) {
            pivotX = target3X;
            pivotY = target3Y;
        }
        else {
            pivoting = false;
        }

        if (pivoting) {
            // Draw a circle around the target
            g2.drawOval(pivotX - 2 * TARGET_RADIUS,
                        pivotY - 2 * TARGET_RADIUS,
                        4 * TARGET_RADIUS, 4 * TARGET_RADIUS);

            // Draw an arrow head at left side of top half
            GeneralPath arrow1 = new GeneralPath();
            arrow1.moveTo(pivotX - 2 * TARGET_RADIUS, pivotY);
            arrow1.lineTo(pivotX - 2 * TARGET_RADIUS - 5, pivotY - 5);
            arrow1.moveTo(pivotX - 2 * TARGET_RADIUS, pivotY);
            arrow1.lineTo(pivotX - 2 * TARGET_RADIUS + 5, pivotY - 5);

            GeneralPath arrow2 = new GeneralPath();
            arrow1.moveTo(pivotX + 2 * TARGET_RADIUS, pivotY);
            arrow1.lineTo(pivotX + 2 * TARGET_RADIUS - 5, pivotY + 5);
            arrow1.moveTo(pivotX + 2 * TARGET_RADIUS, pivotY);
            arrow1.lineTo(pivotX + 2 * TARGET_RADIUS + 5, pivotY + 5);

            g2.draw(arrow1);
            g2.draw(arrow2);

        }

        // Draw the portion of the view that is not visible to the robot
        g2.setColor(Color.RED);
        Polygon invisible = new Polygon();
        invisible.addPoint(x,y);
        int distToField = calculateDistanceToField();

        Point close1 = polarToCartesian(distToField, theta - cameraFOV_X / 2);
        Point close2 = polarToCartesian(distToField, theta + cameraFOV_X / 2);

        invisible.addPoint(x + (int) close1.getX(), y + (int) close1.getY());
        invisible.addPoint(x + (int) close2.getX(), y + (int) close2.getY());

        //g2.fill(invisible);


        g2.setColor(Color.BLUE);




        Polygon p = new Polygon();
        //p.addPoint(x,y);

        p.addPoint(x + (int) close1.getX(), y + (int) close1.getY());
        p.addPoint(x + (int) close2.getX(), y + (int) close2.getY());
        p.addPoint(target2X,
                   target2Y);
        p.addPoint(target3X,
                   target3Y);




        Polygon outsideRange = new Polygon();

        // Calculate where the endpoints would lie
        Point bottomLineOffset = polarToCartesian(1000, theta - cameraFOV_X/2);
        Point topLineOffset = polarToCartesian(1000, theta + cameraFOV_X/2);


        outsideRange.addPoint(target2X, target2Y);
        outsideRange.addPoint(x + (int) topLineOffset.getX(), y + (int) topLineOffset.getY());

        outsideRange.addPoint(x + (int) bottomLineOffset.getX(), y + (int) bottomLineOffset.getY());
        outsideRange.addPoint(target3X, target3Y);



        Composite oldComposite = g2.getComposite();


        g2.setColor(Color.BLUE);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
						    0.3f));

        g2.fill(p);
        g2.setColor(Color.RED);
        g2.fill(outsideRange);


        g2.setColor(Color.BLACK);
        g2.setComposite(oldComposite);



        Stroke oldStroke = g2.getStroke();
        float dash[] = {10.0f};
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));

        // Draw the middle part
        g2.drawLine(x, y,
                targetX, targetY);

        g2.setStroke(oldStroke);


     // Draw the 3 targets
        g2.setColor(Color.RED);
        g2.fillOval(targetX - TARGET_RADIUS,
                    targetY - TARGET_RADIUS,
                    2 * TARGET_RADIUS,
                    2 * TARGET_RADIUS);
        g2.setColor(Color.BLACK);
        g2.drawOval(targetX - TARGET_RADIUS,
                targetY - TARGET_RADIUS,
                2 * TARGET_RADIUS,
                2 * TARGET_RADIUS);



        g2.setColor(Color.GREEN);
        g2.fillOval(target2X - TARGET_RADIUS,
                    target2Y - TARGET_RADIUS,
                    2 * TARGET_RADIUS,
                    2 * TARGET_RADIUS);
        g2.setColor(Color.BLACK);
        g2.drawOval(target2X - TARGET_RADIUS,
                target2Y - TARGET_RADIUS,
                2 * TARGET_RADIUS,
                2 * TARGET_RADIUS);


        g2.setColor(Color.BLUE);
        g2.fillOval(target3X - TARGET_RADIUS,
                    target3Y - TARGET_RADIUS,
                    2 * TARGET_RADIUS,
                    2 * TARGET_RADIUS);
        g2.setColor(Color.BLACK);
        g2.drawOval(target3X - TARGET_RADIUS,
        			target3Y - TARGET_RADIUS,
        			2 * TARGET_RADIUS,
        			2 * TARGET_RADIUS);


    }

    public void getPossibleAngles(Point p) {
    	getPossibleAngles((int) p.getX(), (int) p.getY());
    }


    /* Given a target object's location on the field, calculate the actual angle
     * it should be given the current orientation, as well as the min and max
     * angles it could be. These min and max are calculated by assuming that the
     * robot is actually at the edge of the radius of uncertainty rather than
     * at the center.
     *
     */
    public void getPossibleAngles(int x2, int y2) {
        double alpha = theta - Math.toDegrees(Math.atan2(y2 - y, x2 - x));

        // Find the points on the circle that are orthogonal to the ray between the center of the circle and (targetX, targetY)

        double alpha1 = theta - 90;
        double alpha2 = theta + 90;

        // These hold the offsets necessary to shift the center of the circle by to reach the
        // orthogonal points
        Point p1 = polarToCartesian(loc.getRadius(), alpha1);
        Point p2 = polarToCartesian(loc.getRadius(), alpha2);

        // Now p1 and p2 refer to points on the radius of the circle
        p1.translate(this.x, this.y);
        p2.translate(this.x, this.y);

        // At what angle is the central target from p1?
        double theta1 = Math.toDegrees(Math.atan2(targetY - (int) p1.getY(), targetX - (int) p1.getX()));

        // At what angle is the new point from p1?
        double beta1 = Math.toDegrees(Math.atan2(y2 - (int) p1.getY(), x2 - (int) p1.getX()));

        // At what angle is the central target from p2?
        double theta2 = Math.toDegrees(Math.atan2(targetY - (int) p2.getY(), targetX - (int) p2.getX()));

        // At what angle is the new point from p2?
        double beta2 = Math.toDegrees(Math.atan2(y2 - (int) p2.getY(), x2 - (int) p2.getX()));


    }



    /**
     *
     * @param r length of the line
     * @param theta number of degrees raised above x axis
     * @return a pair (x, y)
     */
    public Point polarToCartesian(double r, double theta) {
        double thetaR = Math.toRadians(theta);

        // cos theta = x / r
        int x_ = (int) (r * Math.cos(thetaR));

        // sin theta = y / r
        int y_ = (int) (r * Math.sin(thetaR));

        return new Point(x_, y_);
    }



    /**
     * Computes the point(s) of intersection of two circles.
     * Sets the intersection1 to be the location of the first point of
     * intersection, and intersection2 to be the location of the second
     * point of intersection
     * Adapted from C code available at
     * http://ozviz.wasp.uwa.edu.au/~pbourke/geometry/2circle/tvoght.c
     *
     * @param x0 x coord of first circle
     * @param y0 y coord of first circle
     * @param r0 radius of first circle
     * @param x1 x coord of second circle
     * @param y1 y coord of second circle
     * @param r1 radius of second circle
     * @param intersection1 the Point object in which to place the first
     *        intersection location
     * @param intersection2 the Point object in which to place the second
     *        intersection location
     * @return boolean true if the circles intersect, false otherwise
     */
    public boolean circleCircleIntersection(double x0, double y0, double r0,
                                         double x1, double y1, double r1,
                                         Point intersection1, Point intersection2)
    {
        double a, dx, dy, d, h, rx, ry;
        double x2, y2;

        dx = x1 - x0;
        dy = y1 - y0;


        // What is the straight line distance between the centers
        d = Math.sqrt(dx * dx + dy * dy);

        // No point of intersection; circles are completely outside of each other
        if (d > r0 + r1) {
            return false;
        }

        // No solution, one circle contained in the other
        if (d < Math.abs(r0 - r1)) {
            return false;
        }

        /* 'point 2' is the point where the line through the circle
        * intersection points crosses the line between the circle
        * centers.
        */

        /* Determine the distance from point 0 to point 2. */
        a = ((r0*r0) - (r1*r1) + (d*d)) / (2.0 * d) ;

        /* Determine the coordinates of point 2. */
        x2 = x0 + (dx * a/d);
        y2 = y0 + (dy * a/d);

        /* Determine the distance from point 2 to either of the
        * intersection points.
        */
        h = Math.sqrt((r0*r0) - (a*a));

        /* Now determine the offsets of the intersection points from
        * point 2.
        */
        rx = -dy * (h/d);
        ry = dx * (h/d);

        /* Determine the absolute intersection points. */

        intersection1.setLocation(x2 + rx, y2 + ry);

        intersection2.setLocation(x2 - rx, y2 - ry);

        return true;

    }




}


