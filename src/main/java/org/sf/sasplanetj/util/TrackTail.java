package org.sf.sasplanetj.util;

import java.awt.Graphics;
import java.util.Iterator;
import java.util.LinkedList;

import org.sf.sasplanetj.gps.XY;
import org.sf.sasplanetj.ui.ColorsAndFonts;

/**
 * Stores latLng points
 */
public class TrackTail extends LinkedList {

	private static final long serialVersionUID = -5736366814412155585L;

	private final int maxSize;

	public TrackTail(int maxsize) {
		super();
		this.maxSize = maxsize;
	}

	public void addPoint(XY point) {
		if (this.size() >= maxSize) {
			this.removeFirst();
		}
		this.addLast(point);
	}

	public String getPointsString() {
		StringBuffer sb = new StringBuffer();
		for (Iterator it = this.iterator(); it.hasNext();) {
			sb.append(((XY) it.next()).toString());
			sb.append(' ');
		}
		return sb.toString();
	}

	public void draw(Graphics dbf, XYint[] matrix) {
		if (this.size() < 2)
			return;

		dbf.setColor(ColorsAndFonts.clTail);

		int[] x = new int[this.size()];
		int[] y = new int[this.size()];

		int i = 0;
		for (Iterator it = this.iterator(); it.hasNext();) {
			final XY latlng = (XY) it.next(); // map pixel coordinates
			final XYint inmatrix = TilesUtil.coordinateToDisplay(latlng.getX(),
					latlng.getY(), Config.zoom, Config.isMapYandex);
			inmatrix.subtract(matrix[0]); // find point in tile matrix
			inmatrix.add(matrix[1]);
			// point with matrix position drawing offset

			x[i] = inmatrix.x;
			y[i] = inmatrix.y;
			i++;
		}
		dbf.drawPolyline(x, y, x.length);
	}

}
