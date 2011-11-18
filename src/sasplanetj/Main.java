// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Main.java

package sasplanetj;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.lang.reflect.Method;
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
import sasplanetj.util.Zip;

// Referenced classes of package sasplanetj:
//   App

public class Main extends Panel
 implements GPSListener, MouseListener, MouseMotionListener, KeyListener, ActionListener
{

 private static final boolean debugMouseEvents = false;
 
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
 private int skipCounter;
 private final XYint mouseDragPrevXY = new XYint();
 private boolean isMousePressed;
 private static boolean ignoreDblClick;
 private static final Method mouseGetButtonMethod;
 private boolean repaintOnLowResources;

 static
 {
  Method m = null;
  try
  {
   m = MouseEvent.class.getMethod("getButton", new Class[0]);
  }
  catch (Exception e)
  {
   System.out.println("No MouseEvent.getButton()");
  }
  mouseGetButtonMethod = m;
 }

 public Main()
 {
  if (!App.useAwtWorkaround())
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
     Dimension offsetBtnSize = offsetBtn.getSize();
     add(Main.offsetBtn,
      new XYConstraints(getSize().width - offsetBtnSize.width - 5,
      getSize().height - offsetBtnSize.height - 5, offsetBtnSize.width,
      offsetBtnSize.height));
     validate();
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
   if (offscreen != null)
    offscreen.flush();
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
   System.err.println("offscreen==null");
   return;
  }
  try
  {
   paintInner(g);
  }
  catch (Error e)
  {
   System.err.println("paint error: " + e);
   if (repaintOnLowResources || !isLowResourceExc(e))
   {
    Config.save();
    Runtime.getRuntime().halt(1);
   }
   repaintOnLowResources = true;
   clearCache();
   getToolkit().beep();
   paint(g);
   System.out.println("Repainted after cache clearing");
   printHeapStat();
   repaintOnLowResources = false;
  }
 }

 private static boolean isLowResourceExc(Error e)
 {
  String errMsg;
  return e instanceof OutOfMemoryError ||
          (e.getClass().getName().endsWith(".SWTError") &&
          (errMsg = e.getMessage()) != null && errMsg.startsWith("No more"));
 }

 private static void clearCache()
 {
  Zip.zipsCache.clearAll();
  TilesUtil.tilesCache.clearAll();
  TilesUtil.zipExistanceCache.clearAll();
  Wikimapia.kmlCache.clearAll();
  System.gc();
  Runtime.getRuntime().runFinalization();
  System.gc();
 }

 private void paintInner(Graphics g)
 {
  XYint center = new XYint(offscreen.getWidth(null) / 2, offscreen.getHeight(null) / 2);
  XYint displayXY = TilesUtil.coordinateToDisplay(latlng.lat, latlng.lng,
                     Config.zoom, Config.isMapYandex);
  if (Config.drawTail && latlng.lat != 0.0D && latlng.lng != 0.0D)
   trackTail.addPoint(new XY(latlng.lat, latlng.lng));
  displayXY.subtract(viewOffset);
  XYint centerTileTopLeft =
   new XYint(center.x - (displayXY.x & ((1 << TilesUtil.LOG2_TILESIZE) - 1)),
   center.y - (displayXY.y & ((1 << TilesUtil.LOG2_TILESIZE) - 1)));
  XYint tileXY = TilesUtil.getTileByDisplayCoord(displayXY);
  XYint tileWikiXY = tileXY;
  if (Config.isMapYandex)
  {
   XYint displayWikiXY = TilesUtil.coordinateToDisplay(latlng.lat, latlng.lng,
                          Config.zoom, false);
   displayWikiXY.subtract(viewOffset);
   tileWikiXY = TilesUtil.getTileByDisplayCoord(displayWikiXY);
  }
  XYint matrix[] = TilesUtil.drawTilesArea(offscreen.getWidth(null),
                    offscreen.getHeight(null), centerTileTopLeft, tileXY,
                    tileWikiXY, dbf);
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
  if (App.serialReader != null)
   App.serialReader.addGPSListener(this);
 }

 public void removeListener()
 {
  if (App.serialReader != null)
   App.serialReader.removeGPSListener(this);
 }

 public void mouseClicked(MouseEvent mouseevent)
 {
  if (debugMouseEvents)
   System.out.println("mouseClicked: " + mouseevent);
 }

 private boolean doPopup(MouseEvent e)
 {
  boolean isPopup;
  if (System.getProperty("java.vm.name").startsWith("CrE-ME"))
   isPopup = true;
  else
  {
   if (e.isPopupTrigger() || e.getModifiers() == InputEvent.META_MASK)
   {
    isPopup = true;
    ignoreDblClick = true;
   }
   else
   {
    isPopup = false;
    if (mouseGetButtonMethod != null)
    {
     try
     {
      int button =
       ((Integer)mouseGetButtonMethod.invoke(e, new Object[0])).intValue();
      if (button == 2 || button == 3)
      {
       isPopup = true;
       ignoreDblClick = true;
      }
     }
     catch (Exception ee) {}
    }
    if (!ignoreDblClick && e.getModifiers() == InputEvent.BUTTON1_MASK &&
        e.getClickCount() == 2)
     isPopup = true;
   }
  }
  if (isPopup && !App.useAwtWorkaround())
  {
   try
   {
    doPopupInner(e);
   }
   catch (Error e2)
   {
    System.err.println("doPopup error: " + e2);
    if (!repaintOnLowResources && isLowResourceExc(e2))
     clearCache();
    getToolkit().beep();
   }
  }
  return isPopup;
 }

 private void doPopupInner(MouseEvent e)
 {
  popup.removeAll();
  setClickLatlng(e);
  MenuItem mi = new MenuItem(clickLatlng.toShortString());
  popup.add(mi);
  mi = new MenuItem("Go here");
  mi.setActionCommand("GO_HERE");
  popup.add(mi);
  mi = new MenuItem("Create waypoint");
  mi.setActionCommand("CREATE_WAYPOINT");
  popup.add(mi);
  int px = e.getX();
  if (Config.drawWikimapia)
  {
   Point point = new Point(px + ((TilesUtil.lastWikiEndX - px - 1) &
                  ~((1 << (Config.zoom + TilesUtil.LOG2_TILESIZE - 1)) - 1)),
                  e.getY());
   popupWiki.clear();
   Hashtable wikiStrSet = new Hashtable();
   int i = 0;
   for (Iterator iterator = Wikimapia.drawnKmlsIterator(); iterator.hasNext();)
   {
    Wikimapia.KML kml = (Wikimapia.KML)iterator.next();
    if (kml.drawnPoly.contains(point))
    {
     String name = kml.strip();
     if (wikiStrSet.put(name, "") == null)
     {
      mi = new MenuItem(name);
      mi.setActionCommand("POPUP_WIKI" + i);
      popupWiki.add(mi);
     }
     i++;
    }
   }
   if (popupWiki.size() > 0)
    popup.addSeparator();
   for (i = 0; i < popupWiki.size(); i++)
    popup.add((MenuItem)popupWiki.get(i));

  }
  popup.show(this, px + 5, e.getY());
 }

 private void setClickLatlng(MouseEvent e)
 {
  XYint displayXY = TilesUtil.coordinateToDisplay(latlng.lat, latlng.lng,
                     Config.zoom, Config.isMapYandex);
  displayXY.subtract(viewOffset);
  XYint clickOffset = new XYint(e.getPoint().x - getSize().width / 2,
                       e.getPoint().y - getSize().height / 2);
  displayXY.add(clickOffset);
  clickLatlng = TilesUtil.displayToCoordinate(displayXY, Config.zoom,
                 Config.isMapYandex);
 }

 public void mouseEntered(MouseEvent mouseevent)
 {
 }

 public void mouseExited(MouseEvent mouseevent)
 {
 }

 public void mousePressed(MouseEvent e)
 {
  if (debugMouseEvents)
   System.out.println("mousePressed: " + e);
  isMousePressed = false;
  if (!doPopup(e) && (e.getModifiers() & (InputEvent.BUTTON1_MASK |
      (1 << 10)/*InputEvent.BUTTON1_DOWN_MASK*/)) != 0)
   isMousePressed = true;
  mouseDragPrevXY.setLocation(e.getX(), e.getY());
 }

 public void mouseReleased(MouseEvent mouseevent)
 {
  if (debugMouseEvents)
   System.out.println("mouseReleased: " + mouseevent);
  isMousePressed = false;
 }

 public void mouseDragged(MouseEvent e)
 {
  if (debugMouseEvents)
   System.out.println("mouseDragged: " + e);
  if (!isMousePressed && mouseGetButtonMethod == null)
   return;
  handleDrag(e);
 }

 private void handleDrag(MouseEvent e)
 {
  XYint deltadrag = new XYint(e.getX() - mouseDragPrevXY.x, e.getY() - mouseDragPrevXY.y);
  viewOffset.add(deltadrag);
  viewOffsetChanged();
  mouseDragPrevXY.setLocation(e.getX(), e.getY());
 }

 public void mouseMoved(MouseEvent e)
 {
  if (isMousePressed)
  {
   if (debugMouseEvents)
    System.out.println("mouseMoved (pressed state): " + e);
   handleDrag(e);
  }
 }

 public void keyPressed(KeyEvent keyevent)
 {
 }

 public void keyReleased(KeyEvent e)
 {
  int keyCode = e.getKeyCode();
  if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0)
  {
   switch (keyCode)
   {
   case KeyEvent.VK_UP:
    keyCode = KeyEvent.VK_PAGE_UP;
    break;
   case KeyEvent.VK_DOWN:
    keyCode = KeyEvent.VK_PAGE_DOWN;
    break;
   case KeyEvent.VK_RIGHT:
    keyCode = KeyEvent.VK_1;
    break;
   case KeyEvent.VK_LEFT:
    keyCode = KeyEvent.VK_2;
    break;
   }
  }
  switch (keyCode)
  {
  case KeyEvent.VK_F2:
  case KeyEvent.VK_PAGE_DOWN:
   App.zoomOut();
   break;

  case KeyEvent.VK_F1:
  case KeyEvent.VK_PAGE_UP:
   App.zoomIn();
   break;

  case KeyEvent.VK_UP:
   viewOffset.y += viewOffsetStep;
   viewOffsetChanged();
   break;

  case KeyEvent.VK_RIGHT:
   viewOffset.x -= viewOffsetStep;
   viewOffsetChanged();
   break;

  case KeyEvent.VK_DOWN:
   viewOffset.y -= viewOffsetStep;
   viewOffsetChanged();
   break;

  case KeyEvent.VK_LEFT:
   viewOffset.x += viewOffsetStep;
   viewOffsetChanged();
   break;

  case KeyEvent.VK_ENTER:
   viewOffset0();
   System.gc();
   printHeapStat();
   break;

  case KeyEvent.VK_1:
   switchMapToAndRepaint(Config.curMapIndex + 1);
   break;
  
  case KeyEvent.VK_2:
   switchMapToAndRepaint(Config.curMapIndex - 1);
   break;
  
  }
 }

 private static void printHeapStat()
 {
  long totalMemSize = Runtime.getRuntime().totalMemory();
  System.out.println("Memory heap used/total: " +
   (int)((totalMemSize - Runtime.getRuntime().freeMemory()) >> 10) + "/" +
   (int)(totalMemSize >> 10) + " KiB");
 }

 private void switchMapToAndRepaint(int mapIndex)
 {
  App.cmiCurMapSetState(false);
  Config.switchMapTo(mapIndex < 0 ? Config.maps.length - 1 :
   mapIndex < Config.maps.length ? mapIndex : 0);
  App.cmiCurMapSetState(true);
  repaint();
 }

 public void viewOffsetChanged()
 {
  viewOffset.x = TilesUtil.adjustViewOfsX(viewOffset.x, Config.zoom);
  if (offsetBtn.isImageLoaded())
  {
   boolean found = false;
   for (int i = 0; i < getComponentCount(); i++)
   {
    if (getComponent(i) != offsetBtn)
     continue;
    found = true;
    break;
   }
   if (viewOffset.x != 0 || viewOffset.y != 0)
   {
    if (!found)
    {
     Dimension offsetBtnSize = offsetBtn.getSize();
     add(offsetBtn, new XYConstraints(
      getSize().width - offsetBtnSize.width - 5,
      getSize().height - offsetBtnSize.height - 5,
      offsetBtnSize.width, offsetBtnSize.height));
     offsetBtn.setVisible(true);
    }
   } else
   {
    if (found)
    {
     remove(offsetBtn);
     offsetBtn.setVisible(false);
    }
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
  char ch = e.getKeyChar();
  if (ch != '\n' && ch != '1' && ch != '2')
   System.out.println("keyTyped=" + e);
 }

 private void processGoHere()
 {
  clickLatlng.copyTo(latlng);
  trackTail.clear();
  viewOffset0();
  repaint();
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
   processGoHere();
  } else
  if (command.equals("CREATE_WAYPOINT"))
   (new NewWaypointDialog(App.getSelf(), clickLatlng)).setVisible(true);
 }

}
