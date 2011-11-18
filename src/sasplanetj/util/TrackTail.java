package sasplanetj.util;

import java.awt.Graphics;
import java.util.*;

import sasplanetj.gps.XY;
import sasplanetj.ui.ColorsAndFonts;

/**
 * Stores lat lng points
 */
public class TrackTail extends LinkedList{

	int MAX_ENTRIES;
	
	public TrackTail(int maxsize){
		super();
		this.MAX_ENTRIES = maxsize;
	}
	
	public void addPoint(XY point){
		if (this.size()>=MAX_ENTRIES){
			this.removeFirst();
		}
		this.addLast(point);
	}

	public String getPointsString(){
		String result = "";
		for (Iterator it = this.iterator(); it.hasNext();) {
			result += ((XY)it.next()).toString()+" ";
		}
		return result;
	}
	
	public void draw(Graphics dbf, XYint[] matrix){
		if (this.size()<2) return;
		
		//System.out.println(trackTail.getPointsString());
		//LinkedList<XYint> trackTail = (LinkedList<XYint>) Main.trackTail.clone();
		dbf.setColor(ColorsAndFonts.clTail);
        //XYint prevpoint = null; //with screen coordinates
        
        int[] x = new int[this.size()];
        int[] y = new int[this.size()];

        int i=0;
        for (Iterator it = this.iterator(); it.hasNext();) {
        	final XY latlng = (XY) it.next(); //map pixel coordinates
        	final XYint inmatrix = TilesUtil.coordinateToDisplay(latlng.x, latlng.y, Config.zoom);
        	inmatrix.subtract(matrix[0]); //find point in tile matrix
        	inmatrix.add(matrix[1]); //point with matrix position drawing offset
        	/*
        	if (prevpoint!=null){
				dbf.drawLine(prevpoint.x, prevpoint.y, inmatrix.x, inmatrix.y);
			}
			prevpoint = inmatrix;
			*/
        	x[i] = inmatrix.x;
        	y[i] = inmatrix.y;
        	i++;
		}
        dbf.drawPolyline(x, y, x.length);
	}
	
}
