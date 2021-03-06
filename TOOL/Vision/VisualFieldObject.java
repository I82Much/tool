package TOOL.Vision;

/**
 * class VisualFieldObject
 *
 * it's not exactly clear how the stuff in the robot code are going to change
 * as related to vision, so this should be only a termporary class/ solution
 * 
 * @author  Octavian Neamtu 2009
**/


public class VisualFieldObject {

    //some constants I yanked out of the Nao code -- temporary
    public static final int BLUE_GOAL_LEFT_POST = 30,
	BLUE_GOAL_RIGHT_POST = 31,
	YELLOW_GOAL_LEFT_POST = 32,
	YELLOW_GOAL_RIGHT_POST = 33,
	BLUE_GOAL_POST = 34,
	YELLOW_GOAL_POST = 35,
	BLUE_GOAL_BACKSTOP = 36,
	YELLOW_GOAL_BACKSTOP = 37;

    private int id;
    private double w, h;
    private int ltx, lty, rtx, rty, 
	lbx, lby, rbx, rby,
	cx, cy;

    //constructor
    public VisualFieldObject() {
    }

    public VisualFieldObject(int id){
        this.id = id;
    }

    //setters
    public void setID(int id) { this.id = id;}
    public void setWidth(double w) { this.w = w;}
    public void setHeight(double h) { this.h = h;}
    public void setLeftTopX(int ltx) { this.ltx = ltx;}
    public void setLeftTopY(int lty) { this.lty = lty;}
    public void setRightTopX(int rtx) { this.rtx = rtx;}
    public void setRightTopY(int rty) { this.rty = rty;}
    public void setLeftBottomX(int lbx) { this.lbx = lbx;}
    public void setLeftBottomY(int lby) { this.lby = lby;}
    public void setRightBottomX(int rbx) {this.rbx = rbx;}
    public void setRightBottomY(int rby) {this.rby = rby;}
    public void setCenterX(int cx) {this.cx = cx;}
    public void setCenterY(int cy) {this.cy = cy;}

    //getters
    public int getID() {return id;}
    public double getWidth() {return w;}
    public double getHeight() {return h;}
    public int getLeftTopX() {return ltx;}
    public int getLeftTopY() {return lty;}
    public int getRightTopX() {return rtx;}
    public int getRightTopY() {return rty;}
    public int getLeftBottomX() {return lbx;}
    public int getLeftBottomY() {return lby;}
    public int getRightBottomX() {return rbx;}
    public int getRightBottomY() {return rby;}
    public int getCenterX() {return cx;}
    public int getCenterY() {return cy;}

}
