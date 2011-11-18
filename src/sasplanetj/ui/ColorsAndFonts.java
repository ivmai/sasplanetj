// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   ColorsAndFonts.java

package sasplanetj.ui;

import java.awt.Color;

public class ColorsAndFonts
{

 public static final Color clImageNotFound = new Color(0xd4927c);
 public static final Color clLatLng = new Color(0xfff60a);
 public static final Color clGrid = new Color(0x7480a2);
 public static final Color clZoomLevel = new Color(0xeed696);
 public static final Color clWikimapia = new Color(0x99ffff55);
 public static final Color clTail;
 public static final Color clTrack = new Color(0xffb800);
 public static final Color clPositionPen = new Color(0xf9f8b0);
 public static final Color clPositionBrush = new Color(0xe52e4c);
 public static final Color clWaypointPen = new Color(0);
 public static final Color clWaypointBrush;
 public static final Color clWaypointFont;

 public ColorsAndFonts()
 {
 }

 static 
 {
  clTail = Color.cyan;
  clWaypointBrush = new Color(0xb0fb25);
  clWaypointFont = clWaypointBrush;
 }
}
