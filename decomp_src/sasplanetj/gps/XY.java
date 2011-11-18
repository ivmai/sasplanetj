// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   XY.java

package sasplanetj.gps;


public class XY
{

 public double x;
 public double y;

 public XY()
 {
 }

 public XY(double _x, double _y)
 {
  x = _x;
  y = _y;
 }

 public String toString()
 {
  return "[" + x + "," + y + "]";
 }
}
