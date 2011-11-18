// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   NewWaypointDialog.java

package sasplanetj.ui;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import sasplanetj.App;
import sasplanetj.gps.LatLng;
import sasplanetj.util.Waypoints;

public class NewWaypointDialog extends Dialog
{

 TextField lat;
 TextField lng;
 TextField name;

 public NewWaypointDialog(Frame owner, LatLng latlng)
 {
  super(owner);
  lat = new TextField();
  lng = new TextField();
  name = new TextField();
  setTitle("Create waypoint");
  setModal(true);
  setSize(220, 180);
  setLocation(owner.getLocation().x + (owner.getSize().width - getSize().width) / 2, owner.getLocation().y + (owner.getSize().height - getSize().height) / 2);
  setLayout(new GridLayout(4, 2, 8, 8));
  add(new Label("Latitude:"));
  lat.setText(LatLng.latlngFormat7.format(latlng.lat));
  add(lat);
  add(new Label("Longitude:"));
  lng.setText(LatLng.latlngFormat7.format(latlng.lng));
  add(lng);
  add(new Label("Waypoint name:"));
  name.setText("wp" + (Waypoints.points != null ? Waypoints.points.size() + 1 : "1"));
  add(name);
  add(new Button("OK"));
  add(new Button("Cancel"));
 }

 public boolean action(Event e, Object o)
 {
  if ((e.target instanceof Button) && ((String)o).equals("OK"))
  {
   LatLng latlng = new LatLng();
   latlng.lat = Double.valueOf(lat.getText()).doubleValue();
   latlng.lng = Double.valueOf(lng.getText()).doubleValue();
   App.CreateWaypoint(latlng, name.getText());
  }
  dispose();
  return true;
 }
}
