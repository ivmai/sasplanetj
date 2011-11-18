// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   TestMenu.java

package sasplanetj.test;

import java.awt.*;

public class TestMenu extends Frame
{

 public CheckboxMenuItem cmi;

 public TestMenu()
 {
  setSize(200, 200);
  MenuBar menuBar = new MenuBar();
  Menu menu = new Menu("Opts");
  cmi = new CheckboxMenuItem("Draw grid", true);
  menu.add(cmi);
  menuBar.add(menu);
  cmi.setState(true);
  setMenuBar(menuBar);
  cmi.setState(true);
 }

 public static void main(String args[])
 {
  TestMenu t = new TestMenu();
  t.validate();
  t.setVisible(true);
 }
}
