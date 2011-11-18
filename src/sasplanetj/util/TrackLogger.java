// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   TrackLogger.java

package sasplanetj.util;

import java.io.*;
import java.text.NumberFormat;
import sasplanetj.App;
import sasplanetj.gps.*;

// Referenced classes of package sasplanetj.util:
//   Config

public class TrackLogger
 implements GPSListener
{

 private final File file = new File(Config.curDir, "track.plt");

 private FileWriter out;
 private int breakTrack = 1;
 private int skipCounter;

 public TrackLogger()
 {
 }

 public boolean deleteFile()
 {
  return file.delete();
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
   System.err.println("TrackLogger: error writing to " + file.getPath());
  }
 }

 public void loggerStart()
 {
  if (!Config.connectGPS)
   return;
  if (out == null)
   try
   {
    if (!file.exists())
    {
     out = new FileWriter(file);
     out.write("OziExplorer Track Point File Version 2.0\r\n");
     out.write("WGS 84\r\n");
     out.write("Altitude is in Feet\r\n");
     out.write("Reserved 3\r\n");
     out.write("0,2,255,sasplanetJ Track Log File,1\r\n");
     out.write("0\r\n");
     out.flush();
    } else
    {
     out = new FileWriter(file, true);
    }
    breakTrack = 1;
    if (App.serialReader != null)
     App.serialReader.addGPSListener(this);
    App.cmiTrackLogSetState();
   }
   catch (IOException e)
   {
    System.err.println("TrackLogger: error opening " + file.getPath());
   }
 }

 public void loggerStop()
 {
  if (out != null)
  {
   if (App.serialReader != null)
    App.serialReader.removeGPSListener(this);
   try
   {
    out.close();
   }
   catch (IOException e)
   {
    System.err.println("TrackLogger: error flushing " + file.getPath());
   }
   out = null;
  }
  App.cmiTrackLogSetState();
 }

}
