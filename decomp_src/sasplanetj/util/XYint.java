// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   XYint.java

package sasplanetj.util;


public class XYint
{

 public int x;
 public int y;

 public XYint()
 {
 }

 public XYint(int x, int y)
 {
  this.x = x;
  this.y = y;
 }

 public XYint(XYint other)
 {
  x = other.x;
  y = other.y;
 }

 public void setLocation(int x, int y)
 {
  this.x = x;
  this.y = y;
 }

 public boolean equalXY(XYint xy)
 {
  return x == xy.x && y == xy.y;
 }

 public int getX()
 {
  return x;
 }

 public void setX(int x)
 {
  this.x = x;
 }

 public int getY()
 {
  return y;
 }

 public void setY(int y)
 {
  this.y = y;
 }

 public String toString()
 {
  return "[" + x + "," + y + "]";
 }

 public XYint getDifference(XYint other)
 {
  return new XYint(x - other.x, y - other.y);
 }

 public void add(XYint other)
 {
  setLocation(x + other.x, y + other.y);
 }

 public void subtract(XYint other)
 {
  setLocation(x - other.x, y - other.y);
 }

 public void multiply(double m)
 {
  setLocation((int)((double)x * m), (int)((double)y * m));
 }
}
