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


 public static Cache kmlCache;
 public static final String mapDir = "Wiki";
 public static ArrayList drawnKmls = new ArrayList();

 public Wikimapia()
 {
 }

 public static String getCachePath(int x, int y, int zoom)
 {
  return Config.cachePath + StringUtil.fileSep + "Wiki" + StringUtil.fileSep + "z" + Config.zoom + StringUtil.fileSep + x / 1024 + StringUtil.fileSep + "x" + x + StringUtil.fileSep + y / 1024 + StringUtil.fileSep + "y" + y + ".kml";
 }

 public static ArrayList getTileKML(XYint tileXY, int zoom)
 {
  String filename = getCachePath(tileXY.x, tileXY.y, zoom);
  int cachei = kmlCache.containsKey(filename);
  if (cachei > -1)
  {
   return (ArrayList)kmlCache.get(cachei);
  } else
  {
   String kmlstr = loadKML(filename);
   ArrayList kmls = parse(kmlstr, zoom);
   kmlCache.put(filename, kmls);
   return kmls;
  }
 }

 private static ArrayList parse(String kmlstr, int zoom)
  throws NumberFormatException
 {
  if (kmlstr == null)
   return null;
  ArrayList kmls = new ArrayList(1);
  for (int descrPos = 0; (descrPos = kmlstr.indexOf("<description>", descrPos + 1)) != -1;)
  {
   KML kml = new KML();
   int descrTextStart = kmlstr.indexOf("<![CDATA[", descrPos + 1) + "<![CDATA[".length();
   int descrTextEnd = kmlstr.indexOf("<br>", descrTextStart + 1);
   kml.description = kmlstr.substring(descrTextStart, descrTextEnd).trim();
   int coordinatesStart = kmlstr.indexOf("<coordinates>", descrTextEnd) + "<coordinates>".length();
   int coordinatesEnd = kmlstr.indexOf("</coordinates>", coordinatesStart + 1);
   String coordsStr = kmlstr.substring(coordinatesStart, coordinatesEnd).trim();
   String coordsStrs[] = StringUtil.split(coordsStr, "\n");
   kml.x = new int[coordsStrs.length];
   kml.y = new int[coordsStrs.length];
   for (int i = 0; i < coordsStrs.length; i++)
   {
    int coma1 = coordsStrs[i].indexOf(',', 1);
    int coma2 = coordsStrs[i].indexOf(',', coma1 + 1);
    double lng = Double.valueOf(coordsStrs[i].substring(0, coma1)).doubleValue();
    double lat = Double.valueOf(coordsStrs[i].substring(coma1 + 1, coma2)).doubleValue();
    XYint intile = TilesUtil.coordinateToDisplay(lat, lng, zoom);
    kml.x[i] = intile.x;
    kml.y[i] = intile.y;
   }

   kmls.add(kml);
  }

  System.out.println("Wikimapia: Parsed " + kmls.size() + " items");
  return kmls.size() <= 0 ? null : kmls;
 }

 public static String loadKML(String filename)
 {
  String zipnames[] = TilesUtil.checkZipFileExistance(filename);
  if (zipnames != null)
  {
   System.out.print("KML unzip: " + zipnames[0] + ": " + zipnames[1]);
   Date start = new Date();
   byte binary[] = Zip.getZipFile(zipnames[0], zipnames[1]);
   System.out.println(" " + ((new Date()).getTime() - start.getTime()) + "ms");
   if (binary == null)
   {
    System.out.println(" FAILED \n");
    return null;
   }
   try
   {
    return new String(binary, "UTF-8");
   }
   catch (UnsupportedEncodingException e)
   {
    System.out.println("UnsupportedEncodingException");
   }
   return new String(binary);
  } else
  {
   return loadKML_FS(filename);
  }
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
   int len = (int)(new File(filename)).length();
   FileInputStream fis = new FileInputStream(filename);
   byte buf[] = new byte[len];
   fis.read(buf);
   fis.close();
   try
   {
    return new String(buf, "UTF-8");
   }
   catch (UnsupportedEncodingException e)
   {
    System.out.println("UnsupportedEncodingException");
   }
   return new String(buf);
  }
  catch (IOException e)
  {
   System.out.println("Wikimapia loadKML: IOException");
   e.printStackTrace();
   return null;
  }
 }

 public static ArrayList drawTile(XYint xy, Graphics dbf, XYint matrix[])
 {
  ArrayList kmlsForTile = getTileKML(xy, Config.zoom);
  if (kmlsForTile != null)
  {
   dbf.setColor(ColorsAndFonts.clWikimapia);
   KML kml;
   for (Iterator iterator = kmlsForTile.iterator(); iterator.hasNext(); dbf.drawPolygon(kml.drawnPoly))
   {
    kml = (KML)iterator.next();
    kml.drawnPoly = new Polygon(kml.x, kml.y, kml.x.length);
    kml.drawnPoly.translate(-matrix[0].x + matrix[1].x, -matrix[0].y + matrix[1].y);
   }

  }
  return kmlsForTile;
 }

}
