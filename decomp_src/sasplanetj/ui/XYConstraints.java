// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   XYConstraints.java

package sasplanetj.ui;

import java.awt.Rectangle;
import java.io.Serializable;

public class XYConstraints
 implements Cloneable, Serializable
{

 int x;
 int y;
 int width;
 int height;

 public XYConstraints()
 {
  this(0, 0, 0, 0);
 }

 public XYConstraints(Rectangle r)
 {
  this(r.x, r.y, r.width, r.height);
 }

 public XYConstraints(int x, int y, int width, int height)
 {
  this.x = x;
  this.y = y;
  this.width = width;
  this.height = height;
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

 public int getWidth()
 {
  return width;
 }

 public void setWidth(int width)
 {
  this.width = width;
 }

 public int getHeight()
 {
  return height;
 }

 public void setHeight(int height)
 {
  this.height = height;
 }

 public int hashCode()
 {
  return x ^ y * 37 ^ width * 43 ^ height * 47;
 }

 public boolean equals(Object that)
 {
  if (that instanceof XYConstraints)
  {
   XYConstraints other = (XYConstraints)that;
   return other.x == x && other.y == y && other.width == width && other.height == height;
  } else
  {
   return false;
  }
 }

 public Object clone()
 {
  return new XYConstraints(x, y, width, height);
 }

 public String toString()
 {
  return "XYConstraints[" + x + "," + y + "," + width + "," + height + "]";
 }
}
