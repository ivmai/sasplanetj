package sasplanetj.util;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import sasplanetj.ui.ColorsAndFonts;

public class Wikimapia {
	public static Cache kmlCache; //Cache<String, ArrayList<KML>>

	public static final String mapDir = "Wiki";

	/**
	 * last drawn KMLs
	 */
	public static ArrayList drawnKmls = new ArrayList(); //ArrayList<KML>


    public static String getCachePath(int x, int y, int zoom){
    	///cache/Wiki/z17/37/x38343/21/y22152.kml
    	return Config.cachePath+StringUtil.fileSep+mapDir+StringUtil.fileSep+"z"+Config.zoom+StringUtil.fileSep+(x/1024)+StringUtil.fileSep+"x"+x+StringUtil.fileSep+(y/1024)+StringUtil.fileSep+"y"+y+".kml";
    }

    /**
     *
     * @param tileXY
     * @param zoom
     * @return ArrayList<KML>
     */
	public static ArrayList getTileKML(XYint tileXY, int zoom) {
		String filename = getCachePath(tileXY.x, tileXY.y, zoom);

		int cachei = kmlCache.containsKey(filename);
		if (cachei>-1) return (ArrayList) kmlCache.get(cachei);

		String kmlstr = loadKML(filename);
		ArrayList kmls = parse(kmlstr, zoom);

		kmlCache.put(filename, kmls);
		//System.out.println("Caching "+filename);

		return kmls;
	}

	/**
	 *
	 * @param kmlstr
	 * @param zoom
	 * @return ArrayList<KML>
	 * @throws NumberFormatException
	 */
	private static ArrayList parse(String kmlstr, int zoom) throws NumberFormatException {
		if (kmlstr==null) return null;

		ArrayList kmls = new ArrayList(1);
		int descrPos = 0;
		while ( (descrPos = kmlstr.indexOf("<description>", descrPos+1))!=-1){
			KML kml = new KML();
			int descrTextStart = kmlstr.indexOf("<![CDATA[",  descrPos+1) + "<![CDATA[".length();
			int descrTextEnd =  kmlstr.indexOf("<br>",  descrTextStart+1); //]]>
			kml.description = kmlstr.substring(descrTextStart, descrTextEnd).trim();

			int coordinatesStart = kmlstr.indexOf("<coordinates>",  descrTextEnd) + "<coordinates>".length();
			int coordinatesEnd = kmlstr.indexOf("</coordinates>",  coordinatesStart+1);
			String coordsStr = kmlstr.substring(coordinatesStart, coordinatesEnd).trim();
			String[] coordsStrs = StringUtil.split(coordsStr, "\n");
			kml.x = new int[coordsStrs.length];
			kml.y = new int[coordsStrs.length];
			for (int i = 0; i < coordsStrs.length; i++) {
				int coma1 = coordsStrs[i].indexOf(',', 1);
				int coma2 = coordsStrs[i].indexOf(',', coma1+1);
				//lat lng order is twisted
				double lng = Double.valueOf( coordsStrs[i].substring(0, coma1) ).doubleValue();
				double lat = Double.valueOf( coordsStrs[i].substring(coma1+1, coma2) ).doubleValue();
	        	final XYint intile = TilesUtil.coordinateToDisplay(lat, lng, zoom);
	        	kml.x[i] = intile.x;
	        	kml.y[i] = intile.y;
			}
			//kml.poly = new Polygon(x, y, x.length);
			kmls.add(kml);
		}
		System.out.println("Wikimapia: Parsed "+kmls.size()+" items");
		return kmls.size()>0 ? kmls : null;
	}


	public static class KML{
		public String description;
		//Polygon poly;
		/*Intile coordinates*/
		public int x[];
		public int y[];
		//previously calculated polygon on screen
		public Polygon drawnPoly;

		public String strip(){
			return StringUtil.replace(description, "&amp;quot;", "\"");
		}
	}



	public static String loadKML(String filename) {
		/*Check if there are any zips instead of directories*/
		String[] zipnames = TilesUtil.checkZipFileExistance(filename);
		if (zipnames != null) {
			System.out.print("KML unzip: " + zipnames[0] + ": " + zipnames[1]);
			Date start = new Date();
			byte[] binary = Zip.getZipFile(zipnames[0], zipnames[1]);
			System.out.println(" " + (new Date().getTime() - start.getTime()) + "ms");
			if (binary == null){
				System.out.println(" FAILED \n");
				return null;
			}
			try {
				return new String(binary, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				System.out.println("UnsupportedEncodingException");
				return new String(binary);
			}
		}

		return loadKML_FS(filename);
	}

	public static String loadKML_FS(String filename) {
		File f = new File(filename);
		if (!f.exists()){
			System.out.println("Wikimapia: "+filename+" does not exist");
			return null;
		}
		//System.out.println("Wikimapia: loading "+filename);
		try {
			/*
			FileInputStream fis = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			//FileReader fileread = new FileReader(filename);
			BufferedReader bufread = new BufferedReader(isr);
			String str = new String();
			String res = new String();
			while ((str = bufread.readLine()) != null) {
				res += str;
			}
			bufread.close();
			return res;
			*/

			int len = (int) (new File(filename).length());
			FileInputStream fis = new FileInputStream(filename);
			byte buf[] = new byte[len];
			fis.read(buf);
			fis.close();
			try{
				return new String(buf, "UTF-8");
			} catch(UnsupportedEncodingException e) {
				System.out.println("UnsupportedEncodingException");
				return new String(buf);
			}
		} catch (IOException e) {
			System.out.println("Wikimapia loadKML: IOException");
			e.printStackTrace();
		}
		return null;
	}


	public static ArrayList drawTile(XYint xy, Graphics dbf, XYint[] matrix){
		ArrayList kmlsForTile = Wikimapia.getTileKML(xy, Config.zoom);
		if (kmlsForTile!=null){
			dbf.setColor(ColorsAndFonts.clWikimapia);
			//dbf.setFont(ColorsAndFonts.font10);
			for (Iterator iterator = kmlsForTile.iterator(); iterator.hasNext();) {
				KML kml = (KML) iterator.next();
				//if (kml.x.length<=1) continue;
	        	//inmatrix.subtract(matrix[0]); //find point in tile matrix
	        	//inmatrix.add(matrix[1]); //point with matrix position drawing offset
				kml.drawnPoly = new Polygon(kml.x, kml.y, kml.x.length);
				kml.drawnPoly.translate(-matrix[0].x+matrix[1].x, -matrix[0].y+matrix[1].y);
				dbf.drawPolygon(kml.drawnPoly);
				//dbf.drawString(kml.description, kml.drawnPoly.getBounds().x, kml.drawnPoly.getBounds().y);
			}
		}
		return kmlsForTile;
	}

}
