// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   TrackLogger.java

package sasplanetj.util;

import java.awt.CheckboxMenuItem;
import java.io.*;
import java.text.NumberFormat;
import sasplanetj.App;
import sasplanetj.gps.*;

// Referenced classes of package sasplanetj.util:
//   Config

public class TrackLogger
 implements GPSListener
{

 public static final TrackLogger self = new TrackLogger();
 public static final String logFilename = "track.plt";
 public static final File f = new File("track.plt");
 public static FileWriter out;
 public static int breakTrack = 1;
 private static int skipCounter = 0;

 public TrackLogger()
 {
 }

 public void gpsEvent(LatLng latlng)
 {
  if (skipCounter == Config.trackLogSkip)
  {
   skipCounter = 0;
  } else
  {
   skipCounter++;
   return;
  }
  try
  {
   out.write(LatLng.latlngFormat7.format(latlng.lat) + ", " + LatLng.latlngFormat7.format(latlng.lng) + ", " + breakTrack + "\r\n");
   breakTrack = 0;
   out.flush();
  }
  catch (IOException e)
  {
   System.out.println("TrackLogger: error writing to track.plt");
  }
 }

 public static void loggerStart()
 {
  if (!Config.connectGPS)
   return;
  if (out == null)
   try
   {
    if (!f.exists())
    {
     out = new FileWriter("track.plt", true);
     out.write("OziExplorer Track Point File Version 2.0\r\n");
     out.write("WGS 84\r\n");
     out.write("Altitude is in Feet\r\n");
     out.write("Reserved 3\r\n");
     out.write("0,2,255,sasplanetJ Track Log File,1\r\n");
     out.write("0\r\n");
     out.flush();
    } else
    {
     out = new FileWriter("track.plt", true);
    }
    breakTrack = 1;
    App.serialReader.addGPSListener(self);
    App.cmiTrackLog.setState(Config.trackLog);
   }
   catch (IOException e)
   {
    System.out.println("TrackLogger: error opening track.plt");
   }
 }

 public static void loggerStop()
 {
  if (out != null)
  {
   App.serialReader.removeGPSListener(self);
   try
   {
    out.flush();
    out.close();
   }
   catch (IOException e)
   {
    System.out.println("TrackLogger: error flushing track.plt");
   }
   out = null;
  }
  App.cmiTrackLog.setState(Config.trackLog);
 }

}
