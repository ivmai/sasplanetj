// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Tracks.java

package sasplanetj.util;

import java.awt.Component;
import java.awt.Graphics;
import java.io.*;
import java.util.*;
import sasplanetj.App;
import sasplanetj.gps.XY;
import sasplanetj.ui.ColorsAndFonts;

// Referenced classes of package sasplanetj.util:
//   StringUtil, Config, TilesUtil, XYint

public class Tracks
{

 public static final Tracks self = new Tracks();
 public static ArrayList tracks = null;

 public Tracks()
 {
 }

 public static void load(String filename)
 {
  System.out.println("Tracks: loading " + filename);
  if (tracks == null)
   tracks = new ArrayList();
  try
  {
   BufferedReader br = new BufferedReader(new FileReader(filename));
   Wikimapia.StopWatch watch = new Wikimapia.StopWatch();
   watch.start();
   int lineCount = 0;
   ArrayList track = null;
   String s;
   while ((s = br.readLine()) != null) 
    if (++lineCount > 6)
    {
     String splits[] = StringUtil.split(s, ",");
     if (splits.length < 3)
     {
      System.out.println("Tracks: wrong data on line " + lineCount + ": " + s);
     } else
     {
      int newTrack = Integer.valueOf(splits[2].trim()).intValue();
      if (newTrack == 1)
      {
       addTrack(track);
       track = new ArrayList(200);
      }
      if (track == null)
      {
       System.out.println("Tracks: WARNING: No starting point in track");
       track = new ArrayList(200);
      }
      if (track.size() % 200 == 0)
       track.ensureCapacity(track.size() + 200);
      track.add(new XY(Double.valueOf(splits[0].trim()).doubleValue(), Double.valueOf(splits[1].trim()).doubleValue()));
     }
    }
   br.close();
   addTrack(track);
   if (tracks.size() == 0)
   {
    System.out.println("Tracks: no tracks found in file");
    tracks = null;
   } else
   {
    System.out.println("Tracks: now we have " + tracks.size() +
     " tracks loaded in " + watch.currentMillis() + "ms");
   }
   App.main.repaint();
  }
  catch (Exception e)
  {
   e.printStackTrace();
   System.err.println("Tracks: error reading " + filename);
  }
 }

 private static void addTrack(ArrayList track)
 {
  if (track != null)
  {
   if (track.size() < 2)
   {
    System.out.println("Tracks: WARNING: track size is < 2, skiping it");
    return;
   }
   XY array[] = new XY[track.size()];
   track.toArray(array);
   tracks.add(array);
   System.out.println("Tracks: track " + tracks.size() + " has " + ((XY[])tracks.get(tracks.size() - 1)).length + " points");
  }
 }

 public static void draw(Graphics dbf, XYint matrix[])
 {
  dbf.setColor(ColorsAndFonts.clTrack);
  XY track[];
  int x[];
  int y[];
  for (Iterator tr = tracks.iterator(); tr.hasNext(); dbf.drawPolyline(x, y, track.length))
  {
   track = (XY[])tr.next();
   x = new int[track.length];
   y = new int[track.length];
   for (int i = 0; i < track.length; i++)
   {
    XY latlng = track[i];
    XYint inmatrix = TilesUtil.coordinateToDisplay(latlng.x, latlng.y,
                      Config.zoom, Config.isMapYandex);
    inmatrix.subtract(matrix[0]);
    inmatrix.add(matrix[1]);
    x[i] = inmatrix.x;
    y[i] = inmatrix.y;
   }

  }

 }

}
