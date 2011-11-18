// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   LatLng.java

package sasplanetj.gps;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class LatLng
 implements Cloneable, Serializable
{

 private static final DecimalFormatSymbols decimalSymbols =
  new DecimalFormatSymbols(Locale.US);
 public static final DecimalFormat latlngFormat7 =
  new DecimalFormat("0.0000000", decimalSymbols);
 public static final DecimalFormat latlngFormat4 =
  new DecimalFormat("0.0000", decimalSymbols);
 public double lat;
 public double lng;

 public LatLng()
 {
 }

 public LatLng(double latitude, double longitude)
 {
  lat = normCoord(latitude, 90);
  lng = normCoord(longitude, 180);
 }

 private static double normCoord(double v, double max)
 {
  double res = (v + max) % (max * 2);
  return res >= 0 ? res - max : res + max;
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
  return latlngFormat4.format(lat) + ", " + latlngFormat4.format(lng);
 }

 public boolean equalXY(LatLng other)
 {
  return lat == other.lat && lng == other.lng;
 }

}
