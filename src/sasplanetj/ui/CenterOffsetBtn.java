// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   CenterOffsetBtn.java

package sasplanetj.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import sasplanetj.App;
import sasplanetj.Main;

public class CenterOffsetBtn extends Button
{

 private static final Dimension size = new Dimension(25, 25);
 private final Image img = loadImageFromFile("offset.png");

 public CenterOffsetBtn()
 {
  if (img == null)
   return;
  size.setSize(img.getWidth(null), img.getHeight(null));
  setAllSizes();
  addMouseListener(new MouseAdapter() {

   public void mouseClicked(MouseEvent e)
   {
    App.main.viewOffset0();
   }

  }
);
 }

 private void setAllSizes()
 {
  setSize(size);
 }

 private Image loadImageFromFile(String fname)
 {
  try
  {
   java.net.URL url = sasplanetj.ui.CenterOffsetBtn.class.getResource(fname);
   if (url != null)
   {
    Image img = getToolkit().getImage(url);
    MediaTracker tracker = new MediaTracker(this);
    tracker.addImage(img, 0);
    tracker.waitForAll();
    return img;
   }
  }
  catch (Exception e) {}
  System.err.println("Cannot open image: " + fname);
  return null;
 }

 public void repaint(Graphics g)
 {
  paint(g);
 }

 public void paint(Graphics g)
 {
  if (img != null)
   g.drawImage(img, 0, 0, this);
 }

 public boolean isImageLoaded()
 {
  return img != null;
 }
}
