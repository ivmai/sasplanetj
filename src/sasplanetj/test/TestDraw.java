// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   TestDraw.java

package sasplanetj.test;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintStream;
import java.util.Date;

public class TestDraw extends Frame
 implements MouseListener
{

 public TestDraw()
 {
 }

 public void paint(Graphics g)
 {
  int size = 10000;
  int x[] = new int[size];
  int y[] = new int[size];
  x[1] = y[1] = 100;
  Date start = new Date();
  g.drawPolyline(x, y, x.length);
  Date end = new Date();
  System.out.println("drawn polyline in " + (end.getTime() - start.getTime()));
  start = new Date();
  for (int i = 1; i < y.length; i++)
   g.drawLine(x[i - 1], y[i - 1], x[i], y[i]);

  end = new Date();
  System.out.println("drawn lines in " + (end.getTime() - start.getTime()));
 }

 public static void main(String args[])
 {
  TestDraw t = new TestDraw();
  t.addMouseListener(t);
  t.setSize(300, 300);
  t.setVisible(true);
 }

 public void mouseClicked(MouseEvent e)
 {
  System.exit(0);
 }

 public void mouseEntered(MouseEvent mouseevent)
 {
 }

 public void mouseExited(MouseEvent mouseevent)
 {
 }

 public void mousePressed(MouseEvent mouseevent)
 {
 }

 public void mouseReleased(MouseEvent mouseevent)
 {
 }
}
