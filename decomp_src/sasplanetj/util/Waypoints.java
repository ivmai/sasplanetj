// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Waypoints.java

package sasplanetj.util;

import java.awt.*;
import java.io.*;
import java.util.*;
import sasplanetj.App;
import sasplanetj.gps.LatLng;
import sasplanetj.ui.ColorsAndFonts;
import sasplanetj.ui.ShowMessage;

// Referenced classes of package sasplanetj.util:
//   StringUtil, Waypoint, Config, TilesUtil, 
//   XYint

public class Waypoints
{

 public static ArrayList points = new ArrayList();

 public Waypoints()
 {
 }

 public static void load(String filename)
 {
  System.out.println("Waypoints: loading " + filename);
  if (points == null)
   points = new ArrayList();
  try
  {
   FileInputStream fstream = new FileInputStream(filename);
   DataInputStream in = new DataInputStream(fstream);
   BufferedReader br = new BufferedReader(new InputStreamReader(in));
   int lineCount = 0;
   String s;
   while ((s = br.readLine()) != null) 
    if (++lineCount > 4)
    {
     String splits[] = StringUtil.split(s, ",");
     if (splits.length < 4)
     {
      System.out.println("Waypoints: wrong data on line " + lineCount + ": " + s);
     } else
     {
      Waypoint wp = new Waypoint();
      wp.name = splits[1].trim();
      wp.latlng = new LatLng(Double.valueOf(splits[2].trim()).doubleValue(), Double.valueOf(splits[3].trim()).doubleValue());
      points.add(wp);
     }
    }
   in.close();
   System.out.println("Waypoints: now we have " + points.size() + " waypoints loaded");
   if (points.size() == 0)
    points = null;
   App.main.repaint();
  }
  catch (Exception e)
  {
   System.out.println("Waypoints: error reading " + filename);
  }
 }

 public static void draw(Graphics dbf, XYint matrix[])
 {
  Rectangle r;
  for (Iterator it = points.iterator(); it.hasNext(); dbf.drawOval(r.x, r.y, r.width, r.height))
  {
   Waypoint wp = (Waypoint)it.next();
   XYint inmatrix = TilesUtil.coordinateToDisplay(wp.latlng.lat, wp.latlng.lng, Config.zoom);
   inmatrix.subtract(matrix[0]);
   inmatrix.add(matrix[1]);
   r = new Rectangle(inmatrix.x - 3, inmatrix.y - 3, 7, 7);
   dbf.setColor(ColorsAndFonts.clWaypointBrush);
   dbf.fillOval(r.x, r.y, r.width, r.height);
   dbf.drawString(wp.name, inmatrix.x + 5, inmatrix.y + 5);
   dbf.setColor(ColorsAndFonts.clWaypointPen);
  }

 }

 public static void save(String filename)
 {
  if (points == null || points.size() == 0)
   return;
  try
  {
   FileWriter out = new FileWriter(filename, false);
   out.write("OziExplorer CE Waypoint File Version 1.2\r\n");
   out.write("WGS 84\r\n");
   out.write("Reserved 2\r\n");
   out.write("Reserved 3\r\n");
   out.flush();
   for (Iterator it = points.iterator(); it.hasNext(); out.write("\r\n"))
   {
    Waypoint wp = (Waypoint)it.next();
    out.write("-1," + wp.name + "," + wp.latlng.lat + "," + wp.latlng.lng);
   }

   out.close();
   System.out.println("Waypoint: saved " + points.size() + " points to " + filename);
  }
  catch (IOException e)
  {
   new ShowMessage("Failed to write to file " + filename);
   e.printStackTrace();
  }
 }

}
