// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   GPSListener.java

package sasplanetj.gps;


// Referenced classes of package sasplanetj.gps:
//   LatLng

public interface GPSListener
{

 public abstract void gpsEvent(LatLng latlng);
}
