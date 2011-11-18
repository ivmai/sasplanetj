// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   App.java

package sasplanetj;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import javax.comm.CommPortIdentifier;
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

// Referenced classes of package sasplanetj:
//   Main, NMEALog, GPSLog, GoTo

public class App extends Frame
 implements ActionListener, ItemListener
{

 private static App self;
 public static String args[];
 public static SerialReader serialReader = null;
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
 private static final MenuBar menuBar = new MenuBar();
 public static CheckboxMenuItem cmiConnectGPS;
 public static CheckboxMenuItem cmiTrackLog;
 public static CheckboxMenuItem cmiWikimapia;
 public static CheckboxMenuItem cmiDrawGrid;
 public static CheckboxMenuItem cmiDrawLatLng;
 public static CheckboxMenuItem cmiDrawTail;
 public static MenuItem miZoomOut;
 public static MenuItem miZoomIn;
 public static Menu menuZoomOnlyTo;
 public static CheckboxMenuItem chkMenuZoomOnlyTo[];
 public static Menu menuZoomTo;
 public static CheckboxMenuItem chkMenuZoomTo[];
 public Component currentView;
 public static CheckboxMenuItem cmiMaps[];

 public App(String args[])
 {
  currentView = null;
  this.args = args;
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
  TrackLogger.loggerStop();
  if (serialReader != null && serialReader.isAlive())
   serialReader.stopReading();
  Config.save();
  System.exit(0);
 }

 public void addComponents()
 {
  Menu menu = new Menu("Menu");
  menu.addActionListener(this);
  MenuItem mi = new MenuItem("Go to...");
  mi.setActionCommand("GOTO_COMMAND");
  menu.add(mi);
  mi = new MenuItem("Map");
  mi.setActionCommand("MAIN_COMMAND");
  menu.add(mi);
  mi = new MenuItem("GPS coordinates");
  mi.setActionCommand("GPSLOG_COMMAND");
  menu.add(mi);
  mi = new MenuItem("NMEA");
  mi.setActionCommand("NMEALOG_COMMAND");
  menu.add(mi);
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
  cmiTrackLog = new CheckboxMenuItem("Log to \"track.plt\"");
  cmiTrackLog.setActionCommand("TRACKLOG");
  cmiTrackLog.addItemListener(this);
  menuTracks.add(cmiTrackLog);
  mi = new MenuItem("Delete \"track.plt\"");
  mi.setActionCommand("TRACKLOG_DELETE");
  menuTracks.add(mi);
  menu.add(menuTracks);
  menu.addSeparator();
  cmiConnectGPS = new CheckboxMenuItem("Connect to GPS");
  cmiConnectGPS.setActionCommand("CONNECT_GPS");
  cmiConnectGPS.addItemListener(this);
  menu.add(cmiConnectGPS);
  menu.addSeparator();
  mi = new MenuItem("Exit");
  mi.setActionCommand("EXIT_COMMAND");
  menu.add(mi);
  menuBar.add(menu);
  menu = new Menu("Opts");
  cmiDrawGrid = new CheckboxMenuItem("Draw grid");
  cmiDrawGrid.setActionCommand("DRAWGRID_COMMAND");
  cmiDrawGrid.addItemListener(this);
  menu.add(cmiDrawGrid);
  cmiDrawLatLng = new CheckboxMenuItem("Draw LatLng");
  cmiDrawLatLng.setActionCommand("DRAWLATLNG_COMMAND");
  cmiDrawLatLng.addItemListener(this);
  menu.add(cmiDrawLatLng);
  cmiDrawTail = new CheckboxMenuItem("Draw track tail");
  cmiDrawTail.setActionCommand("DRAWTAIL_COMMAND");
  cmiDrawTail.addItemListener(this);
  menu.add(cmiDrawTail);
  menuBar.add(menu);
  menu = new Menu("Maps");
  for (int i = 0; i < Config.maps.length; i++)
  {
   CheckboxMenuItem cmi = new CheckboxMenuItem(Config.maps[i].name);
   cmi.setActionCommand("MAPSWITCH" + i);
   cmi.addItemListener(this);
   menu.add(cmi);
   cmiMaps[i] = cmi;
  }

  menu.addSeparator();
  Menu menuLayers = new Menu("Layers");
  cmiWikimapia = new CheckboxMenuItem("Wikimapa KML");
  cmiWikimapia.setActionCommand("WIKIMAPIA");
  cmiWikimapia.addItemListener(this);
  menuLayers.add(cmiWikimapia);
  menu.add(menuLayers);
  menuBar.add(menu);
  miZoomOut = new MenuItem("Zoom out");
  miZoomOut.setActionCommand("ZOOMOUT_COMMAND");
  miZoomIn = new MenuItem("Zoom in");
  miZoomIn.setActionCommand("ZOOMIN_COMMAND");
  menuZoomOnlyTo = new Menu("Zoom in/out...");
  menuZoomOnlyTo.addActionListener(this);
  chkMenuZoomOnlyTo = new CheckboxMenuItem[19];
  menuZoomTo = new Menu("Zoom to...");
  menuZoomTo.addActionListener(this);
  chkMenuZoomTo = new CheckboxMenuItem[19];
  for (int i = 19; i >= 1; i--)
  {
   CheckboxMenuItem cmi = new CheckboxMenuItem("level " + i);
   cmi.setActionCommand("ZOOMONLYTO_COMMAND" + i);
   cmi.addItemListener(this);
   menuZoomOnlyTo.add(cmi);
   chkMenuZoomOnlyTo[i - 1] = cmi;
   cmi = new CheckboxMenuItem("level " + i);
   cmi.setActionCommand("ZOOMTO_COMMAND" + i);
   cmi.addItemListener(this);
   menuZoomTo.add(cmi);
   chkMenuZoomTo[i - 1] = cmi;
  }

  setMenuBar(menuBar);
  TilesUtil.tilesCache = new Cache(Config.imageCacheSize);
  Wikimapia.kmlCache = new Cache(Config.wikikmlCacheSize);
  if (!Config.connectGPS)
   Config.trackLog = false;
  if (Config.connectGPS)
  {
   createSerialReader();
   serialReader.start();
   if (Config.trackLog)
    TrackLogger.loggerStart();
  } else
  {
   serialReader = new SerialReader();
  }
  if (main == null)
   main = new Main();
  changeView(main);
 }

 private void afterSetVisible()
 {
  cmiConnectGPS.setState(Config.connectGPS);
  cmiDrawGrid.setState(Config.drawGrid);
  cmiDrawLatLng.setState(Config.drawLatLng);
  cmiDrawTail.setState(Config.drawTail);
  cmiWikimapia.setState(Config.drawWikimapia);
  cmiMaps[Config.curMapIndex].setState(true);
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
  }
  add(newView);
  currentView = newView;
  currentView.requestFocus();
  validate();
  if (currentView == main && Config.connectGPS)
   ((Main)currentView).registerListener();
 }

 public void actionPerformed(ActionEvent e)
 {
  String command = e.getActionCommand();
  if (command == "EXIT_COMMAND")
   quit();
  else
  if (command == "NMEALOG_COMMAND")
   changeView(new NMEALog());
  else
  if (command == "GPSLOG_COMMAND")
   changeView(new GPSLog());
  else
  if (command == "MAIN_COMMAND")
   changeView(main);
  else
  if (command == "ZOOMIN_COMMAND")
   zoomIn();
  else
  if (command == "ZOOMOUT_COMMAND")
   zoomOut();
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
   File tracklog = new File("track.plt");
   if (tracklog.exists())
    tracklog.delete();
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
  currentView.requestFocus();
 }

 public void itemStateChanged(ItemEvent e)
 {
  CheckboxMenuItem chk = (CheckboxMenuItem)e.getSource();
  String command = chk.getActionCommand();
  if (command == "DRAWGRID_COMMAND")
  {
   Config.drawGrid = e.getStateChange() == 1;
   main.repaint();
  } else
  if (command == "DRAWLATLNG_COMMAND")
  {
   Config.drawLatLng = e.getStateChange() == 1;
   main.repaint();
  } else
  if (command == "WIKIMAPIA")
  {
   Config.drawWikimapia = e.getStateChange() == 1;
   main.repaint();
  } else
  if (command.startsWith("MAPSWITCH"))
  {
   for (int i = 0; i < Config.maps.length; i++)
    cmiMaps[i].setState(false);

   Config.switchMapTo(Integer.valueOf(command.substring("MAPSWITCH".length())).intValue());
   cmiMaps[Config.curMapIndex].setState(true);
   main.repaint();
  } else
  if (command.startsWith("ZOOMTO_COMMAND"))
   zoom((new Integer(command.substring("ZOOMTO_COMMAND".length()))).intValue());
  else
  if (command.startsWith("ZOOMONLYTO_COMMAND"))
  {
   Integer z = Integer.valueOf(command.substring("ZOOMONLYTO_COMMAND".length()));
   if (e.getStateChange() == 1)
    Config.zoomsAvail.add(z);
   else
    Config.zoomsAvail.remove(z);
  } else
  if (command == "DRAWTAIL_COMMAND")
  {
   Config.drawTail = e.getStateChange() == 1;
   if (Config.drawTail)
    Main.trackTail.clear();
   main.repaint();
  } else
  if (command == "CONNECT_GPS")
  {
   Config.connectGPS = e.getStateChange() == 1;
   if (!Config.connectGPS)
   {
    Config.trackLog = false;
    TrackLogger.loggerStop();
    main.removeListener();
    if (serialReader.isAlive())
     serialReader.stopReading();
   } else
   {
    if (Config.drawTail)
     Main.trackTail.clear();
    if (serialReader == null || !serialReader.isAlive())
    {
     createSerialReader();
     main.registerListener();
     serialReader.start();
    }
   }
  } else
  if (command == "TRACKLOG")
  {
   Config.trackLog = e.getStateChange() == 1;
   if (!Config.connectGPS)
    Config.trackLog = false;
   if (Config.trackLog)
    TrackLogger.loggerStart();
   else
    TrackLogger.loggerStop();
  }
  currentView.requestFocus();
 }

 public static void zoomIn()
 {
  if (Config.zoom + 1 > 19)
  {
   Toolkit.getDefaultToolkit().beep();
   return;
  }
  int zoom = Config.zoom + 1;
  for (int i = zoom; i <= 19; i++)
  {
   if (Config.zoomsAvail.contains(new Integer(zoom)))
    break;
   zoom++;
  }

  if (zoom == 20)
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
  double deltaview = Math.pow(2D, zoom) / Math.pow(2D, Config.zoom);
  Main.viewOffset.multiply(deltaview);
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
  menuZoom.add(menuZoomTo);
  menuZoom.add(menuZoomOnlyTo);
  menuBar.add(menuZoom);
  for (int i = 19; i >= 1; i--)
  {
   chkMenuZoomTo[i - 1].setState(false);
   chkMenuZoomOnlyTo[i - 1].setState(Config.zoomsAvail.contains(new Integer(i)));
  }

  chkMenuZoomTo[Config.zoom - 1].setState(true);
 }

 public static void createSerialReader()
 {
  if (args.length > 0)
  {
   String port = args[0];
   int baudRate = args.length <= 1 ? 9600 : (new Integer(args[1])).intValue();
   try
   {
    serialReader = new SerialReader(port, baudRate);
   }
   catch (Exception e)
   {
    e.printStackTrace();
   }
  } else
  {
   serialReader = new SerialReader();
  }
 }

 public static void Goto(LatLng latlng)
 {
  Config.connectGPS = false;
  cmiConnectGPS.setState(false);
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
  System.out.println("Working directory: " + Config.curDir);
  if (args.length > 0 && args[0].equals("list"))
  {
   Enumeration portList = CommPortIdentifier.getPortIdentifiers();
   System.out.println("Available ports:");
   CommPortIdentifier portId;
   for (; portList.hasMoreElements(); System.out.println("\t" + portId.getName()))
    portId = (CommPortIdentifier)portList.nextElement();

   return;
  }
  App app = new App(args);
  if (System.getProperty("os.name").equals("Windows CE"))
  {
   System.out.println("Screen size=" + Toolkit.getDefaultToolkit().getScreenSize());
   app.setSize(Toolkit.getDefaultToolkit().getScreenSize());
  } else
  {
   app.setLocation(600, 100);
   app.setSize(new Dimension(245, 320));
  }
  app.addComponents();
  app.setVisible(true);
  app.afterSetVisible();
  app.currentView.requestFocus();
 }

 public void fullscreen()
 {
  GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
  gd.setFullScreenWindow(this);
 }

 public static App getSelf()
 {
  return self;
 }

 static 
 {
  cmiMaps = new CheckboxMenuItem[Config.maps.length];
 }

}
