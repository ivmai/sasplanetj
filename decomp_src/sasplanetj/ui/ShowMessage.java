// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   ShowMessage.java

package sasplanetj.ui;

import java.awt.*;
import sasplanetj.App;

public class ShowMessage extends Dialog
{

 private Frame owner;

 public ShowMessage(String msg)
 {
  super(App.getSelf());
  owner = App.getSelf();
  setTitle(owner.getTitle());
  setSize(220, 220);
  setLocation(owner.getLocation().x + (owner.getSize().width - getSize().width) / 2, owner.getLocation().y + (owner.getSize().height - getSize().height) / 2);
  setLayout(new BorderLayout(8, 8));
  TextArea msgObj = new TextArea(msg);
  add(msgObj, "Center");
  add(new Button("OK"), "South");
  setVisible(true);
 }

 public boolean action(Event e, Object o)
 {
  dispose();
  return true;
 }
}
