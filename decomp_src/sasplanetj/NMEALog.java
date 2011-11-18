// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   NMEALog.java

package sasplanetj;

import java.awt.TextArea;
import sasplanetj.gps.GPSListenerNMEA;
import sasplanetj.gps.SerialReader;

// Referenced classes of package sasplanetj:
//   App

public class NMEALog extends TextArea
 implements GPSListenerNMEA
{

 public NMEALog()
 {
  App.serialReader.addNMEAListener(this);
 }

 public void gpsEventNMEA(String msg)
 {
  append(msg + "\n");
 }

 public void removeListener()
 {
  App.serialReader.removeNMEAListener(this);
 }
}
