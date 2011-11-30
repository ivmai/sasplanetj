package org.sf.sasplanetj.util;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

public class WikiMapKML {

	private final String description;

	/* In-tile coordinates */
	private final int x[];
	private final int y[];

	// previously calculated polygon on screen (initialized on draw)
	private Polygon drawnPoly;

	public WikiMapKML(String description, int[] x, int[] y) {
		this.description = description;
		this.x = x;
		this.y = y;
	}

	public String getDescription() {
		return description;
	}

	public void drawPolygon(Graphics dbf, int deltaX, int deltaY) {
		drawnPoly = new Polygon(x, y, x.length);
		drawnPoly.translate(deltaX, deltaY);
		dbf.drawPolygon(drawnPoly);
	}

	public boolean containsPoint(Point point) {
		// Assume the polygon is already drawn
		return drawnPoly.contains(point);
	}
}