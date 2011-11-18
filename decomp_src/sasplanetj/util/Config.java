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

  public String name;
  public String dir;
  public String extension;

  public MapInfo(String name, String dir, String extension)
  {
   this.name = name;
   this.dir = dir;
   this.extension = extension;
  }
 }


 public static String curDir;
 public static String configFilename;
 public static final String cachePath;
 public static boolean connectGPS;
 public static int zoom;
 public static ArrayList zoomsAvail = new ArrayList(19);
 public static boolean drawGrid;
 public static boolean drawLatLng;
 public static boolean drawTail;
 public static boolean trackLog;
 public static boolean drawWikimapia;
 public static int trackTailSize;
 public static int imageCacheSize;
 public static int wikikmlCacheSize;
 public static int drawMapSkip;
 public static int trackLogSkip;
 public static int curMapIndex;
 public static String curMapDir;
 public static String curMapExt;
 public static boolean curMapYandex = true;
 public static final MapInfo maps[] = {
  new MapInfo("Google satellite", "SAT", "jpg"), new MapInfo("Google map", "MAP", "png"), new MapInfo("Google landscape", "LAND", "jpg"), new MapInfo("Yandex satellite", "yasat", "jpg"), new MapInfo("Yandex map", "yamap", "jpg"), new MapInfo("Digital Globe", "DGsat", "jpg"), new MapInfo("Virtual Earth satellite", "vesat", "jpg"), new MapInfo("Gurtam", "Gumap", "PNG"), new MapInfo("WikiMap", "WikiMapia", "png")
 };
 public static final Properties ini = new Properties();

 public Config()
 {
 }

 public static void switchMapTo(int mapIndex)
 {
  curMapIndex = mapIndex;
  curMapYandex = maps[curMapIndex].name.startsWith("Yandex");
  curMapDir = maps[curMapIndex].dir;
  curMapExt = maps[curMapIndex].extension;
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
   e.printStackTrace();
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
  trackTailSize = Integer.valueOf(ini.getProperty("trackTailSize", "50")).intValue();
  imageCacheSize = Integer.valueOf(ini.getProperty("imageCacheSize", "32")).intValue();
  wikikmlCacheSize = Integer.valueOf(ini.getProperty("wikikmlCacheSize", "32")).intValue();
  drawMapSkip = Integer.valueOf(ini.getProperty("drawMapSkip", "0")).intValue();
  trackLogSkip = Integer.valueOf(ini.getProperty("trackLogSkip", "0")).intValue();
  String zoomsStr = ini.getProperty("zoomsAvail", "1,2,3,4,5,6,7,8,10,12,14,17");
  String zoomsAvailArray[] = StringUtil.split(zoomsStr, ",");
  for (int i = 0; i < zoomsAvailArray.length; i++)
   zoomsAvail.add(Integer.valueOf(zoomsAvailArray[i]));

 }

 public static void save()
 {
  try
  {
   FileWriter out = new FileWriter(configFilename, false);
   out.write("lat=" + String.valueOf(Main.latlng.lat) + "\r\n");
   out.write("longitude=" + String.valueOf(Main.latlng.lng) + "\r\n");
   out.write("zoom=" + String.valueOf(zoom) + "\r\n");
   out.write("curMap=" + String.valueOf(curMapIndex) + "\r\n");
   out.write("\r\n# \u041F\u043E\u0434\u043A\u043B\u044E\u0447\u0430\u0442\u044C\u0441\u044F \u043A GPS\r\n");
   out.write("connectGPS=" + String.valueOf(connectGPS) + "\r\n");
   out.write("\r\n# \u0420\u0438\u0441\u043E\u0432\u0430\u0442\u044C \u0441\u0435\u0442\u043A\u0443\r\n");
   out.write("drawGrid=" + String.valueOf(drawGrid) + "\r\n");
   out.write("\r\n# \u0420\u0438\u0441\u043E\u0432\u0430\u0442\u044C \u043A\u043E\u043E\u0440\u0434\u0438\u043D\u0430\u0442\u044B\r\n");
   out.write("drawLatLng=" + String.valueOf(drawLatLng) + "\r\n");
   out.write("\r\n# \u041F\u0438\u0441\u0430\u0442\u044C \u0442\u0440\u0435\u043A\r\n");
   out.write("trackLog=" + String.valueOf(trackLog) + "\r\n");
   out.write("\r\n# \u0420\u0438\u0441\u043E\u0432\u0430\u0442\u044C \u0445\u0432\u043E\u0441\u0442\r\n");
   out.write("drawTail=" + String.valueOf(drawTail) + "\r\n");
   out.write("\r\n# \u0421\u043A\u043E\u043B\u044C\u043A\u043E \u0442\u043E\u0447\u0435\u043A \u043E\u0442\u043E\u0431\u0440\u0430\u0436\u0430\u0442\u044C \u0432 \u0445\u0432\u043E\u0441\u0442\u0435 \u0442\u0440\u0435\u043A\u0430\r\n");
   out.write("trackTailSize=" + String.valueOf(trackTailSize) + "\r\n");
   out.write("\r\n# \u0421\u043A\u043E\u043B\u044C\u043A\u043E \u0442\u0430\u0439\u043B\u043E\u0432 \u0431\u0443\u0434\u0435\u0442 \u043A\u0435\u0448\u0438\u0440\u043E\u0432\u0430\u0442\u044C\u0441\u044F \u0432 \u043F\u0430\u043C\u044F\u0442\u0438\r\n");
   out.write("imageCacheSize=" + String.valueOf(imageCacheSize) + "\r\n");
   out.write("\r\n# \u041F\u0440\u043E\u043F\u0443\u0441\u043A\u0430\u0442\u044C \u0442\u043E\u0447\u0435\u043A \u043F\u0440\u0438 \u043E\u0442\u0440\u0438\u0441\u043E\u0432\u043A\u0435 \u043A\u0430\u0440\u0442\u044B\r\n");
   out.write("drawMapSkip=" + String.valueOf(drawMapSkip) + "\r\n");
   out.write("\r\n# \u041F\u0440\u043E\u043F\u0443\u0441\u043A\u0430\u0442\u044C \u0442\u043E\u0447\u0435\u043A \u043F\u0440\u0438 \u0432\u0435\u0434\u0435\u043D\u0438\u0438 \u0437\u0430\u043F\u0438\u0441\u0438 \u0442\u0440\u0435\u043A\u0430\r\n");
   out.write("trackLogSkip=" + String.valueOf(trackLogSkip) + "\r\n");
   out.write("\r\n# \u0420\u0438\u0441\u043E\u0432\u0430\u0442\u044C Wikimapia \u043F\u043E\u0432\u0435\u0440\u0445\r\n");
   out.write("drawWikimapia=" + String.valueOf(drawWikimapia) + "\r\n");
   out.write("\r\n# \u0421\u043A\u043E\u043B\u044C\u043A\u043E \u0440\u0430\u0441\u043F\u0430\u0440\u0441\u0435\u043D\u044B\u0445 wikimapia kml \u0445\u0440\u0430\u043D\u0438\u0442\u044C \u0432 \u043F\u0430\u043C\u044F\u0442\u0438\r\n");
   out.write("wikikmlCacheSize=" + String.valueOf(wikikmlCacheSize) + "\r\n");
   out.write("\r\n# \u0414\u043E\u0441\u0442\u0443\u043F\u043D\u044B\u0435 \u0443\u0440\u043E\u0432\u043D\u0438 \u0437\u0443\u043C\u0430\r\n");
   out.write("zoomsAvail=");
   boolean first = true;
   for (Iterator iterator = zoomsAvail.iterator(); iterator.hasNext();)
   {
    Integer z = (Integer)iterator.next();
    out.write((first ? "" : ",") + z.toString());
    first = false;
   }

   out.write("\r\n");
   out.flush();
  }
  catch (IOException e)
  {
   e.printStackTrace();
  }
 }

 static 
 {
  curDir = System.getProperty("user.dir");
  configFilename = StringUtil.normPath(curDir + "/config.txt");
  cachePath = StringUtil.normPath(curDir + "/cache");
 }
}
