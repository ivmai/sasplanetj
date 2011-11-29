package org.sf.sasplanetj.util;

public class XYint {

	public int x;

	public int y;

	public XYint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Sets new x and y
	 */
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean equalXY(XYint xy) {
		return x == xy.x && y == xy.y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String toString() {
		return "[" + x + "," + y + "]";
	}

	/**
	 * Return the difference in coordinates, this-other
	 */
	public XYint getDifference(XYint other) {
		return new XYint(this.x - other.x, this.y - other.y);
	}

	/**
	 * Adds coordinates of other point
	 */
	public void add(XYint other) {
		setLocation(this.x + other.x, this.y + other.y);
	}

	public void subtract(XYint other) {
		setLocation(this.x - other.x, this.y - other.y);
	}

	public void multiply(double m) {
		setLocation((int) (this.x * m), (int) (this.y * m));
	}

}
