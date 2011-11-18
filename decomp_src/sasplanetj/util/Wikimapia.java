// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Wikimapia.java

package sasplanetj.util;

import java.awt.Graphics;
import java.awt.Polygon;
import java.io.*;
import java.util.*;
import sasplanetj.ui.ColorsAndFonts;

// Referenced classes of package sasplanetj.util:
//   Config, StringUtil, XYint, Cache, 
//   TilesUtil, Zip

public class Wikimapia
{
 public static class KML
 {

  public String description;
  public int x[];
  public int y[];
  public Polygon drawnPoly;

  public String strip()
  {
   return StringUtil.replace(description, "&amp;quot;", "\"");
  }

  public KML()
  {
  }
 }

 public static class StopWatch
 {

  private static boolean useNanoTime;

  private long startValue;

  private boolean isValid;

  static
  {
   try
   {
    System.nanoTime();
    useNanoTime = true;
   }
   catch (NoSuchMethodError e)
   {
    System.out.println(
     "No System.nanoTime available, using currentTimeMillis...");
   }
  }

  public void start()
  {
   startValue = useNanoTime ? System.nanoTime() : System.currentTimeMillis();
   isValid = true;
  }

  public int currentMillis()
  {
   return isValid ? (int)(useNanoTime ?
           (System.nanoTime() - startValue) / (1000L * 1000L) :
           System.currentTimeMillis() - startValue) : -1;
  }
 }

 public static Cache kmlCache;
 public static final String mapDir = "Wiki";
 private static final ArrayList drawnKmls = new ArrayList();

 public Wikimapia()
 {
 }

 public static void clearDrawnKmls()
 {
  drawnKmls.clear();
 }

 public static Iterator drawnKmlsIterator()
 {
  return drawnKmls.iterator();
 }

 public static String getCachePath(int x, int y, int zoom)
 {
  return Config.cachePath + StringUtil.fileSep + mapDir + StringUtil.fileSep + "z" + Config.zoom + StringUtil.fileSep + x / 1024 + StringUtil.fileSep + "x" + x + StringUtil.fileSep + y / 1024 + StringUtil.fileSep + "y" + y + ".kml";
 }

 private static ArrayList getTileKML(int x, int y, int zoom, boolean isYandex)
 {
  String filename = getCachePath(x, y, zoom);
  String entryName = isYandex ? filename + "?Y" : filename;
  ArrayList kmls = (ArrayList)kmlCache.get(entryName);
  if (kmls == null)
  {
   String kmlstr = loadKML(filename);
   kmls = parse(kmlstr, zoom, isYandex);
   kmlCache.put(entryName, kmls, false);
  }
  return kmls;
 }

 private static ArrayList parse(String kmlstr, int zoom, boolean isYandex)
  throws NumberFormatException
 {
  if (kmlstr == null)
   return null;
  int totalCoordCnt = 0;
  ArrayList kmls = new ArrayList(1);
  StopWatch watch = new StopWatch();
  watch.start();
  for (int descrPos = 0; (descrPos = kmlstr.indexOf("<description>", descrPos + 1)) != -1;)
  {
   KML kml = new KML();
   int descrTextStart = kmlstr.indexOf("<![CDATA[", descrPos + 1) + "<![CDATA[".length();
   int descrTextEnd = kmlstr.indexOf("<br>", descrTextStart + 1);
   kml.description = kmlstr.substring(descrTextStart, descrTextEnd).trim();
   int coordinatesStart = kmlstr.indexOf("<coordinates>", descrTextEnd) + "<coordinates>".length();
   int coordinatesEnd = kmlstr.indexOf("</coordinates>", coordinatesStart + 1);
   String coordsStr = kmlstr.substring(coordinatesStart, coordinatesEnd).trim();
   StringTokenizer st1 = new StringTokenizer(coordsStr, "\n");
   int coordsCnt = st1.countTokens();
   totalCoordCnt += coordsCnt;
   kml.x = new int[coordsCnt];
   kml.y = new int[coordsCnt];
   double prevLng = 0.0;
   for (int i = 0; i < coordsCnt; i++)
   {
    String coord = st1.nextToken();
    int comma1 = coord.indexOf(',', 1);
    int comma2 = coord.indexOf(',', comma1 + 1);
    double lng = Double.valueOf(coord.substring(0, comma1)).doubleValue();
    if (Math.abs(prevLng - lng) > 180.0)
     lng = lng >= 0.0 ? lng - 360.0 : lng + 360.0;
    double lat = Double.valueOf(
                  coord.substring(comma1 + 1, comma2)).doubleValue();
    prevLng = lng;
    XYint intile = TilesUtil.coordinateToDisplay(lat, lng, zoom, isYandex);
    kml.x[i] = intile.x;
    kml.y[i] = intile.y;
   }
   descrPos = coordinatesEnd;
   kmls.add(kml);
  }
  if (kmls.size() == 0)
   return null;
  System.out.println("Wikimapia: Parsed " + totalCoordCnt +
   " coordinates in " + watch.currentMillis() + "ms");
  return kmls;
 }

 public static String loadKML(String filename)
 {
  String zipnames[] = TilesUtil.checkZipFileExistance(filename);
  if (zipnames != null)
  {
   byte binary[] = Zip.getZipFile(zipnames[0], zipnames[1]);
   if (binary != null)
    return utf8ToString(binary);
  }
  return loadKML_FS(filename);
 }

 private static String utf8ToString(byte buf[])
 {
  String str = null;
  try
  {
   str = new String(buf, "UTF-8");
  }
  catch (UnsupportedEncodingException e)
  {
   System.err.println(e);
  }
  return str;
 }

 public static String loadKML_FS(String filename)
 {
  File f = new File(filename);
  if (!f.exists())
  {
   System.out.println("Wikimapia: " + filename + " does not exist");
   return null;
  }
  try
  {
   int len = (int)f.length();
   DataInputStream fis = new DataInputStream(new FileInputStream(filename));
   byte buf[] = new byte[len];
   fis.readFully(buf);
   fis.close();
   return utf8ToString(buf);
  }
  catch (IOException e)
  {
   System.err.println("Wikimapia loadKML: " + e);
   return null;
  }
 }

 public static void drawTile(int x, int y, int zoom, Graphics dbf,
   XYint matrix[])
 {
  ArrayList kmlsForTile = getTileKML(TilesUtil.adjustTileX(x, zoom), y, zoom,
                           Config.isMapYandex);
  if (kmlsForTile != null)
  {
   int shiftX = (x & ~((1 << (zoom - 1)) - 1)) << TilesUtil.LOG2_TILESIZE;
   dbf.setColor(ColorsAndFonts.clWikimapia);
   KML kml;
   for (Iterator iterator = kmlsForTile.iterator(); iterator.hasNext(); dbf.drawPolygon(kml.drawnPoly))
   {
    kml = (KML)iterator.next();
    kml.drawnPoly = new Polygon(kml.x, kml.y, kml.x.length);
    kml.drawnPoly.translate(shiftX + matrix[1].x - matrix[0].x,
     matrix[1].y - matrix[0].y);
   }
   drawnKmls.addAll(kmlsForTile);
  }
 }

}
