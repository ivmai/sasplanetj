// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   TilesUtil.java

package sasplanetj.util;

import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import sasplanetj.App;
import sasplanetj.gps.LatLng;
import sasplanetj.ui.ColorsAndFonts;

// Referenced classes of package sasplanetj.util:
//   XYint, Config, StringUtil, Cache, 
//   Zip, Wikimapia

public class TilesUtil
{

 public static final int ZOOM_MIN = 1;
 public static final int ZOOM_MAX = 19;
 public static final int TILESIZE = 256;
 public static Cache tilesCache;

 public TilesUtil()
 {
 }

 public static double atanh(double x)
 {
  return 0.5D * Math.log((1.0D + x) / (1.0D - x));
 }

 public static XYint coordinateToDisplayYandex(double lat, double lng, int zoom)
 {
  double PixelsAtZoom = 256D * Math.pow(2D, zoom);
  double exct = 0.081819699999999995D;
  double z = Math.sin(Math.toRadians(lat));
  double c = PixelsAtZoom / 6.2831853071795862D;
  double x = Math.floor(PixelsAtZoom / 2D + lng * (PixelsAtZoom / 360D));
  double y = Math.floor(PixelsAtZoom / 2D - c * (atanh(z) - exct * atanh(exct * z)));
  y /= 2D;
  x /= 2D;
  return new XYint((int)Math.floor(x), (int)Math.floor(y));
 }

 public static XYint coordinateToDisplay(double lat, double lng, int zoom)
 {
  if (Config.curMapYandex)
  {
   return coordinateToDisplayYandex(lat, lng, zoom);
  } else
  {
   double numberOfTiles = Math.pow(2D, zoom);
   double x = ((lng + 180D) * (numberOfTiles * 256D)) / 360D;
   double projection = Math.log(Math.tan(0.78539816339744828D + Math.toRadians(lat) / 2D));
   double y = projection / 3.1415926535897931D;
   y = 1.0D - y;
   y = (y / 2D) * (numberOfTiles * 256D);
   y /= 2D;
   x /= 2D;
   return new XYint((int)Math.floor(x), (int)Math.floor(y));
  }
 }

 public static XYint getTileByDisplayCoord(XYint p)
 {
  return new XYint((int)Math.floor(p.x / 256), (int)Math.floor(p.y / 256));
 }

 public static LatLng displayToCoordinate(XYint point, int zoom)
 {
  point.x = point.x * 2;
  point.y = point.y * 2;
  double longitude = (double)point.x * (360D / (Math.pow(2D, zoom) * 256D)) - 180D;
  double lat = (double)point.y * (2D / (Math.pow(2D, zoom) * 256D));
  lat = 1.0D - lat;
  lat *= 3.1415926535897931D;
  lat = Math.toDegrees(Math.atan(Math.sinh(lat)));
  return new LatLng(lat, longitude);
 }

 public static String getCachePath(int x, int y, int zoom)
 {
  return Config.cachePath + StringUtil.fileSep + Config.curMapDir + StringUtil.fileSep + "z" + Config.zoom + StringUtil.fileSep + x / 1024 + StringUtil.fileSep + "x" + x + StringUtil.fileSep + y / 1024 + StringUtil.fileSep + "y" + y + "." + Config.curMapExt;
 }

 public static Image getTileImage(XYint tileXY, int zoom)
 {
  String filename = getCachePath(tileXY.x, tileXY.y, zoom);
  int cachei = tilesCache.containsKey(filename);
  if (cachei > -1)
  {
   return (Image)tilesCache.get(cachei);
  } else
  {
   Image img = loadImage(filename);
   tilesCache.put(filename, img);
   return img;
  }
 }

 public static Image loadImage(String filename)
 {
  String zipnames[] = checkZipFileExistance(filename);
  if (zipnames != null)
  {
   System.out.print("Image unzip: " + zipnames[0] + ": " + zipnames[1]);
   Date start = new Date();
   byte binary[] = Zip.getZipFile(zipnames[0], zipnames[1]);
   if (binary == null)
   {
    System.out.println(" FAILED \n");
    return null;
   }
   Image img = Toolkit.getDefaultToolkit().createImage(binary);
   MediaTracker mt = new MediaTracker(App.getSelf());
   mt.addImage(img, 0);
   try
   {
    mt.waitForAll();
   }
   catch (InterruptedException e)
   {
    e.printStackTrace();
   }
   System.out.println(" " + ((new Date()).getTime() - start.getTime()) + "ms");
   return img;
  } else
  {
   return loadImageFromFS(filename);
  }
 }

 public static String[] checkZipFileExistance(String filename)
 {
  String splits[] = StringUtil.split(filename.substring(Config.curDir.length()), StringUtil.fileSep);
  String prepath = Config.curDir + StringUtil.fileSep;
  for (int i = 0; i < splits.length - 1; i++)
   if ((new File(prepath + splits[i])).exists())
    prepath = prepath + splits[i] + StringUtil.fileSep;
   else
   if ((new File(prepath + splits[i] + ".zip")).exists())
   {
    String zipname = prepath + splits[i] + ".zip";
    String fileinzip = "";
    for (int j = i; j < splits.length; j++)
     fileinzip = fileinzip + splits[j] + (j >= splits.length - 1 ? "" : "/");

    return (new String[] {
     zipname, fileinzip
    });
   }

  return null;
 }

 public static Image loadImageFromFS(String filename)
 {
  Image img = Toolkit.getDefaultToolkit().getImage(filename);
  MediaTracker mt = new MediaTracker(App.getSelf());
  mt.addImage(img, 0);
  try
  {
   mt.waitForAll();
  }
  catch (InterruptedException e)
  {
   e.printStackTrace();
  }
  if (img == null || img.getHeight(null) < 0 || img.getWidth(null) < 0)
  {
   System.out.println("Error loading image " + filename);
   return null;
  } else
  {
   return img;
  }
 }

 public static XYint[] drawTilesArea(int width, int height, XYint centerTileTopLeft, XYint tileXY, Graphics dbf)
 {
  int tilesToLeft = centerTileTopLeft.x <= 0 ? 0 : (int)Math.ceil((double)centerTileTopLeft.x / 256D);
  int tilesToRight = centerTileTopLeft.x + 256 >= width ? 0 : (int)Math.ceil((double)(width - centerTileTopLeft.x - 256) / 256D);
  int tilesToTop = centerTileTopLeft.y <= 0 ? 0 : (int)Math.ceil((double)centerTileTopLeft.y / 256D);
  int tilesToBottom = centerTileTopLeft.y + 256 >= height ? 0 : (int)Math.ceil((double)(height - centerTileTopLeft.y - 256) / 256D);
  int totalMatrixW = tilesToLeft + tilesToRight + 1;
  int totalMatrixH = tilesToTop + tilesToBottom + 1;
  int matrixX = centerTileTopLeft.x - tilesToLeft * 256;
  int matrixY = centerTileTopLeft.y - tilesToTop * 256;
  XYint matrix[] = {
   new XYint((tileXY.x - tilesToLeft) * 256, (tileXY.y - tilesToTop) * 256), new XYint(matrixX, matrixY)
  };
  if (Config.drawWikimapia)
   Wikimapia.drawnKmls.clear();
  for (int i = tileXY.x - tilesToLeft; i < (tileXY.x - tilesToLeft) + totalMatrixW; i++)
  {
   for (int j = tileXY.y - tilesToTop; j < (tileXY.y - tilesToTop) + totalMatrixH; j++)
   {
    Image img = getTileImage(new XYint(i, j), Config.zoom);
    int x = matrixX + ((i - tileXY.x) + tilesToLeft) * 256;
    int y = matrixY + ((j - tileXY.y) + tilesToTop) * 256;
    if (img != null)
    {
     dbf.drawImage(img, x, y, null);
    } else
    {
     dbf.setColor(ColorsAndFonts.clImageNotFound);
     dbf.fillRect(x, y, 256, 256);
    }
    if (Config.drawGrid)
    {
     dbf.setColor(ColorsAndFonts.clGrid);
     dbf.drawRect(x, y, 256, 256);
    }
   }

  }

  if (Config.drawWikimapia)
  {
   for (int i = tileXY.x - tilesToLeft; i < (tileXY.x - tilesToLeft) + totalMatrixW; i++)
   {
    for (int j = tileXY.y - tilesToTop; j < (tileXY.y - tilesToTop) + totalMatrixH; j++)
    {
     ArrayList kmls = Wikimapia.drawTile(new XYint(i, j), dbf, matrix);
     if (kmls != null)
      Wikimapia.drawnKmls.addAll(kmls);
    }

   }

  }
  return matrix;
 }
}
