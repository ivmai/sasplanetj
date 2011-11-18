// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   App.java

package sasplanetj;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;
import sasplanetj.gps.LatLng;
import sasplanetj.gps.SerialReader;
import sasplanetj.ui.ShowMessage;
import sasplanetj.util.Cache;
import sasplanetj.util.Config;
import sasplanetj.util.TilesUtil;
import sasplanetj.util.TrackLogger;
import sasplanetj.util.Tracks;
import sasplanetj.util.Waypoint;
import sasplanetj.util.Waypoints;
import sasplanetj.util.Wikimapia;
import sasplanetj.util.XYint;
import sasplanetj.util.Zip;

// Referenced classes of package sasplanetj:
//   Main, NMEALog, GPSLog, GoTo

public class App extends Frame
 implements ActionListener, ItemListener
{

 private static App self;
 public static String args[];
 public static SerialReader serialReader = null;
 private static final TrackLogger trackLogger = new TrackLogger();
 private static final String EXIT_COMMAND = "EXIT_COMMAND";
 private static final String NMEALOG_COMMAND = "NMEALOG_COMMAND";
 private static final String GPSLOG_COMMAND = "GPSLOG_COMMAND";
 private static final String MAIN_COMMAND = "MAIN_COMMAND";
 private static final String ZOOMOUT_COMMAND = "ZOOMOUT_COMMAND";
 private static final String ZOOMIN_COMMAND = "ZOOMIN_COMMAND";
 private static final String ZOOMTO_COMMAND = "ZOOMTO_COMMAND";
 private static final String ZOOMONLYTO_COMMAND = "ZOOMONLYTO_COMMAND";
 private static final String DRAWGRID_COMMAND = "DRAWGRID_COMMAND";
 private static final String DRAWLATLNG_COMMAND = "DRAWLATLNG_COMMAND";
 private static final String DRAWTAIL_COMMAND = "DRAWTAIL_COMMAND";
 private static final String GOTO_COMMAND = "GOTO_COMMAND";
 public static Main main = null;
 private static MenuBar menuBar;
 private static Menu mapsMenu;
 private static MenuItem cmiMapView;
 private static MenuItem cmiCoords;
 private static MenuItem cmiNmea;
 private static MenuItem cmiConnectGPS;
 private static MenuItem cmiTrackLog;
 private static MenuItem cmiWikimapia;
 private static MenuItem cmiDrawGrid;
 private static MenuItem cmiDrawLatLng;
 private static MenuItem cmiDrawTail;
 private static final MenuItem chkMenuZoomOnlyTo[] =
  new MenuItem[TilesUtil.ZOOM_MAX];
 private static final MenuItem chkMenuZoomTo[] =
  new MenuItem[TilesUtil.ZOOM_MAX];
 private static final MenuItem cmiMaps[] = new MenuItem[Config.maps.length];

 public static MenuItem miZoomOut;
 public static MenuItem miZoomIn;
 public static Menu menuZoomOnlyTo;
 private static Menu menuZoomToHigh;
 private static Menu menuZoomToLow;
 private static MenuItem miCenter;
 public Component currentView;

 private static Class miCheckboxClass;
 private static Method cmiSetStateMethod;

 static
 {
  try
  {
   miCheckboxClass = Class.forName("java.awt.CheckboxMenuItem");
   if (!MenuItem.class.isAssignableFrom(miCheckboxClass))
    miCheckboxClass = null;
   cmiSetStateMethod = miCheckboxClass.getMethod("setState",
                        new Class[] { boolean.class });
  }
  catch (Exception e)
  {
   miCheckboxClass = null;
  }
 }

 public App(String appArgs[])
 {
  currentView = null;
  args = appArgs;
  self = this;
  setTitle("SAS.Planet.J");
  addWindowListener(new WindowAdapter() {

   public void windowClosing(WindowEvent we)
   {
    quit();
   }

  }
);
  Config.load();
 }

 private void quit()
 {
  trackLogger.loggerStop();
  if (serialReader != null)
   serialReader.stopReading();
  Config.save();
  if (getToolkit().getClass().getName().endsWith(".BBToolkit"))
   Runtime.getRuntime().halt(0);
  System.exit(0);
 }

 public static boolean useAwtWorkaround()
 {
  return miCheckboxClass == null;
 }

 public static void cmiTrackLogSetState()
 {
  menuCheckboxSetState(cmiTrackLog, Config.trackLog);
 }

 public static void cmiCurMapSetState(boolean state)
 {
  menuCheckboxSetState(cmiMaps[Config.curMapIndex], state);
  if (state)
   mapsMenu.setLabel("Map (" + Config.maps[Config.curMapIndex].key + ")");
 }

 private static void menuCheckboxSetState(MenuItem item, boolean state)
 {
  if (miCheckboxClass != null)
  {
   try
   {
    cmiSetStateMethod.invoke(item, new Object[] { new Boolean(state) });
   }
   catch (Exception e)
   {
    throw new RuntimeException(e.toString());
   }
  }
  else
  {
   String label = item.getLabel();
   if (label.startsWith(">>"))
    label = label.substring(2);
   item.setLabel(state ? ">>" + label : label);
  }
 }

 private MenuItem menuAddNewCheckbox(String name, String cmd, Menu menu)
 {
  MenuItem cmi;
  if (miCheckboxClass != null)
  {
   try
   {
    cmi = (MenuItem)miCheckboxClass.newInstance();
   }
   catch (Exception e)
   {
    throw new RuntimeException(e.toString());
   }
   ((ItemSelectable)cmi).addItemListener(this);
   cmi.setLabel(name);
  }
  else
  {
   cmi = new MenuItem(name);
   cmi.addActionListener(this);
  }
  cmi.setActionCommand(cmd);
  menu.add(cmi);
  return cmi;
 }

 public void addComponents()
 {
  Menu menu = new Menu("Menu");
  menu.addActionListener(this);
  MenuItem mi = new MenuItem("Go to...");
  mi.setActionCommand("GOTO_COMMAND");
  menu.add(mi);
  menu.addSeparator();
  cmiMapView = menuAddNewCheckbox("Map view", "MAIN_COMMAND", menu);
  menuCheckboxSetState(cmiMapView, true);
  cmiCoords = menuAddNewCheckbox("GPS coordinates", "GPSLOG_COMMAND", menu);
  cmiNmea = menuAddNewCheckbox("NMEA", "NMEALOG_COMMAND", menu);
  menu.addSeparator();
  Menu menuWaypoints = new Menu("Waypoints");
  menuWaypoints.addActionListener(this);
  mi = new MenuItem("Open waypoints...");
  mi.setActionCommand("WAYPOINTS_OPEN");
  menuWaypoints.add(mi);
  mi = new MenuItem("Clear waypoints");
  mi.setActionCommand("WAYPOINTS_CLEAR");
  menuWaypoints.add(mi);
  mi = new MenuItem("Save waypoints");
  mi.setActionCommand("WAYPOINTS_SAVE");
  menuWaypoints.add(mi);
  menu.add(menuWaypoints);
  Menu menuTracks = new Menu("Tracks");
  menuTracks.addActionListener(this);
  mi = new MenuItem("Open track...");
  mi.setActionCommand("TRACK_OPEN");
  menuTracks.add(mi);
  mi = new MenuItem("Clear tracks");
  mi.setActionCommand("TRACK_CLEAR");
  menuTracks.add(mi);
  cmiTrackLog = menuAddNewCheckbox("Log to \"track.plt\"", "TRACKLOG",
                 menuTracks);
  mi = new MenuItem("Delete \"track.plt\"");
  mi.setActionCommand("TRACKLOG_DELETE");
  menuTracks.add(mi);
  menu.add(menuTracks);
  menu.addSeparator();
  cmiConnectGPS = menuAddNewCheckbox("Connect to GPS", "CONNECT_GPS", menu);
  menu.addSeparator();
  mi = new MenuItem("Exit");
  mi.setActionCommand("EXIT_COMMAND");
  menu.add(mi);
  menuBar.add(menu);
  menu = new Menu("Opts");
  cmiDrawGrid = menuAddNewCheckbox("Draw grid", "DRAWGRID_COMMAND", menu);
  cmiDrawLatLng = menuAddNewCheckbox("Draw LatLng", "DRAWLATLNG_COMMAND",
                   menu);
  cmiDrawTail = menuAddNewCheckbox("Draw track tail", "DRAWTAIL_COMMAND",
                 menu);
  menuBar.add(menu);
  menu = new Menu();
  for (int i = 0; i < Config.maps.length; i++)
   cmiMaps[i] = menuAddNewCheckbox(Config.maps[i].name, "MAPSWITCH" + i,
                 menu);
  menu.addSeparator();
  Menu menuLayers = new Menu("Layers");
  cmiWikimapia = menuAddNewCheckbox("Wikimapa KML", "WIKIMAPIA", menuLayers);
  menu.add(menuLayers);
  mapsMenu = menu;
  menuBar.add(menu);
  miZoomOut = new MenuItem("Zoom out");
  miZoomOut.setActionCommand("ZOOMOUT_COMMAND");
  miZoomIn = new MenuItem("Zoom in");
  miZoomIn.setActionCommand("ZOOMIN_COMMAND");
  menuZoomOnlyTo = new Menu("Zoom in/out...");
  menuZoomOnlyTo.addActionListener(this);
  menuZoomToHigh = new Menu("Zoom to (high)...");
  menuZoomToHigh.addActionListener(this);
  menuZoomToLow = new Menu("Zoom to (low)...");
  menuZoomToLow.addActionListener(this);
  miCenter = new MenuItem("Center");
  miCenter.setActionCommand("CENTER_COMMAND");
  for (int i = TilesUtil.ZOOM_MAX; i >= 1; i--)
  {
   chkMenuZoomOnlyTo[i - 1] = menuAddNewCheckbox("level " + i,
                               "ZOOMONLYTO_COMMAND" + i, menuZoomOnlyTo);
   chkMenuZoomTo[i - 1] = menuAddNewCheckbox("level " + i,
                           "ZOOMTO_COMMAND" + i, i > TilesUtil.ZOOM_MAX / 2 ?
                           menuZoomToHigh : menuZoomToLow);
  }

  setMenuBar(menuBar);
  Zip.zipsCache = new Cache(Config.useSoftRefs ?
                   Config.zipCacheSize * 2 + 1 : Config.zipCacheSize);
  TilesUtil.tilesCache = new Cache(Config.imageCacheSize);
  Wikimapia.kmlCache = new Cache(Config.wikikmlCacheSize);
  if (Config.connectGPS)
  {
   createSerialReader();
   if (serialReader == null)
    serialReader = new SerialReader();
   serialReader.start();
   if (Config.trackLog)
    trackLogger.loggerStart();
  } else
  {
   Config.trackLog = false;
  }
  if (main == null)
   main = new Main();
  changeView(main);
 }

 private void redrawMenuCheckboxes()
 {
  menuCheckboxSetState(cmiConnectGPS, Config.connectGPS);
  menuCheckboxSetState(cmiDrawGrid, Config.drawGrid);
  menuCheckboxSetState(cmiDrawLatLng, Config.drawLatLng);
  menuCheckboxSetState(cmiDrawTail, Config.drawTail);
  menuCheckboxSetState(cmiWikimapia, Config.drawWikimapia);
 }

 private void afterSetVisible()
 {
  redrawMenuCheckboxes();
  cmiCurMapSetState(true);
  zoomMenu();
 }

 public void changeView(Component newView)
 {
  if (currentView != null)
  {
   if (currentView.getClass() == sasplanetj.NMEALog.class)
    ((NMEALog)currentView).removeListener();
   else
   if (currentView.getClass() == sasplanetj.GPSLog.class)
    ((GPSLog)currentView).removeListener();
   else
   if (currentView.getClass() == sasplanetj.Main.class)
    ((Main)currentView).removeListener();
   remove(currentView);
   menuCheckboxSetState(cmiMapView, false);
   menuCheckboxSetState(cmiCoords, false);
   menuCheckboxSetState(cmiNmea, false);
  }
  add(newView);
  currentView = newView;
  currentView.requestFocus();
  validate();
  if (currentView == main)
  {
   menuCheckboxSetState(cmiMapView, true);
   if (Config.connectGPS)
    ((Main)currentView).registerListener();
  }
  else if (currentView.getClass() == GPSLog.class)
   menuCheckboxSetState(cmiCoords, true);
  else if (currentView.getClass() == NMEALog.class)
   menuCheckboxSetState(cmiNmea, true);
 }

 public void actionPerformed(ActionEvent e)
 {
  String command = e.getActionCommand();
  if (command == "EXIT_COMMAND")
   quit();
  else
  if (command == "ZOOMIN_COMMAND")
   zoomIn();
  else
  if (command == "ZOOMOUT_COMMAND")
   zoomOut();
  else
  if (command == "CENTER_COMMAND")
  {
   main.viewOffset0();
   return;
  }
  else
  if (command == "GOTO_COMMAND")
   (new GoTo(this)).setVisible(true);
  else
  if (command.equals("WAYPOINTS_OPEN"))
  {
   FileDialog d = new FileDialog(this, "Open waypoints file", 0);
   d.setFile("*.wpt");
   d.setVisible(true);
   if (d.getFile() != null)
   {
    String filename = d.getDirectory();
    if (filename.charAt(filename.length() - 1) != File.separatorChar)
     filename = filename + File.separatorChar;
    filename = filename + d.getFile();
    Waypoints.load(filename);
   }
  } else
  if (command.equals("TRACK_OPEN"))
  {
   FileDialog d = new FileDialog(this, "Open track file", 0);
   d.setFile("*.plt");
   d.setVisible(true);
   if (d.getFile() != null)
   {
    String filename = d.getDirectory();
    if (filename.charAt(filename.length() - 1) != File.separatorChar)
     filename = filename + File.separatorChar;
    filename = filename + d.getFile();
    Tracks.load(filename);
   }
  } else
  if (command.equals("WAYPOINTS_CLEAR"))
  {
   Waypoints.points = null;
   main.repaint();
  } else
  if (command.equals("TRACK_CLEAR"))
  {
   Tracks.tracks = null;
   main.repaint();
  } else
  if (command.equals("TRACKLOG_DELETE"))
  {
   trackLogger.deleteFile();
  } else
  if (command.equals("WAYPOINTS_SAVE"))
  {
   if (Waypoints.points == null || Waypoints.points.size() == 0)
   {
    new ShowMessage("Waypoint list is empty");
    return;
   }
   FileDialog d = new FileDialog(this, "Save waypoints to file", 1);
   d.setFile("*.wpt");
   d.setVisible(true);
   if (d.getFile() != null)
   {
    String filename = d.getDirectory();
    if (filename.charAt(filename.length() - 1) != File.separatorChar)
     filename = filename + File.separatorChar;
    filename = filename + d.getFile();
    Waypoints.save(filename);
   }
  }
  if (miCheckboxClass != null)
   currentView.requestFocus();
  else
  {
   processCheckboxToggled(command);
   redrawMenuCheckboxes();
  }
 }

 public void itemStateChanged(ItemEvent e)
 {
  processCheckboxToggled(((MenuItem)e.getSource()).getActionCommand());
 }

 private void processCheckboxToggled(String command)
 {
  if (command == "MAIN_COMMAND")
   changeView(main);
  else
  if (command == "GPSLOG_COMMAND")
   changeView(new GPSLog());
  else
  if (command == "NMEALOG_COMMAND")
   changeView(new NMEALog());
  else
  if (command == "DRAWGRID_COMMAND")
  {
   Config.drawGrid = !Config.drawGrid;
   main.repaint();
  } else
  if (command == "DRAWLATLNG_COMMAND")
  {
   Config.drawLatLng = !Config.drawLatLng;
   main.repaint();
  } else
  if (command == "WIKIMAPIA")
  {
   Config.drawWikimapia = !Config.drawWikimapia;
   main.repaint();
  } else
  if (command.startsWith("MAPSWITCH"))
  {
   cmiCurMapSetState(false);
   Config.switchMapTo(Integer.valueOf(command.substring("MAPSWITCH".length())).intValue());
   cmiCurMapSetState(true);
   main.repaint();
  } else
  if (command.startsWith("ZOOMTO_COMMAND"))
   zoom((new Integer(command.substring("ZOOMTO_COMMAND".length()))).intValue());
  else
  if (command.startsWith("ZOOMONLYTO_COMMAND"))
  {
   Integer z = Integer.valueOf(command.substring("ZOOMONLYTO_COMMAND".length()));
   if (!Config.zoomsAvail.contains(z))
    Config.zoomsAvail.add(z);
   else
    Config.zoomsAvail.remove(z);
  } else
  if (command == "DRAWTAIL_COMMAND")
  {
   Config.drawTail = !Config.drawTail;
   if (Config.drawTail)
    Main.trackTail.clear();
   main.repaint();
  } else
  if (command == "CONNECT_GPS")
  {
   Config.connectGPS = !Config.connectGPS;
   if (!Config.connectGPS)
   {
    Config.trackLog = false;
    trackLogger.loggerStop();
    main.removeListener();
    if (serialReader != null)
     serialReader.suspendReading();
   } else
   {
    if (Config.drawTail)
     Main.trackTail.clear();
    if (serialReader != null && serialReader.isAlive())
    {
     main.registerListener();
     serialReader.resumeReading();
    }
    else
    {
     createSerialReader();
     if (serialReader != null)
     {
      main.registerListener();
      serialReader.start();
     }
     else
     {
      Config.connectGPS = false;
      redrawMenuCheckboxes();
      getToolkit().beep();
     }
    }
   }
  } else
  if (command == "TRACKLOG")
  {
   Config.trackLog = !Config.trackLog;
   if (!Config.connectGPS)
    Config.trackLog = false;
   if (Config.trackLog)
    trackLogger.loggerStart();
   else
    trackLogger.loggerStop();
  }
  currentView.requestFocus();
 }

 public static void zoomIn()
 {
  if (Config.zoom + 1 > TilesUtil.ZOOM_MAX)
  {
   Toolkit.getDefaultToolkit().beep();
   return;
  }
  int zoom = Config.zoom + 1;
  for (int i = zoom; i <= TilesUtil.ZOOM_MAX; i++)
  {
   if (Config.zoomsAvail.contains(new Integer(zoom)))
    break;
   zoom++;
  }
  if (zoom > TilesUtil.ZOOM_MAX)
  {
   Toolkit.getDefaultToolkit().beep();
   System.out.println("This is the maximum zoom allowed");
   return;
  } else
  {
   zoom(zoom);
   return;
  }
 }

 public static void zoomOut()
 {
  if (Config.zoom - 1 < 1)
  {
   Toolkit.getDefaultToolkit().beep();
   return;
  }
  int zoom = Config.zoom - 1;
  for (int i = zoom; i >= 1; i--)
  {
   if (Config.zoomsAvail.contains(new Integer(zoom)))
    break;
   zoom--;
  }

  if (zoom == 0)
  {
   Toolkit.getDefaultToolkit().beep();
   System.out.println("This is the minimum zoom allowed");
   return;
  } else
  {
   zoom(zoom);
   return;
  }
 }

 public static void zoom(int zoom)
 {
  Main.viewOffset.multiply(Math.pow(2.0, zoom - Config.zoom));
  Config.zoom = zoom;
  getSelf().zoomMenu();
  main.repaint();
 }

 public void zoomMenu()
 {
  if (menuBar.getMenuCount() >= 4)
   menuBar.remove(3);
  Menu menuZoom = new Menu("Zoom " + Config.zoom);
  menuZoom.addActionListener(this);
  menuZoom.add(miZoomOut);
  menuZoom.add(miZoomIn);
  menuZoom.addSeparator();
  menuZoom.add(menuZoomToHigh);
  menuZoom.add(menuZoomToLow);
  menuZoom.add(menuZoomOnlyTo);
  menuZoom.addSeparator();
  menuZoom.add(miCenter);
  menuBar.add(menuZoom);
  for (int i = TilesUtil.ZOOM_MAX; i >= 1; i--)
  {
   menuCheckboxSetState(chkMenuZoomTo[i - 1], false);
   menuCheckboxSetState(chkMenuZoomOnlyTo[i - 1], Config.zoomsAvail.contains(new Integer(i)));
  }

  menuCheckboxSetState(chkMenuZoomTo[Config.zoom - 1], true);
 }

 public static void createSerialReader()
 {
  if (args.length > 0)
  {
   String port = args[0];
   try
   {
    int baudRate = args.length <= 1 ? 9600 : (new Integer(args[1])).intValue();
    serialReader = new SerialReader(port, baudRate);
   }
   catch (Exception e)
   {
    serialReader = null;
   }
  } else
  {
   serialReader = new SerialReader();
  }
 }

 public static void Goto(LatLng latlng)
 {
  Config.connectGPS = false;
  menuCheckboxSetState(cmiConnectGPS, false);
  main.removeListener();
  Main.trackTail.clear();
  latlng.copyTo(Main.latlng);
  main.viewOffset0();
 }

 public static void CreateWaypoint(LatLng latlng, String name)
 {
  Waypoints.points.add(new Waypoint(latlng, name));
  main.repaint();
 }

 public static void main(String args[])
  throws Exception
 {
  System.out.println("SAS.Planet.J v0.0.6");
  System.out.println("Working directory: " + Config.curDir);
  if (args.length > 0 && args[0].equals("list"))
  {
   SerialReader.listPorts();
   return;
  }
  if (miCheckboxClass == null)
   System.out.println("No CheckboxMenuItem class");
  menuBar = new MenuBar();
  App app = new App(args);
  Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  if (System.getProperty("os.name").equals("Windows CE"))
  {
   System.out.println("Screen size=" + screenSize);
   app.setSize(screenSize);
  } else
  {
   app.setLocation(screenSize.width / 2, screenSize.height / 10);
   app.setSize(new Dimension(245, 320));
  }
  app.addComponents();
  app.setVisible(true);
  app.afterSetVisible();
  app.currentView.requestFocus();
 }

 /* public void fullscreen()
 {
  GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
  gd.setFullScreenWindow(this);
 } */

 public static App getSelf()
 {
  return self;
 }
}
