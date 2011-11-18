// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Main.java

package sasplanetj;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.util.*;
import sasplanetj.gps.GPSListener;
import sasplanetj.gps.LatLng;
import sasplanetj.gps.SerialReader;
import sasplanetj.gps.XY;
import sasplanetj.ui.CenterOffsetBtn;
import sasplanetj.ui.ColorsAndFonts;
import sasplanetj.ui.NewWaypointDialog;
import sasplanetj.ui.ShowMessage;
import sasplanetj.ui.XYConstraints;
import sasplanetj.ui.XYLayout;
import sasplanetj.util.Config;
import sasplanetj.util.TilesUtil;
import sasplanetj.util.TrackTail;
import sasplanetj.util.Tracks;
import sasplanetj.util.Waypoints;
import sasplanetj.util.Wikimapia;
import sasplanetj.util.XYint;

// Referenced classes of package sasplanetj:
//   App

public class Main extends Panel
 implements GPSListener, MouseListener, MouseMotionListener, KeyListener, ActionListener
{

 public static LatLng latlng = new LatLng();
 private static LatLng clickLatlng;
 public static TrackTail trackTail;
 public static final XYint viewOffset = new XYint(0, 0);
 public static int viewOffsetStep = 50;
 public static final CenterOffsetBtn offsetBtn = new CenterOffsetBtn();
 public static final PopupMenu popup = new PopupMenu();
 public static final ArrayList popupWiki = new ArrayList();
 Graphics dbf;
 Image offscreen;
 private static int skipCounter = 0;
 private static final XYint mouseDragPrevXY = new XYint();

 public Main()
 {
  popup.addActionListener(this);
  add(popup);
  offsetBtn.setVisible(false);
  trackTail = new TrackTail(Config.trackTailSize);
  setLayout(new XYLayout());
  addMouseListener(this);
  addMouseMotionListener(this);
  addKeyListener(this);
  addComponentListener(new ComponentAdapter() {

   public void componentHidden(ComponentEvent e)
   {
    System.out.println("Hidden");
   }

   public void componentShown(ComponentEvent e)
   {
    System.out.println("Shown");
   }

   public void componentMoved(ComponentEvent componentevent)
   {
   }

   public void componentResized(ComponentEvent e)
   {
    initDbf();
    if (Main.offsetBtn.isVisible())
    {
     remove(Main.offsetBtn);
     add(Main.offsetBtn, new XYConstraints(getSize().width - Main.offsetBtn.getSize().width - 5, getSize().height - Main.offsetBtn.getSize().height - 5, Main.offsetBtn.getSize().width, Main.offsetBtn.getSize().height));
    }
   }

  }
);
 }

 public void initDbf()
 {
  System.out.println("Main size " + getSize().width + "x" + getSize().height);
  if (getSize().width <= 0 || getSize().height <= 0)
   return;
  if (offscreen != null && offscreen.getWidth(null) == getWidth() && offscreen.getHeight(null) == getHeight())
  {
   return;
  } else
  {
   offscreen = createImage(getSize().width, getSize().height);
   dbf = offscreen.getGraphics();
   return;
  }
 }

 public Dimension getPreferredSize()
 {
  return getSize();
 }

 public void update(Graphics g)
 {
  paint(g);
 }

 public void paint(Graphics g)
 {
  if (offscreen == null)
  {
   System.out.println("offscreen==null");
   return;
  }
  XYint center = new XYint(offscreen.getWidth(null) / 2, offscreen.getHeight(null) / 2);
  XYint displayXY = TilesUtil.coordinateToDisplay(latlng.lat, latlng.lng, Config.zoom);
  if (Config.drawTail && latlng.lat != 0.0D && latlng.lng != 0.0D)
   trackTail.addPoint(new XY(latlng.lat, latlng.lng));
  displayXY.subtract(viewOffset);
  XYint tileXY = TilesUtil.getTileByDisplayCoord(displayXY);
  XYint intile = new XYint(displayXY.x % 256, displayXY.y % 256);
  XYint centerTileTopLeft = new XYint(center.x - intile.x, center.y - intile.y);
  XYint matrix[] = TilesUtil.drawTilesArea(offscreen.getWidth(null), offscreen.getHeight(null), centerTileTopLeft, tileXY, dbf);
  if (Config.drawLatLng)
  {
   dbf.setColor(ColorsAndFonts.clLatLng);
   dbf.drawString(latlng.toString(), 3, 15);
  }
  if (Config.drawTail)
   trackTail.draw(dbf, matrix);
  if (Tracks.tracks != null)
   Tracks.draw(dbf, matrix);
  if (Waypoints.points != null)
   Waypoints.draw(dbf, matrix);
  drawPosition(dbf, center);
  g.drawImage(offscreen, 0, 0, this);
 }

 private void drawPosition(Graphics g, XYint position)
 {
  Rectangle r = new Rectangle((viewOffset.x + position.x) - 3, (viewOffset.y + position.y) - 3, 7, 7);
  g.setColor(ColorsAndFonts.clPositionBrush);
  g.fillOval(r.x, r.y, r.width, r.height);
  g.setColor(ColorsAndFonts.clPositionPen);
  g.drawOval(r.x, r.y, r.width, r.height);
 }

 public void gpsEvent(LatLng gi)
 {
  if (skipCounter == Config.drawMapSkip)
  {
   skipCounter = 0;
  } else
  {
   skipCounter++;
   return;
  }
  latlng = gi;
  repaint();
 }

 public void registerListener()
 {
  App.serialReader.addGPSListener(this);
 }

 public void removeListener()
 {
  App.serialReader.removeGPSListener(this);
 }

 public void mouseClicked(MouseEvent mouseevent)
 {
 }

 private boolean doPopup(MouseEvent e)
  throws HeadlessException
 {
  boolean isPopup;
  if (System.getProperty("java.vm.name").startsWith("CrE-ME"))
   isPopup = true;
  else
   isPopup = e.isPopupTrigger() || e.getButton() == 2 || e.getButton() == 3;
  if (isPopup)
  {
   popup.removeAll();
   XYint displayXY = TilesUtil.coordinateToDisplay(latlng.lat, latlng.lng, Config.zoom);
   displayXY.subtract(viewOffset);
   XYint clickOffset = new XYint(e.getPoint().x - getSize().width / 2, e.getPoint().y - getSize().height / 2);
   displayXY.add(clickOffset);
   clickLatlng = TilesUtil.displayToCoordinate(displayXY, Config.zoom);
   MenuItem mi = new MenuItem(clickLatlng.toShortString());
   popup.add(mi);
   mi = new MenuItem("Go here");
   mi.setActionCommand("GO_HERE");
   popup.add(mi);
   mi = new MenuItem("Create waypoint");
   mi.setActionCommand("CREATE_WAYPOINT");
   popup.add(mi);
   if (Config.drawWikimapia)
   {
    popupWiki.clear();
    int i = 0;
    for (Iterator iterator = Wikimapia.drawnKmls.iterator(); iterator.hasNext();)
    {
     sasplanetj.util.Wikimapia.KML kml = (sasplanetj.util.Wikimapia.KML)iterator.next();
     if (kml.drawnPoly.contains(e.getPoint()))
     {
      mi = new MenuItem(kml.strip());
      mi.setActionCommand("POPUP_WIKI" + i);
      popupWiki.add(mi);
      i++;
     }
    }

    if (popupWiki.size() > 0)
     popup.addSeparator();
    for (i = 0; i < popupWiki.size(); i++)
     popup.add((MenuItem)popupWiki.get(i));

   }
   popup.show(this, e.getX(), e.getY());
  }
  return isPopup;
 }

 public void mouseEntered(MouseEvent mouseevent)
 {
 }

 public void mouseExited(MouseEvent mouseevent)
 {
 }

 public void mousePressed(MouseEvent e)
 {
  doPopup(e);
  mouseDragPrevXY.setLocation(e.getX(), e.getY());
 }

 public void mouseReleased(MouseEvent mouseevent)
 {
 }

 public void mouseDragged(MouseEvent e)
 {
  XYint deltadrag = new XYint(e.getX() - mouseDragPrevXY.x, e.getY() - mouseDragPrevXY.y);
  viewOffset.add(deltadrag);
  viewOffsetChanged();
  mouseDragPrevXY.setLocation(e.getX(), e.getY());
 }

 public void mouseMoved(MouseEvent mouseevent)
 {
 }

 public void keyPressed(KeyEvent keyevent)
 {
 }

 public void keyReleased(KeyEvent e)
 {
  switch (e.getKeyCode())
  {
  case 113: // 'q'
   App.zoomOut();
   break;

  case 112: // 'p'
   App.zoomIn();
   break;

  case 38: // '&'
   viewOffset.y += viewOffsetStep;
   viewOffsetChanged();
   break;

  case 39: // '\''
   viewOffset.x -= viewOffsetStep;
   viewOffsetChanged();
   break;

  case 40: // '('
   viewOffset.y -= viewOffsetStep;
   viewOffsetChanged();
   break;

  case 37: // '%'
   viewOffset.x += viewOffsetStep;
   viewOffsetChanged();
   break;

  case 10: // '\n'
   viewOffset0();
   break;
  }
 }

 public void viewOffsetChanged()
 {
  if (viewOffset.x != 0 || viewOffset.y != 0)
  {
   boolean found = false;
   for (int i = 0; i < getComponentCount(); i++)
   {
    if (getComponent(i) != offsetBtn)
     continue;
    found = true;
    break;
   }

   if (!found)
   {
    add(offsetBtn, new XYConstraints(getSize().width - offsetBtn.getSize().width - 5, getSize().height - offsetBtn.getSize().height - 5, offsetBtn.getSize().width, offsetBtn.getSize().height));
    offsetBtn.setVisible(true);
   }
  } else
  {
   boolean found = false;
   for (int i = 0; i < getComponentCount(); i++)
   {
    if (getComponent(i) != offsetBtn)
     continue;
    found = true;
    break;
   }

   if (found)
   {
    remove(offsetBtn);
    offsetBtn.setVisible(false);
   }
  }
  validate();
  repaint();
  requestFocus();
 }

 public void viewOffset0()
 {
  viewOffset.setLocation(0, 0);
  viewOffsetChanged();
 }

 public void keyTyped(KeyEvent e)
 {
  System.out.println("keyTyped=" + e);
 }

 public void actionPerformed(ActionEvent e)
 {
  String command = e.getActionCommand();
  if (command.startsWith("POPUP_WIKI"))
  {
   int i = Integer.valueOf(command.substring("POPUP_WIKI".length())).intValue();
   new ShowMessage(((MenuItem)popupWiki.get(i)).getLabel());
  } else
  if (command.equals("GO_HERE"))
  {
   clickLatlng.copyTo(latlng);
   trackTail.clear();
   App.main.viewOffset0();
   App.main.repaint();
  } else
  if (command.equals("CREATE_WAYPOINT"))
   (new NewWaypointDialog(App.getSelf(), clickLatlng)).setVisible(true);
 }

}
