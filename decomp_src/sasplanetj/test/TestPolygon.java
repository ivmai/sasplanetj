// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   TestPolygon.java

package sasplanetj.test;

import java.awt.*;
import java.io.PrintStream;

public class TestPolygon
{

 public TestPolygon()
 {
 }

 public static void main(String args[])
 {
  Polygon poly = new Polygon();
  poly.addPoint(12, 206);
  poly.addPoint(4, 206);
  poly.addPoint(4, 213);
  poly.addPoint(12, 213);
  poly.addPoint(12, 206);
  System.out.println("Contacins=" + poly.getBounds().contains(new Point(8, 209)));
 }
}
