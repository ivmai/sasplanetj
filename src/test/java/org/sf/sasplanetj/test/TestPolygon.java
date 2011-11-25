package org.sf.sasplanetj.test;

import java.awt.Point;
import java.awt.Polygon;

public class TestPolygon {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Polygon poly = new Polygon();
		poly.addPoint(12, 206);
		poly.addPoint(4, 206);
		poly.addPoint(4, 213);
		poly.addPoint(12, 213);
		poly.addPoint(12, 206);

		System.out.println("Contacins="
				+ poly.getBounds().contains(new Point(8, 209)));
	}

}
