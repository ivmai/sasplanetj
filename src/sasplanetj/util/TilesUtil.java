package sasplanetj.util;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import sasplanetj.App;
import sasplanetj.gps.LatLng;
import sasplanetj.ui.ColorsAndFonts;

/*
http://forum.openstreetmap.org/viewtopic.php?id=244

QPoint coordinateToDisplay(Coordinate& coordinate, int zoom)
{    
    double numberOfTiles = pow(2, zoom);
    // LonToX
    double x = (coordinate.getLongitude()+180) * (numberOfTiles*tilesize)/360.;
    // LatToY
    double projection = log(tan(PI/4+deg_rad(coordinate.getLatitude())/2));
    double y = (projection /PI);
    y = 1-y;
    y = y /2  * (numberOfTiles*tilesize);
    QPoint point = QPoint(int(x), int(y));
    return point;
}

Coordinate displayToCoordinate(const QPoint& point, int zoom)
{
    // longitude
    double longitude = (point.x()*(360/(pow(2,zoom)*256)))-180;
    // lat
    double lat = point.y()*(2/(pow(2,zoom)*256));
    lat = 1-lat;
    lat = lat*PI;
    lat = rad_deg(atan(sinh(lat)));
    
    Coordinate coord = Coordinate(longitude, lat);
    return coord;
}

 */

public class TilesUtil {
	
	public static final int ZOOM_MIN = 1;
	public static final int ZOOM_MAX = 19;

	public static final int TILESIZE = 256;
    public static Cache tilesCache; //<String, Image> 

    
    
    /*
     * http://sasgis.ru/2009/05/27/yandekskarty-na-google-api/#more-248
     * Yandex maps have ellipse projection
     */
	public static double atanh(double x) {
		return 0.5 * Math.log((1 + x) / (1 - x));
	}
	public static XYint coordinateToDisplayYandex(double lat, double lng, int zoom){
		double PixelsAtZoom = TILESIZE*Math.pow(2,zoom);
		double exct = 0.0818197;
		double z = Math.sin(Math.toRadians(lat));
		double c = PixelsAtZoom/(2*Math.PI);
		double x = Math.floor(PixelsAtZoom/2+lng*(PixelsAtZoom/360));
		double y = Math.floor(PixelsAtZoom/2-c*(atanh(z)-exct*atanh(exct*z)));		
	    //why??? but it works so
	    y = y/2;
	    x = x/2;
	    
	    //System.out.println("ya="+new XYint((int)Math.floor(x), (int)Math.floor(y)));
		return new XYint((int)Math.floor(x), (int)Math.floor(y));
	}
    
    
	public static XYint coordinateToDisplay(double lat, double lng, int zoom){
		if (Config.curMapYandex) return coordinateToDisplayYandex(lat, lng, zoom);
		
	    double numberOfTiles = Math.pow(2, zoom);
	    // LonToX
	    double x = (lng+180) * (numberOfTiles*TILESIZE)/360.;
	    // LatToY
	    double projection = Math.log(Math.tan(Math.PI/4+Math.toRadians(lat)/2));
	    double y = (projection /Math.PI);
	    y = 1-y;
	    y = y/2  * (numberOfTiles*TILESIZE);
	    
	    //why??? but it works so
	    y = y/2;
	    x = x/2;
	    
	    //System.out.println(new XYint((int)Math.floor(x), (int)Math.floor(y)));
	    return new XYint((int)Math.floor(x), (int)Math.floor(y));
	}	

	/** returns Tile XY with given display coordinates*/
	public static XYint getTileByDisplayCoord(XYint p){
		return new XYint((int)Math.floor(p.x/TILESIZE), (int)Math.floor(p.y/TILESIZE));
	}
	
	
	public static LatLng displayToCoordinate(XYint point, int zoom) {
		point.x = point.x*2;
		point.y = point.y*2;
		// longitude
		double longitude = (point.x * (360 / (Math.pow(2, zoom) * TILESIZE))) - 180;
		// lat
		double lat = point.y * (2 / (Math.pow(2, zoom) * 256));
		lat = 1 - lat;
		lat = lat * Math.PI;
		lat = Math.toDegrees(Math.atan((Math.sinh(lat))));

		return new LatLng(lat, longitude);
	}	
	
	
	
    public static String getCachePath(int x, int y, int zoom){
    	//sasplanet\cache\sat\z17\37\x38349\21\y22110.jpg
    	//result:=path+'\z'+zoom+'\'+(x div 1024)+'\x'+x+'\'+(y div 1024)+'\y'+y+ext;
    	return Config.cachePath+StringUtil.fileSep+Config.curMapDir+StringUtil.fileSep+"z"+Config.zoom+StringUtil.fileSep+(x/1024)+StringUtil.fileSep+"x"+x+StringUtil.fileSep+(y/1024)+StringUtil.fileSep+"y"+y+"."+Config.curMapExt;
    }
    
	public static Image getTileImage(XYint tileXY, int zoom) {
		String filename = getCachePath(tileXY.x, tileXY.y, zoom);
		
		int cachei = tilesCache.containsKey(filename); 
		if (cachei>-1) return (Image)tilesCache.get(cachei);
		
		Image img = loadImage(filename);
		tilesCache.put(filename, img);
		//System.out.println("Caching "+filename);
		return img;
	} 	
	
	
	public static Image loadImage(String filename) {
		/*Check if there are any zips instead of directories*/
		String[] zipnames = checkZipFileExistance(filename);
		if (zipnames != null) {
			System.out.print("Image unzip: " + zipnames[0] + ": " + zipnames[1]);
			Date start = new Date();
			byte[] binary = Zip.getZipFile(zipnames[0], zipnames[1]);
			if (binary == null){
				System.out.println(" FAILED \n");
				return null;
			}
			//return Toolkit.getDefaultToolkit().createImage(binary);
			
			//Ensure that image is created. Otherwise sometimes there is white areas painted instead of image.
	    	Image img = Toolkit.getDefaultToolkit().createImage(binary);
	    	MediaTracker mt = new MediaTracker(App.getSelf());
	    	mt.addImage(img, 0);
	    	try {
				mt.waitForAll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(" " + (new Date().getTime() - start.getTime()) + "ms");
			return img;
		}
		
		return loadImageFromFS(filename);
		
	}
	
	/**
	 * Will return {zipname, filename} or null
	 * For some reason fileSep in zips is / always, never \
	 */
	public static String[] checkZipFileExistance(String filename) {
		String[] splits = StringUtil.split(filename.substring(Config.curDir.length()), StringUtil.fileSep);
		String prepath = Config.curDir+StringUtil.fileSep;
		for (int i = 0; i < splits.length-1; i++) { //skip last which is filename itself
			if (new File(prepath+splits[i]).exists()){
				prepath += splits[i]+StringUtil.fileSep;
				continue;
			}else{//check for zip
				if (new File(prepath+splits[i]+".zip").exists()){
					String zipname = prepath + splits[i]+".zip";
					String fileinzip = "";
					for (int j = i; j < splits.length; j++) {
						fileinzip += splits[j] + (j<splits.length-1 ? "/" : "");
					}
					return new String[]{zipname, fileinzip};
				}
			}
		}
		return null;
	}
	
	public static Image loadImageFromFS(String filename) {
		//System.out.println(filename);
    	Image img = Toolkit.getDefaultToolkit().getImage(filename);
    	MediaTracker mt = new MediaTracker(App.getSelf());
    	mt.addImage(img, 0);
    	try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    	if (img==null || img.getHeight(null)<0 || img.getWidth(null)<0) {
    		System.out.println("Error loading image "+filename);
    		return null;
    	}else{
    		return img;
    	}
	}	
	
	
	/**
	 * calculate how much tiles needed to cover the area
	 */
	public static XYint[] drawTilesArea(int width, int height, XYint centerTileTopLeft, XYint tileXY, Graphics dbf){
		int tilesToLeft = centerTileTopLeft.x>0 ? (int) Math.ceil((double)centerTileTopLeft.x/TILESIZE) : 0;
		int tilesToRight = centerTileTopLeft.x+TILESIZE<width ? (int) Math.ceil((double)(width-centerTileTopLeft.x-TILESIZE)/TILESIZE) : 0;

		int tilesToTop = centerTileTopLeft.y>0 ? (int) Math.ceil((double)centerTileTopLeft.y/TILESIZE) : 0;
		int tilesToBottom = centerTileTopLeft.y+TILESIZE<height ? (int) Math.ceil((double)(height-centerTileTopLeft.y-TILESIZE)/TILESIZE) : 0;
		
		
		int totalMatrixW = tilesToLeft+tilesToRight+1;
		int totalMatrixH = tilesToTop+tilesToBottom+1;
		
		int matrixX = centerTileTopLeft.x - tilesToLeft*TILESIZE;
		int matrixY = centerTileTopLeft.y - tilesToTop*TILESIZE;
		
		XYint[] matrix = {new XYint((tileXY.x-tilesToLeft)*TILESIZE, (tileXY.y-tilesToTop)*TILESIZE), new XYint(matrixX, matrixY)}; 
		
		//System.out.println("matrix"+totalMatrixW+"x"+totalMatrixH+", tilesToTop="+tilesToTop+", tilesToBottom="+tilesToBottom+", tilesToLeft="+tilesToLeft+", tilesToRight="+tilesToRight);
		if (Config.drawWikimapia){
			Wikimapia.drawnKmls.clear();
		}
		for (int i = tileXY.x-tilesToLeft; i < tileXY.x-tilesToLeft+totalMatrixW; i++) {
			for (int j = tileXY.y-tilesToTop; j < tileXY.y-tilesToTop+totalMatrixH; j++) {
				Image img = TilesUtil.getTileImage(new XYint(i, j), Config.zoom);
				int x = matrixX+(i-tileXY.x+tilesToLeft)*TILESIZE;
				int y = matrixY+(j-tileXY.y+tilesToTop)*TILESIZE;
				if (img!=null){
					dbf.drawImage(img, x, y, null);
				}else{
					dbf.setColor(ColorsAndFonts.clImageNotFound);
					dbf.fillRect(x, y, TILESIZE, TILESIZE);
				}
				
				if (Config.drawGrid){
					dbf.setColor(ColorsAndFonts.clGrid);
					dbf.drawRect(x, y, TILESIZE, TILESIZE);
				}
			}
		}

		if (Config.drawWikimapia){
			for (int i = tileXY.x - tilesToLeft; i < tileXY.x - tilesToLeft + totalMatrixW; i++) {
				for (int j = tileXY.y - tilesToTop; j < tileXY.y - tilesToTop + totalMatrixH; j++) {
					ArrayList kmls = Wikimapia.drawTile(new XYint(i, j), dbf, matrix);
					if (kmls != null)
						Wikimapia.drawnKmls.addAll(kmls);
				}
			}
		}
		
		
		
		/**
		 * 1) matrix display coordinates (map pixel)
		 * 2) matrix screen offset coordinates
		 */
		return matrix;
	}
	

	
   
	
}
