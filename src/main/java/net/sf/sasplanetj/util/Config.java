package net.sf.sasplanetj.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import net.sf.sasplanetj.Main;

public class Config {

	/**
	 * Current working directory. Some JVMs cannot set working dir from command
	 * line, so we have to deal with absolute paths
	 */
	public static final String curDir = getProgBaseFolder();

	private static final String configFilename = StringUtil.normPath(curDir
			+ File.separator + "config.txt");
	/**
	 * Directory with cache
	 */
	static final String cachePath = StringUtil.normPath(curDir + File.separator
			+ "cache");

	private static final Properties ini = new Properties();

	public static final ArrayList zoomsAvail = new ArrayList(TilesUtil.ZOOM_MAX);

	/**
	 * Window width and height (0 means max - screen width/height).
	 */
	public static int windowWidth;
	public static int windowHeight;

	public static boolean connectGPS;
	public static int zoom;
	public static boolean drawGrid;
	public static boolean drawLatLng;
	public static boolean drawTail;
	public static boolean trackLog;
	public static boolean drawWikimapia;

	public static int trackTailSize;
	public static int imageCacheSize;
	public static int wikikmlCacheSize;
	public static int zipCacheSize;
	public static boolean useSoftRefs;

	public static int drawMapSkip;
	public static int trackLogSkip;

	private static String usermapdir;

	/**
	 * Currently selected map
	 */
	private static int curMapIndex;

	public static final MapInfo[] maps = {
			new MapInfo("Google satellite", 'G', "sat", "jpg"),
			new MapInfo("Google map", 'M', "map", "png"),
			new MapInfo("Google landscape", 'L', "land", "jpg"),
			new MapInfo("Yandex satellite", 'Y', "yasat", "jpg", 1, true),
			new MapInfo("Yandex map", 'Q', "yamapng", "png", 1, true),
			new MapInfo("OpenStreetMap", 'O', "osmmap", "png"),
			new MapInfo("Virtual Earth satellite", 'V', "vesat", "jpg"),
			new MapInfo("WikiMap", 'W', "WikiMap", "png"),
			new MapInfo("Usermapdir", 'R', null, "jpg") };

	public static int getCurMapIndex() {
		return curMapIndex;
	}

	public static void switchMapTo(int mapIndex) {
		curMapIndex = mapIndex;
	}

	/**
	 * Selected map is Yandex
	 */
	public static boolean isCurMapYandex() {
		return maps[curMapIndex].isYandexProjection;
	}

	public static String getCurMapDir() {
		String dir = maps[curMapIndex].dir;
		return dir != null ? dir : usermapdir;
	}

	public static String getCurMapExt() {
		return maps[curMapIndex].extension;
	}

	public static int getCurMinZoom() {
		return maps[curMapIndex].minZoom;
	}

	public static void load() {
		try {
			if (new File(configFilename).exists()) {
				InputStream inF = new FileInputStream(configFilename);
				ini.load(inF);
				inF.close();
			}
		} catch (Exception e) {
			System.err.println("Error loading config: " + e.getMessage());
		}

		Main.getLatLng().set(
				Double.valueOf(ini.getProperty("lat", "50.407781"))
						.doubleValue(),
				Double.valueOf(ini.getProperty("longitude", "30.662485"))
						.doubleValue());
		curMapIndex = Integer.valueOf(ini.getProperty("curMap", "0"))
				.intValue();
		zoom = Integer.valueOf(ini.getProperty("zoom", "17")).intValue();
		windowWidth = decodeWinSizeValue(ini.getProperty("windowWidth"));
		windowHeight = decodeWinSizeValue(ini.getProperty("windowHeight"));
		connectGPS = Boolean.valueOf(ini.getProperty("connectGPS", "true"))
				.booleanValue();
		drawGrid = Boolean.valueOf(ini.getProperty("drawGrid", "true"))
				.booleanValue();
		drawLatLng = Boolean.valueOf(ini.getProperty("drawLatLng", "true"))
				.booleanValue();
		trackLog = Boolean.valueOf(ini.getProperty("trackLog", "false"))
				.booleanValue();
		drawTail = Boolean.valueOf(ini.getProperty("drawTail", "true"))
				.booleanValue();
		trackTailSize = Integer
				.valueOf(ini.getProperty("trackTailSize", "200")).intValue();
		imageCacheSize = Integer.valueOf(
				ini.getProperty("imageCacheSize", "32")).intValue();
		drawMapSkip = Integer.valueOf(ini.getProperty("drawMapSkip", "0"))
				.intValue();
		trackLogSkip = Integer.valueOf(ini.getProperty("trackLogSkip", "0"))
				.intValue();
		drawWikimapia = Boolean.valueOf(
				ini.getProperty("drawWikimapia", "false")).booleanValue();
		wikikmlCacheSize = Integer.valueOf(
				ini.getProperty("wikikmlCacheSize", "64")).intValue();
		zipCacheSize = Integer.valueOf(ini.getProperty("zipCacheSize", "2"))
				.intValue();
		useSoftRefs = Boolean.valueOf(ini.getProperty("useSoftRefs", "false"))
				.booleanValue();
		usermapdir = ini.getProperty("usermapdir", "usermapdir");

		switchMapTo(curMapIndex);

		String zoomsStr = ini.getProperty("zoomsAvail",
				"3,4,5,6,7,8,10,12,14,16,17,18");
		String[] zoomsAvailArray = StringUtil.split(zoomsStr, ",");
		for (int i = 0; i < zoomsAvailArray.length; i++) {
			zoomsAvail.add(Integer.valueOf(zoomsAvailArray[i]));
		}
	}

	private static int decodeWinSizeValue(String str) {
		return str == null || str.equals("max") ? 0 : Integer.valueOf(str)
				.intValue();
	}

	public static void save() {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(configFilename));
			out.println("# SAS.Planet.J (sasplanetj) configuration file");
			out.println();
			out.println("lat=" + Main.getLatLng().getLat());
			out.println("longitude=" + Main.getLatLng().getLng());
			out.println("zoom=" + zoom);
			out.println("curMap=" + getCurMapIndex());
			out.println();
			out.println("# Application window width and height (decimal value or 'max')");
			out.println("windowWidth="
					+ (windowWidth != 0 ? "" + windowWidth : "max"));
			out.println("windowHeight="
					+ (windowHeight != 0 ? "" + windowHeight : "max"));
			out.println();
			out.println("# Connect to GPS at start-up");
			out.println("connectGPS=" + connectGPS);
			out.println();
			out.println("# Draw map grid");
			out.println("drawGrid=" + drawGrid);
			out.println();
			out.println("# Draw coordinates");
			out.println("drawLatLng=" + drawLatLng);
			out.println();
			out.println("# Turn on track logging");
			out.println("trackLog=" + trackLog);
			out.println();
			out.println("# Draw track tail");
			out.println("drawTail=" + drawTail);
			out.println();
			out.println("# How many points to draw in track tail");
			out.println("trackTailSize=" + trackTailSize);
			out.println();
			out.println("# Amount of tails to cache in RAM");
			out.println("imageCacheSize=" + imageCacheSize);
			out.println();
			out.println("# How many points to skip on map drawing");
			out.println("drawMapSkip=" + drawMapSkip);
			out.println();
			out.println("# How many points to skip on track recording");
			out.println("trackLogSkip=" + trackLogSkip);
			out.println();
			out.println("# Draw Wikimapia layer");
			out.println("drawWikimapia=" + drawWikimapia);
			out.println();
			out.println("# How many parsed Wikimapia KMLs to cache in RAM");
			out.println("wikikmlCacheSize=" + wikikmlCacheSize);
			out.println();

			out.println("# How many ZIP files to keep open for quick access");
			out.println("zipCacheSize=" + zipCacheSize);
			out.println();
			out.println("# Use SoftReference-based cache");
			out.println("useSoftRefs=" + useSoftRefs);
			out.println();

			out.println("# User-defined map folder (e.g. for 'Gurtam' or 'GenShtab')");
			out.println("usermapdir=" + usermapdir);
			out.println();

			StringBuffer sb = new StringBuffer();
			for (Iterator iterator = zoomsAvail.iterator(); iterator.hasNext();) {
				Integer z = (Integer) iterator.next();
				sb.append("," + z.toString());
			}
			out.println("# Comma-separated list of available zoom levels");
			out.println("zoomsAvail="
					+ (sb.length() > 0 ? sb.toString().substring(1) : ""));
			out.close();
		} catch (IOException e) {
			System.err.println("Error saving config.txt: " + e.getMessage());
		}

	}

	private static String getProgBaseFolder() {
		String classPath = System.getProperty("java.class.path");
		int pathSepIndex = classPath.indexOf(File.pathSeparatorChar);
		if (pathSepIndex >= 0) {
			classPath = classPath.substring(0, pathSepIndex);
		}
		String baseDir = classPath;
		File f = new File(baseDir);
		String name = f.getName();

		if ((name.equals("bin") || name.equals("BIN") || name.endsWith(".jar")
				|| name.endsWith(".JAR") || name.endsWith(".zip") || name
				.endsWith(".ZIP")) && (baseDir = f.getParent()) == null) {
			baseDir = ".";
		}
		return baseDir;
	}

	public static class MapInfo {
		/**
		 * Just name, Google, Yandex...
		 */
		public final String name;

		public final char key;

		/**
		 * directory name (e.g., "yamapng"). If null then use usermapdir value.
		 */
		final String dir;
		/**
		 * tile file extension, jpg, png
		 */
		final String extension;

		final int minZoom; // use 4 for "Gurtam" map, 1 typically for others

		final boolean isYandexProjection;

		public MapInfo(String name, char key, String dir, String extension) {
			this(name, key, dir, extension, 1, false);
		}

		public MapInfo(String name, char key, String dir, String extension,
				int minZoom, boolean isYandexProjection) {
			this.name = name;
			this.key = key;
			this.dir = dir;
			this.extension = extension;
			this.minZoom = minZoom;
			this.isYandexProjection = isYandexProjection;
		}
	}
}
