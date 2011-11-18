// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Config.java

package sasplanetj.util;

import java.io.*;
import java.util.*;
import sasplanetj.Main;
import sasplanetj.gps.LatLng;

// Referenced classes of package sasplanetj.util:
//   StringUtil

public class Config
{
 public static class MapInfo
 {

  public final String name;
  public final char key;
  public final String dir;
  public final String extension;

  public MapInfo(String name, char key, String dir, String extension)
  {
   this.name = name;
   this.key = key;
   this.dir = dir;
   this.extension = extension;
  }
 }


 public static String curDir;
 public static String configFilename;
 public static final String cachePath;
 public static boolean connectGPS;
 public static int zoom;
 public static ArrayList zoomsAvail = new ArrayList(TilesUtil.ZOOM_MAX);
 public static boolean drawGrid;
 public static boolean drawLatLng;
 public static boolean drawTail;
 public static boolean trackLog;
 public static boolean drawWikimapia;
 public static int trackTailSize;
 public static boolean useSoftRefs;
 public static int zipCacheSize;
 public static int imageCacheSize;
 public static int wikikmlCacheSize;
 public static int drawMapSkip;
 public static int trackLogSkip;
 public static int curMapIndex;
 public static String curMapDir;
 public static String curMapExt;
 public static boolean isMapYandex;
 public static final MapInfo maps[] = {
  new MapInfo("Google satellite", 'G', "SAT", "jpg"),
  new MapInfo("Google map", 'M', "MAP", "png"),
  new MapInfo("Google landscape", 'L', "land", "jpg"),
  new MapInfo("Yandex satellite", 'Y', "yasat", "jpg"),
  new MapInfo("Yandex map", 'Q', "yamapng", "png"),
  new MapInfo("OpenStreetMap", 'O', "osmmap", "png"),
  new MapInfo("Virtual Earth satellite", 'V', "vesat", "jpg"),
  new MapInfo("Gurtam", 'U', "gumap", "png"),
  new MapInfo("WikiMap", 'W', "WikiMap", "png")
 };
 public static final Properties ini = new Properties();

 public Config()
 {
 }

 public static void switchMapTo(int mapIndex)
 {
  curMapIndex = mapIndex;
  isMapYandex = maps[curMapIndex].name.startsWith("Yandex");
  curMapDir = maps[curMapIndex].dir;
  curMapExt = maps[curMapIndex].extension;
 }

 public static int curMapMinZoom()
 {
  return maps[curMapIndex].name.startsWith("Gurtam") ? 4 : 1;
 }

 public static void load()
 {
  try
  {
   if ((new File(configFilename)).exists())
    ini.load(new FileInputStream(configFilename));
  }
  catch (Exception e)
  {
   System.err.println("Error loading config: " + e.getMessage());
  }
  connectGPS = Boolean.valueOf(ini.getProperty("connectGPS", "true")).booleanValue();
  Main.latlng.lat = Double.valueOf(ini.getProperty("lat", "50.407781")).doubleValue();
  Main.latlng.lng = Double.valueOf(ini.getProperty("longitude", "30.662485")).doubleValue();
  curMapIndex = Integer.valueOf(ini.getProperty("curMap", "0")).intValue();
  switchMapTo(curMapIndex);
  zoom = Integer.valueOf(ini.getProperty("zoom", "17")).intValue();
  drawGrid = Boolean.valueOf(ini.getProperty("drawGrid", "true")).booleanValue();
  drawLatLng = Boolean.valueOf(ini.getProperty("drawLatLng", "true")).booleanValue();
  drawTail = Boolean.valueOf(ini.getProperty("drawTail", "true")).booleanValue();
  trackLog = Boolean.valueOf(ini.getProperty("trackLog", "false")).booleanValue();
  drawWikimapia = Boolean.valueOf(ini.getProperty("drawWikimapia", "false")).booleanValue();
  trackTailSize =
   Integer.valueOf(ini.getProperty("trackTailSize", "200")).intValue();
  useSoftRefs =
   Boolean.valueOf(ini.getProperty("useSoftRefs", "false")).booleanValue();
  zipCacheSize =
   Integer.valueOf(ini.getProperty("zipCacheSize", "2")).intValue();
  imageCacheSize = Integer.valueOf(ini.getProperty("imageCacheSize", "32")).intValue();
  wikikmlCacheSize = Integer.valueOf(ini.getProperty("wikikmlCacheSize", "64")).intValue();
  drawMapSkip = Integer.valueOf(ini.getProperty("drawMapSkip", "0")).intValue();
  trackLogSkip = Integer.valueOf(ini.getProperty("trackLogSkip", "0")).intValue();
  String zoomsStr =
   ini.getProperty("zoomsAvail", "3,4,5,6,7,8,10,12,14,16,17,18");
  String zoomsAvailArray[] = StringUtil.split(zoomsStr, ",");
  for (int i = 0; i < zoomsAvailArray.length; i++)
   zoomsAvail.add(Integer.valueOf(zoomsAvailArray[i]));

 }

 public static void save()
 {
  try
  {
   PrintWriter out = new PrintWriter(new FileWriter(configFilename));
   out.println("# SAS.Planet.J (sasplanetj) configuration file");
   out.println();
   out.println("lat=" + Main.latlng.lat);
   out.println("longitude=" + Main.latlng.lng);
   out.println("zoom=" + zoom);
   out.println("curMap=" + curMapIndex);
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
   StringBuffer sb = new StringBuffer();
   for (Iterator iterator = zoomsAvail.iterator(); iterator.hasNext();)
    sb.append("," + ((Integer)iterator.next()).toString());
   out.println("# Comma-separated list of available zoom levels");
   out.println("zoomsAvail=" + (sb.length() > 0 ?
    sb.toString().substring(1) : ""));
   out.close();
  }
  catch (IOException e)
  {
   System.err.println("Error saving config.txt: " + e.getMessage());
  }
 }

 private static String getProgBaseFolder()
 {
  String classPath = System.getProperty("java.class.path");
  int pathSepIndex = classPath.indexOf(File.pathSeparatorChar);
  if (pathSepIndex >= 0)
   classPath = classPath.substring(0, pathSepIndex);
  String baseDir = classPath;
  File f = new File(baseDir);
  String name = f.getName();
  if ((name.equals("bin") || name.equals("BIN") ||
      name.endsWith(".jar") || name.endsWith(".JAR") ||
      name.endsWith(".zip") || name.endsWith(".ZIP")) &&
      (baseDir = f.getParent()) == null)
   baseDir = ".";
  return baseDir;
 }

 static 
 {
  /* curDir = System.getProperty("user.dir"); */
  curDir = getProgBaseFolder();
  configFilename = StringUtil.normPath(curDir + File.separator + "config.txt");
  cachePath = StringUtil.normPath(curDir + File.separator + "cache");
 }
}
