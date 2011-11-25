package org.sf.sasplanetj.util;

public class XYint {
	public int x;
	public int y;

	public XYint() {
	}

	public XYint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public XYint(XYint other) {
		this.x = other.x;
		this.y = other.y;
	}

	/**
	 * Sets new x and y
	 * 
	 * @param x
	 * @param y
	 */
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean equalXY(XYint xy) {
		return this.x == xy.x && this.y == xy.y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String toString() {
		return "[" + x + "," + y + "]";
	}

	/**
	 * Return the difference in coordinates, this-other
	 * 
	 * @param other
	 * @return
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
