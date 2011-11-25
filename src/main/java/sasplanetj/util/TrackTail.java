package sasplanetj.util;

import java.awt.Graphics;
import java.util.Iterator;
import java.util.LinkedList;

import sasplanetj.gps.XY;
import sasplanetj.ui.ColorsAndFonts;

/**
 * Stores lat lng points
 */
public class TrackTail extends LinkedList {

	private static final long serialVersionUID = -5736366814412155585L;

	int MAX_ENTRIES;

	public TrackTail(int maxsize) {
		super();
		this.MAX_ENTRIES = maxsize;
	}

	public void addPoint(XY point) {
		if (this.size() >= MAX_ENTRIES) {
			this.removeFirst();
		}
		this.addLast(point);
	}

	public String getPointsString() {
		String result = "";
		for (Iterator it = this.iterator(); it.hasNext();) {
			result += ((XY) it.next()).toString() + " ";
		}
		return result;
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
			final XYint inmatrix = TilesUtil.coordinateToDisplay(latlng.x,
					latlng.y, Config.zoom, Config.isMapYandex);
			inmatrix.subtract(matrix[0]); // find point in tile matrix
			inmatrix.add(matrix[1]); // point with matrix position drawing
										// offset

			x[i] = inmatrix.x;
			y[i] = inmatrix.y;
			i++;
		}
		dbf.drawPolyline(x, y, x.length);
	}

}
