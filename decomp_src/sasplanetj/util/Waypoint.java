// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Waypoint.java

package sasplanetj.util;

import sasplanetj.gps.LatLng;

public class Waypoint
{

 public String name;
 public LatLng latlng;

 public Waypoint()
 {
 }

 public Waypoint(LatLng latlng, String name)
 {
  this.latlng = latlng;
  this.name = name;
 }
}
