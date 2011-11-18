// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   TrackTail.java

package sasplanetj.util;

import java.awt.Graphics;
import java.util.*;
import sasplanetj.gps.XY;
import sasplanetj.ui.ColorsAndFonts;

// Referenced classes of package sasplanetj.util:
//   Config, TilesUtil, XYint

public class TrackTail extends LinkedList
{

 int MAX_ENTRIES;

 public TrackTail(int maxsize)
 {
  MAX_ENTRIES = maxsize;
 }

 public void addPoint(XY point)
 {
  if (size() >= MAX_ENTRIES)
   removeFirst();
  addLast(point);
 }

 public String getPointsString()
 {
  String result = "";
  for (Iterator it = iterator(); it.hasNext();)
   result = result + ((XY)it.next()).toString() + " ";

  return result;
 }

 public void draw(Graphics dbf, XYint matrix[])
 {
  if (size() < 2)
   return;
  dbf.setColor(ColorsAndFonts.clTail);
  int x[] = new int[size()];
  int y[] = new int[size()];
  int i = 0;
  for (Iterator it = iterator(); it.hasNext();)
  {
   XY latlng = (XY)it.next();
   XYint inmatrix = TilesUtil.coordinateToDisplay(latlng.x, latlng.y, Config.zoom);
   inmatrix.subtract(matrix[0]);
   inmatrix.add(matrix[1]);
   x[i] = inmatrix.x;
   y[i] = inmatrix.y;
   i++;
  }

  dbf.drawPolyline(x, y, x.length);
 }
}
