// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   GoTo.java

package sasplanetj;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import sasplanetj.gps.LatLng;

// Referenced classes of package sasplanetj:
//   Main, App

public class GoTo extends Dialog
{

 TextField lat;
 TextField lng;

 public GoTo(Frame owner)
 {
  super(owner);
  lat = new TextField(LatLng.latlngFormat7.format(Main.latlng.lat), 4);
  lng = new TextField(LatLng.latlngFormat7.format(Main.latlng.lng), 4);
  setTitle("Go to...");
  setModal(true);
  setSize(220, 150);
  setLocation(owner.getLocation().x + (owner.getSize().width - getSize().width) / 2, owner.getLocation().y + (owner.getSize().height - getSize().height) / 2);
  setLayout(new GridLayout(3, 2, 8, 8));
  add(new Label("Latitude:"));
  add(lat);
  add(new Label("Longitude:"));
  add(lng);
  add(new Button("OK"));
  add(new Button("Cancel"));
  addWindowListener(
   new WindowAdapter()
   {
    public void windowClosing(WindowEvent e)
    {
     dispose();
    }
   });
 }

 public boolean action(Event e, Object o)
 {
  if ((e.target instanceof Button) && ((String)o).equals("OK") ||
      e.target instanceof TextField)
   App.Goto(new LatLng(decodeAsDouble(lat), decodeAsDouble(lng)));
  dispose();
  return true;
 }

 private static double decodeAsDouble(TextField f)
 {
  return Double.valueOf(f.getText().replace(',', '.')).doubleValue();
 }
}
