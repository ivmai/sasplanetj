package sasplanetj.util;

import java.io.*;
import java.util.*;

import sasplanetj.Main;


public class Config{

	/**
	 * Current working direcotry. Some JVMs cant set working dirfrom command line, so we have to deal with absolute pathes  
	 */
	public static String curDir = System.getProperty("user.dir");
	public static boolean isCE = System.getProperty("os.name").equals("Windows CE");
	
	public static String configFilename = StringUtil.normPath(curDir+"/config.txt");
	/**
	 * Directory with cache
	 */
	public static final String cachePath = StringUtil.normPath(curDir+"/cache");

	public static boolean connectGPS;
	
	public static int zoom;
	public static ArrayList zoomsAvail = new ArrayList(TilesUtil.ZOOM_MAX-TilesUtil.ZOOM_MIN+1);
	public static boolean drawGrid;
	public static boolean drawLatLng;
	public static boolean drawTail;
	public static boolean trackLog;
	//public static boolean drawZoomLevel;
	public static boolean drawWikimapia;
	
	public static int trackTailSize;
	public static int imageCacheSize;
	public static int wikikmlCacheSize;
	
	public static int drawMapSkip;
	public static int trackLogSkip;

	public static String usermapdir;
	
	/**
	 * Currently selected map
	 */
	public static int curMapIndex;
	public static String curMapDir;
	public static String curMapExt;
	/**
	 * Selected map is yandex
	 */
	public static boolean curMapYandex = true;
	
	public static final MapInfo[] maps = { 
		new MapInfo("Google satellite", "SAT", "jpg"), 
		new MapInfo("Google map", "MAP", "png"), 
		new MapInfo("Google landscape", "LAND", "jpg"), 
		new MapInfo("Yandex satellite", "yasat", "jpg"),	
		new MapInfo("Yandex map", "yamap", "jpg"),	
		//new MapInfo("Yandex hybrid", "yahyb", "png"),	
		new MapInfo("Digital Globe", "DGsat", "jpg"),	
		new MapInfo("Virtual Earth satellite", "vesat", "jpg"),	
		new MapInfo("Gurtam", "Gumap", "PNG"),	
		new MapInfo("WikiMap", "WikiMapia", "png"),	
		new MapInfo("Usermapdir", "usermapdir", "jpg"),	
	};
	
	
	public static void switchMapTo(int mapIndex){
		curMapIndex = mapIndex;
		curMapYandex = maps[curMapIndex].name.startsWith("Yandex"); 
		curMapDir = maps[curMapIndex].dir;
		curMapExt = maps[curMapIndex].extension;
	}
	
	
	public static final Properties ini = new Properties();
	
	public static void load() {
	    try {
	    	if (new File(configFilename).exists())
	    		ini.load(new FileInputStream(configFilename));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    connectGPS = Boolean.valueOf(ini.getProperty("connectGPS", "true")).booleanValue();
	    Main.latlng.lat = Double.valueOf(ini.getProperty("lat", "50.407781")).doubleValue();
	    Main.latlng.lng = Double.valueOf(ini.getProperty("longitude", "30.662485")).doubleValue();
	    curMapIndex = Integer.valueOf(ini.getProperty("curMap", "0")).intValue();
	    zoom = Integer.valueOf(ini.getProperty("zoom", "17")).intValue();
	    drawGrid = Boolean.valueOf(ini.getProperty("drawGrid", "true")).booleanValue();
	    drawLatLng = Boolean.valueOf(ini.getProperty("drawLatLng", "true")).booleanValue();
	    drawTail = Boolean.valueOf(ini.getProperty("drawTail", "true")).booleanValue();
	    trackLog = Boolean.valueOf(ini.getProperty("trackLog", "false")).booleanValue();
	    drawWikimapia = Boolean.valueOf(ini.getProperty("drawWikimapia", "false")).booleanValue();
	    trackTailSize = Integer.valueOf(ini.getProperty("trackTailSize", "50")).intValue();
	    imageCacheSize = Integer.valueOf(ini.getProperty("imageCacheSize", "32")).intValue();
	    wikikmlCacheSize = Integer.valueOf(ini.getProperty("wikikmlCacheSize", "32")).intValue();
	    drawMapSkip = Integer.valueOf(ini.getProperty("drawMapSkip", "0")).intValue();
	    trackLogSkip = Integer.valueOf(ini.getProperty("trackLogSkip", "0")).intValue();

	    usermapdir = ini.getProperty("usermapdir", "usermapdir");
	    maps[maps.length-1].dir = usermapdir; 

	    switchMapTo(curMapIndex);
	    
	    String zoomsStr = ini.getProperty("zoomsAvail", "1,2,3,4,5,6,7,8,10,12,14,17");
    	String[] zoomsAvailArray = StringUtil.split(zoomsStr, ",");
    	for (int i = 0; i < zoomsAvailArray.length; i++) {
		    zoomsAvail.add(Integer.valueOf(zoomsAvailArray[i]));
		}
	}
	
	public static void save(){
		try {
			FileWriter out = new FileWriter(configFilename, false);
			out.write("lat="+String.valueOf(Main.latlng.lat)+"\r\n");
			out.write("longitude="+String.valueOf(Main.latlng.lng)+"\r\n");
			out.write("zoom="+String.valueOf(zoom)+"\r\n");
			out.write("curMap="+String.valueOf(curMapIndex)+"\r\n");
			out.write("\r\n# ������������ � GPS\r\n");
			out.write("connectGPS="+String.valueOf(connectGPS)+"\r\n");
			out.write("\r\n# �������� �����\r\n");
			out.write("drawGrid="+String.valueOf(drawGrid)+"\r\n");
			out.write("\r\n# �������� ����������\r\n");
			out.write("drawLatLng="+String.valueOf(drawLatLng)+"\r\n");
			//out.write("\r\n# �������� ������� ����\r\n");
			//out.write("drawZoomLevel="+String.valueOf(drawZoomLevel)+"\r\n");
			out.write("\r\n# ������ ����\r\n");
			out.write("trackLog="+String.valueOf(trackLog)+"\r\n");
			out.write("\r\n# �������� �����\r\n");
			out.write("drawTail="+String.valueOf(drawTail)+"\r\n");
			out.write("\r\n# ������� ����� ���������� � ������ �����\r\n");
			out.write("trackTailSize="+String.valueOf(trackTailSize)+"\r\n");
			out.write("\r\n# ������� ������ ����� ������������ � ������\r\n");
			out.write("imageCacheSize="+String.valueOf(imageCacheSize)+"\r\n");
			out.write("\r\n# ���������� ����� ��� ��������� �����\r\n");
			out.write("drawMapSkip="+String.valueOf(drawMapSkip)+"\r\n");
			out.write("\r\n# ���������� ����� ��� ������� ������ �����\r\n");
			out.write("trackLogSkip="+String.valueOf(trackLogSkip)+"\r\n");
			out.write("\r\n# �������� Wikimapia ������\r\n");
			out.write("drawWikimapia="+String.valueOf(drawWikimapia)+"\r\n");
			out.write("\r\n# ������� ����������� wikimapia kml ������� � ������\r\n");
			out.write("wikikmlCacheSize="+String.valueOf(wikikmlCacheSize)+"\r\n");

			out.write("\r\n# ���������� � ���������������� �����, ���� ����� �������� �������, ��������\r\n");
			out.write("usermapdir="+usermapdir+"\r\n");			
			
			out.write("\r\n# ��������� ������ ����\r\n");
			out.write("zoomsAvail=");
			boolean first = true;
			for (Iterator iterator = zoomsAvail.iterator(); iterator.hasNext();) {
				Integer z = (Integer)iterator.next();
				out.write((first ? "":",")+z.toString());
				first = false;
			}
			out.write("\r\n");
			
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	
	public static class MapInfo{
		/**
		 * Just name, Google, Yandex...
		 */
		public String name;
		/**
		 * directory name, SAT, yhhyb
		 */
		public String dir;
		/**
		 * tile file extension, jpg, png
		 */
		public String extension;

		public MapInfo(String name, String dir, String extension) {
			this.name = name;
			this.dir = dir;
			this.extension = extension;
		}
		
	}	
}