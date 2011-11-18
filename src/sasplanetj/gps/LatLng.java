// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   LatLng.java

package sasplanetj.gps;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class LatLng
 implements Cloneable, Serializable
{

 public static final DecimalFormat latlngFormat7 = new DecimalFormat("#.0000000");
 public static final DecimalFormat latlngFormat4 = new DecimalFormat("#.0000");
 public double lat;
 public double lng;

 public LatLng()
 {
 }

 public LatLng(double latitude, double longitude)
 {
  lat = latitude;
  lng = longitude;
 }

 public void copyTo(LatLng other)
 {
  other.lat = lat;
  other.lng = lng;
 }

 public String toString()
 {
  return "Lat " + latlngFormat7.format(lat) + " Lng " + latlngFormat7.format(lng);
 }

 public String toShortString()
 {
  return latlngFormat4.format(lat) + " " + latlngFormat4.format(lng);
 }

 public boolean equalXY(LatLng other)
 {
  return lat == other.lat && lng == other.lng;
 }

}
