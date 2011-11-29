package org.sf.sasplanetj.ui;

import java.awt.Rectangle;

public class XYConstraints {

	private final int x;
	private final int y;
	private final int width; // <= 0 means use the components preferred size
	private final int height; // <= 0 means use the components preferred size

	public XYConstraints() {
		this(0, 0, 0, 0);
	}

	public XYConstraints(Rectangle r) {
		this(r.x, r.y, r.width, r.height);
	}

	public XYConstraints(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Returns hash code for this XYConstraints.
	 */
	public int hashCode() {
		return x ^ (y * 37) ^ (width * 43) ^ (height * 47);
	}

	/**
	 * Checks whether two XYConstraints are equal.
	 */
	public boolean equals(Object that) {
		if (that instanceof XYConstraints) {
			XYConstraints other = (XYConstraints) that;
			return other.x == x && other.y == y && other.width == width
					&& other.height == height;
		}
		return false;
	}

	public String toString() {
		return "XYConstraints[" + x + "," + y + "," + width + "," + height
				+ "]";
	}
}
