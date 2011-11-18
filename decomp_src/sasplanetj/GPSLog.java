// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   GPSLog.java

package sasplanetj;

import java.awt.TextArea;
import sasplanetj.gps.GPSListener;
import sasplanetj.gps.LatLng;
import sasplanetj.gps.SerialReader;

// Referenced classes of package sasplanetj:
//   App

public class GPSLog extends TextArea
 implements GPSListener
{

 public GPSLog()
 {
  App.serialReader.addGPSListener(this);
 }

 public void gpsEvent(LatLng gi)
 {
  append(gi.toString() + "\r\n");
 }

 public void removeListener()
 {
  App.serialReader.removeGPSListener(this);
 }
}
