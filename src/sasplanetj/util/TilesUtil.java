// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   TilesUtil.java

package sasplanetj.util;

import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import sasplanetj.App;
import sasplanetj.gps.LatLng;
import sasplanetj.ui.ColorsAndFonts;

// Referenced classes of package sasplanetj.util:
//   XYint, Config, StringUtil, Cache, 
//   Zip, Wikimapia

public class TilesUtil
{

 private static class ImageContainer
 {
  private final Image img;

  ImageContainer(Image image)
  {
   img = image;
  }

  Image getImage()
  {
   return img;
  }

  protected void finalize()
  {
   if (img != null)
    img.flush();
  }
 }

 public static final int ZOOM_MIN = 1;
 public static final int ZOOM_MAX = 23;
 
 public static final int LOG2_TILESIZE = 8;
 private static final int TILESIZE = 1 << LOG2_TILESIZE;
 
 private static final double exct = 0.081819699999999995;
 
 public static Cache tilesCache;
 public static int lastWikiEndX;
 private static boolean noMathSinH;
 public static final Cache zipExistanceCache = new Cache(200);

 static
 {
  try
  {
   Math.sinh(0);
  }
  catch (NoSuchMethodError e)
  {
   System.out.println("No Math.sinh()");
   noMathSinH = true;
  }
 }

 private TilesUtil() {}

 private static double atanh(double x)
 {
  return 0.5D * Math.log((1.0D + x) / (1.0D - x));
 }

 public static XYint coordinateToDisplay(double lat, double lng, int zoom,
   boolean isYandex)
 {
  double quarterPixelsAtZoom = (double)(1 << (zoom + (LOG2_TILESIZE - 2)));
  double projection;
  if (isYandex)
  {
   double z = Math.sin(Math.toRadians(lat));
   projection = atanh(z) - exct * atanh(exct * z);
  }
  else
   projection = Math.log(Math.tan(Math.toRadians(lat) / 2.0 + Math.PI / 4.0));
  return new XYint((int)((lng / 180.0 + 1.0) * quarterPixelsAtZoom),
          (int)((1.0 - projection / Math.PI) * quarterPixelsAtZoom));
 }

 public static XYint getTileByDisplayCoord(XYint p)
 {
  return new XYint(p.x >> LOG2_TILESIZE, p.y >> LOG2_TILESIZE);
 }

 public static int adjustTileX(int x, int zoom)
 {
  int max = 1 << (zoom - 1);
  return (x %= max) >= 0 ? x : max + x;
 }

 private static int maxTileY(int zoom, boolean isYandex)
 {
  return (int)(1.15 * (1 << (zoom - 1)));
 }

 public static int adjustViewOfsX(int x, int zoom)
 {
  int max = 1 << (zoom + (LOG2_TILESIZE - 2));
  int res = (x + max) % (max << 1);
  return res >= 0 ? res - max : res + max;
 }

 private static double emulate_expm1(double a)
 {
  double v = Math.exp(a) - 1.0;
  return v != 0.0 ? v : a;
 }

 private static double emulate_sinh(double a)
 {
  boolean isneg = false;
  if (a < 0.0)
  {
   isneg = true;
   a = -a;
  }
  double v = emulate_expm1(a);
  if (v < Double.POSITIVE_INFINITY)
   v = (v / (v + 1.0) + v) * 0.5;
   else
   {
    v = Math.exp(a * 0.5);
    v = (v * 0.5) * v;
   }
  return isneg ? -v : v;
 }

 public static LatLng displayToCoordinate(XYint point, int zoom,
   boolean isYandex)
 {
  double quarterPixelsAtZoom = (double)(1 << (zoom + (LOG2_TILESIZE - 2)));
  double lng = (point.x / quarterPixelsAtZoom - 1.0) * 180.0;
  if (isYandex)
  {
   double yy = (point.y - quarterPixelsAtZoom) * 2.0;
   boolean isLatNeg = false;
   if (yy > 0)
   {
    isLatNeg = true;
    yy = -yy;
   }
   double latRad = Math.atan(Math.exp(yy / (quarterPixelsAtZoom *
                    (-2.0 / Math.PI)))) * 2.0 - Math.PI / 2.0;
   double k = -Math.PI / quarterPixelsAtZoom;
   double prev;
   int i = 1000;
   do
   {
     prev = latRad;
     double sinPrev = Math.sin(prev);
     latRad = Math.asin(1.0 - (sinPrev + 1.0) * Math.pow(1.0 - exct * sinPrev,
               exct) / (Math.exp(yy * k) * Math.pow(1.0 + exct * sinPrev,
               exct)));
   } while (Math.abs(prev - latRad) > 1e-9 && --i > 0);
   if (i >= 0 && !Double.isNaN(latRad))
    return new LatLng(Math.toDegrees(isLatNeg ? -latRad : latRad), lng);
   System.out.println("Failed to calculate lattitude for y=" + point.y +
    ", zoom=" + zoom);
  }
  double projection = (1.0 - point.y / quarterPixelsAtZoom) * Math.PI;
  return new LatLng(Math.toDegrees(Math.atan(noMathSinH ?
          emulate_sinh(projection) : Math.sinh(projection))), lng);
 }

 private static String getCachePath(int x, int y, int zoom)
 {
  return Config.cachePath + StringUtil.fileSep + Config.curMapDir + StringUtil.fileSep + "z" + Config.zoom + StringUtil.fileSep + x / 1024 + StringUtil.fileSep + "x" + x + StringUtil.fileSep + y / 1024 + StringUtil.fileSep + "y" + y + "." + Config.curMapExt;
 }

 private static ImageContainer getTileImage(int x, int y, int zoom)
 {
  if (zoom < Config.curMapMinZoom())
   return null;
  String filename = getCachePath(x, y, zoom);
  ImageContainer img = (ImageContainer)tilesCache.get(filename);
  Image image;
  if (img == null && (image = loadImage(filename)) != null)
  {
   img = new ImageContainer(image);
   tilesCache.put(filename, img, Config.useSoftRefs);
  }
  return img;
 }

 private static Image loadImage(String filename)
 {
  String zipnames[] = checkZipFileExistance(filename);
  if (zipnames != null)
  {
   byte binary[] = Zip.getZipFile(zipnames[0], zipnames[1]);
   if (binary == null)
    return loadImageFromFS(filename);
   Image img = Toolkit.getDefaultToolkit().createImage(binary);
   return waitForImageLoaded(img) ? img : null;
  } else
  {
   return loadImageFromFS(filename);
  }
 }

 private static boolean waitForImageLoaded(Image img)
 {
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
  if (img.getWidth(null) == TILESIZE && img.getHeight(null) == TILESIZE &&
      (mt.statusAll(false) & MediaTracker.COMPLETE) != 0)
   return true;
  img.flush();
  return false;
 }

 public static String[] checkZipFileExistance(String filename)
 {
  String zipnames[] = (String[])zipExistanceCache.get(filename);
  if (zipnames != null)
   return zipnames;
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

    zipnames = new String[] { zipname, fileinzip };
    zipExistanceCache.put(filename, zipnames, false);
    return zipnames;
   }

  return null;
 }

 private static Image loadImageFromFS(String filename)
 {
  if (!App.useAwtWorkaround() || (new File(filename)).isFile())
  {
   Image img = Toolkit.getDefaultToolkit().getImage(filename);
   if (waitForImageLoaded(img))
    return img;
  }
  System.out.println("Cannot load map tile: " + filename);
  return null;
 }

 public static XYint[] drawTilesArea(int width, int height,
   XYint centerTileTopLeft, XYint tileXY, XYint tileWikiXY, Graphics dbf)
 {
  int tilesToLeft = centerTileTopLeft.x <= 0 ? 0 :
                     (centerTileTopLeft.x + (TILESIZE - 1)) >> LOG2_TILESIZE;
  int tilesToRight = centerTileTopLeft.x + TILESIZE >= width ? 0 :
                      (width - centerTileTopLeft.x - 1) >> LOG2_TILESIZE;
  int tilesToTop = centerTileTopLeft.y <= 0 ? 0 :
                    (centerTileTopLeft.y + (TILESIZE - 1)) >> LOG2_TILESIZE;
  int tilesToBottom = centerTileTopLeft.y + TILESIZE >= height ? 0 :
                       (height - centerTileTopLeft.y - 1) >> LOG2_TILESIZE;
  int totalMatrixW = tilesToLeft + tilesToRight + 1;
  int totalMatrixH = tilesToTop + tilesToBottom + 1;
  int matrixX = centerTileTopLeft.x - (tilesToLeft << LOG2_TILESIZE);
  int matrixY = centerTileTopLeft.y - (tilesToTop << LOG2_TILESIZE);
  XYint matrix[] = {
   new XYint((tileXY.x - tilesToLeft) << LOG2_TILESIZE,
    (tileXY.y - tilesToTop) << LOG2_TILESIZE),
   new XYint(matrixX, matrixY)
  };
  Wikimapia.clearDrawnKmls();
  int zoom = Config.zoom;
  int maxY = maxTileY(zoom, Config.isMapYandex);
  int startY = tileXY.y - tilesToTop;
  for (int i = tileXY.x - tilesToLeft; i < (tileXY.x - tilesToLeft) + totalMatrixW; i++)
  {
   for (int j = startY; j < startY + totalMatrixH; j++)
   {
    ImageContainer img = null;
    if (j >= 0 && j < maxY)
     img = getTileImage(adjustTileX(i, zoom), j, zoom);
    int x = matrixX + (((i - tileXY.x) + tilesToLeft) << LOG2_TILESIZE);
    int y = matrixY + (((j - tileXY.y) + tilesToTop) << LOG2_TILESIZE);
    if (img != null)
    {
     dbf.drawImage(img.getImage(), x, y, null);
    } else
    {
     dbf.setColor(ColorsAndFonts.clImageNotFound);
     dbf.fillRect(x, y, TILESIZE, TILESIZE);
    }
    if (Config.drawGrid)
    {
     dbf.setColor(ColorsAndFonts.clGrid);
     dbf.drawRect(x, y, TILESIZE, TILESIZE);
    }
   }

  }

  if (Config.drawWikimapia)
  {
   startY = tileWikiXY.y - tilesToTop;
   int endY = Math.min(startY + totalMatrixH,
               Config.isMapYandex ? maxTileY(zoom, false) : maxY);
   if (startY < 0)
    startY = 0;
   int startX = tileWikiXY.x - tilesToLeft;
   for (int i = startX; i < startX + totalMatrixW; i++)
   {
    for (int j = startY; j < endY; j++)
     Wikimapia.drawTile(i, j, zoom, dbf, matrix);
   }
   lastWikiEndX = matrixX + (totalMatrixW << LOG2_TILESIZE);
  }
  return matrix;
 }
}
