package net.sf.sasplanetj;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import net.sf.sasplanetj.gps.GPSListener;
import net.sf.sasplanetj.gps.LatLng;
import net.sf.sasplanetj.gps.XY;
import net.sf.sasplanetj.ui.CenterOffsetBtn;
import net.sf.sasplanetj.ui.ColorsAndFonts;
import net.sf.sasplanetj.ui.NewWaypointDialog;
import net.sf.sasplanetj.ui.ShowMessage;
import net.sf.sasplanetj.ui.XYConstraints;
import net.sf.sasplanetj.ui.XYLayout;
import net.sf.sasplanetj.util.Config;
import net.sf.sasplanetj.util.TilesUtil;
import net.sf.sasplanetj.util.TrackTail;
import net.sf.sasplanetj.util.Tracks;
import net.sf.sasplanetj.util.Waypoints;
import net.sf.sasplanetj.util.WikiMapKML;
import net.sf.sasplanetj.util.Wikimapia;
import net.sf.sasplanetj.util.XYint;
import net.sf.sasplanetj.util.Zip;

/**
 * Panel. In CreME panel does not gain focus. But Container or Component flash
 * during repaint.
 */
public class Main extends Panel implements GPSListener, MouseListener,
		MouseMotionListener, KeyListener, ActionListener {

	private static final long serialVersionUID = -4867581244823612838L;

	private static final boolean debugMouseEvents = false;

	private static final String ZOOMIN_COMMAND = "ZOOMIN_COMMAND";
	private static final String ZOOMOUT_COMMAND = "ZOOMOUT_COMMAND";
	private static final String CREATE_WAYPOINT = "CREATE_WAYPOINT";
	private static final String GO_HERE = "GO_HERE";

	private LatLng clickLatlng; // hold last click coordinates
	private int viewOffsetStep = 50; // view offset on key press
	private static final CenterOffsetBtn offsetBtn = new CenterOffsetBtn();

	private final PopupMenu popup = new PopupMenu();
	private final ArrayList popupWiki = new ArrayList(); // ArrayList<MenuItem>

	private static TrackTail trackTail;
	private static LatLng latLng = new LatLng();
	private final XYint viewOffset = new XYint(0, 0);

	private boolean ignoreDblClick;

	private static final Method mouseGetButtonMethod; // null if no method

	static {
		Method m = null;
		// Check whether JRE MouseEvent class has getButton()
		try {
			m = MouseEvent.class.getMethod("getButton", new Class[0]);
		} catch (Exception e) {
			System.out.println("No MouseEvent.getButton()");
		}
		mouseGetButtonMethod = m;
	}

	Graphics dbf; // double buffer
	Image offscreen;

	private int skipCounter;

	private boolean isMousePressed;
	private boolean repaintOnLowResources;

	public Main() {
		if (!App.useAwtWorkaround()) {
			popup.addActionListener(this);
		}
		add(popup);

		offsetBtn.setVisible(false);
		trackTail = new TrackTail(Config.trackTailSize);

		setLayout(new XYLayout());

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);

		this.addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				System.out.println("Hidden");
			}

			public void componentShown(ComponentEvent e) {
				System.out.println("Shown");
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
				initDbf();
				if (offsetBtn.isVisible()) {
					remove(offsetBtn);

					Dimension offsetBtnSize = offsetBtn.getSize();
					add(Main.offsetBtn, new XYConstraints(getSize().width
							- offsetBtnSize.width - 5, getSize().height
							- offsetBtnSize.height - 5, offsetBtnSize.width,
							offsetBtnSize.height));
					validate();
				}
			}
		});
	}

	/**
	 * Initializes double buffer with new size
	 */
	private void initDbf() {
		System.out.println("Main size " + getSize().width + "x"
				+ getSize().height);
		if (this.getSize().width <= 0 || this.getSize().height <= 0)
			return;

		if (offscreen != null) {
			if (offscreen.getWidth(null) == this.getWidth()
					&& offscreen.getHeight(null) == this.getHeight())
				return;
		}
		if (offscreen != null) {
			offscreen.flush();
		}
		offscreen = createImage(this.getSize().width, this.getSize().height);
		dbf = offscreen.getGraphics();
	}

	public Dimension getPreferredSize() {
		return getSize();
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		if (offscreen == null) {
			System.err.println("offscreen==null");
			return;
		}

		try {
			paintInner(g);
		} catch (Error e) {
			System.err.println("paint error: " + e);
			if (repaintOnLowResources || !isLowResourceExc(e)) {
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

	private static boolean isLowResourceExc(Error e) {
		String errMsg;
		return e instanceof OutOfMemoryError
				|| (e.getClass().getName().endsWith(".SWTError")
						&& (errMsg = e.getMessage()) != null && errMsg
						.startsWith("No more"));
	}

	private static void clearCache() {
		Zip.zipsCache.clearAll();
		TilesUtil.tilesCache.clearAll();
		TilesUtil.zipExistanceCache.clearAll();
		Wikimapia.kmlCache.clearAll();

		System.gc();
		Runtime.getRuntime().runFinalization();
		System.gc(); // Collect garbage after finalization
	}

	private void paintInner(Graphics g) {
		final XYint center = new XYint(offscreen.getWidth(null) / 2,
				offscreen.getHeight(null) / 2);

		/* Tile calculation */
		final XYint displayXY = latLng.toDisplayCoord(Config.zoom);
		if (Config.drawTail && latLng.getLat() != 0 && latLng.getLng() != 0) {
			trackTail.addPoint(new XY(latLng.getLat(), latLng.getLng()));
		}
		displayXY.subtract(viewOffset);

		XYint centerTileTopLeft = new XYint(center.x
				- (displayXY.x & ((1 << TilesUtil.LOG2_TILESIZE) - 1)),
				center.y - (displayXY.y & ((1 << TilesUtil.LOG2_TILESIZE) - 1)));
		final XYint tileXY = TilesUtil.getTileByDisplayCoord(displayXY);
		XYint tileWikiXY = tileXY;
		if (Config.isCurMapYandex()) {
			XYint displayWikiXY = TilesUtil.coordinateToDisplay(
					latLng.getLat(), latLng.getLng(), Config.zoom, false);
			displayWikiXY.subtract(viewOffset);
			tileWikiXY = TilesUtil.getTileByDisplayCoord(displayWikiXY);
		}
		XYint[] matrix = TilesUtil.drawTilesArea(offscreen.getWidth(null),
				offscreen.getHeight(null), centerTileTopLeft, tileXY,
				tileWikiXY, dbf);

		// Draw coordinates
		if (Config.drawLatLng) {
			dbf.setColor(ColorsAndFonts.clLatLng);
			dbf.drawString(latLng.toString(), 3, 15);
		}

		// Draw track
		if (Config.drawTail) {
			trackTail.draw(dbf, matrix);
		}

		if (Tracks.tracks != null)
			Tracks.draw(dbf, matrix);
		if (Waypoints.points != null)
			Waypoints.draw(dbf, matrix);

		// Draw position
		drawPosition(dbf, center);

		// Draw double buffer image to our view
		g.drawImage(offscreen, 0, 0, this);
	}

	private void drawPosition(Graphics g, XYint position) {
		Rectangle r = new Rectangle(viewOffset.x + position.x - 3, viewOffset.y
				+ position.y - 3, 7, 7);
		g.setColor(ColorsAndFonts.clPositionBrush);
		g.fillOval(r.x, r.y, r.width, r.height);
		g.setColor(ColorsAndFonts.clPositionPen);
		g.drawOval(r.x, r.y, r.width, r.height);
	}

	public void gpsEvent(LatLng gi) {
		if (skipCounter == Config.drawMapSkip) {
			skipCounter = 0;
		} else {
			skipCounter++;
			return;
		}
		latLng = gi;
		repaint();
	}

	public static LatLng getLatLng() {
		return latLng;
	}

	public void registerListener() {
		if (App.serialReader != null)
			App.serialReader.addGPSListener(this);
	}

	public void removeListener() {
		if (App.serialReader != null)
			App.serialReader.removeGPSListener(this);
	}

	public void mouseClicked(MouseEvent e) {
		if (debugMouseEvents) {
			System.out.println("mouseClicked: " + e);
		}
	}

	private long prevClickTime = -1; // -1 means unset

	private static final int DBL_CLICK_MAX_DELAY_MS = 300;

	private boolean doPopup(MouseEvent e) {
		boolean isPopup;
		if (!ignoreDblClick
				&& System.getProperty("java.vm.name").startsWith("CrE-ME")) {
			isPopup = true;
		} else if (e.isPopupTrigger()
				|| e.getModifiers() == InputEvent.META_MASK) {
			isPopup = true;
			ignoreDblClick = true;
		} else {
			isPopup = false;
			if (mouseGetButtonMethod != null) {
				try {
					int button = ((Integer) mouseGetButtonMethod.invoke(e,
							new Object[0])).intValue();
					if (button == 2 || button == 3) {
						isPopup = true;
						ignoreDblClick = true;
					}
				} catch (Exception ee) {
					// Ignore.
				}
			}
			if (!ignoreDblClick && e.getModifiers() == InputEvent.BUTTON1_MASK) {
				switch (e.getClickCount()) {
				case 1:
					long time = System.currentTimeMillis();
					if (prevClickTime != -1 && time != prevClickTime
							&& time - prevClickTime <= DBL_CLICK_MAX_DELAY_MS) {
						isPopup = true;
						prevClickTime = -1;
					} else {
						prevClickTime = time;
					}
					break;
				case 2:
					isPopup = true;
					break;
				}
			}
		}

		if (isPopup && !App.useAwtWorkaround()) {
			try {
				doPopupInner(e);
			} catch (Error e2) {
				System.err.println("doPopup error: " + e2);
				if (!repaintOnLowResources && isLowResourceExc(e2)) {
					clearCache();
				}
				getToolkit().beep();
			}
		}
		return isPopup;
	}

	private void doPopupInner(MouseEvent e) {
		popup.removeAll();
		setClickLatlng(e);

		popup.add(new MenuItem(clickLatlng.toShortString()));
		App.menuAddItem("Zoom in", ZOOMIN_COMMAND, popup);
		App.menuAddItem("Zoom out", ZOOMOUT_COMMAND, popup);
		App.menuAddItem("Go here", GO_HERE, popup);
		App.menuAddItem("Create waypoint", CREATE_WAYPOINT, popup);

		int px = e.getX();
		if (Config.drawWikimapia) {
			Point point = new Point(px
					+ ((TilesUtil.lastWikiEndX - px - 1) & ~((1 << (Config.zoom
							+ TilesUtil.LOG2_TILESIZE - 1)) - 1)), e.getY());
			popupWiki.clear();
			Hashtable wikiStrSet = new Hashtable();
			int i = 0;
			for (Iterator iterator = Wikimapia.drawnKmlsIterator(); iterator
					.hasNext();) {
				WikiMapKML kml = (WikiMapKML) iterator.next();
				if (kml.containsPoint(point)) {
					String name = kml.getDescription();
					if (wikiStrSet.put(name, "") == null) {
						MenuItem mi = new MenuItem(name);
						mi.setActionCommand("POPUP_WIKI" + i);
						popupWiki.add(mi);
					}
					i++;
				}
			}
			if (popupWiki.size() > 0)
				popup.addSeparator();
			for (i = 0; i < popupWiki.size(); i++) {
				popup.add((MenuItem) popupWiki.get(i));
			}
		}

		popup.show(this, px + 5, e.getY());
	}

	private void setClickLatlng(MouseEvent e) {
		XYint displayXY = latLng.toDisplayCoord(Config.zoom);
		displayXY.subtract(viewOffset);
		XYint clickOffset = new XYint(e.getPoint().x - getSize().width / 2,
				e.getPoint().y - getSize().height / 2);
		displayXY.add(clickOffset);
		clickLatlng = TilesUtil.displayToCoordinate(displayXY, Config.zoom,
				Config.isCurMapYandex());
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	private final XYint mouseDragPrevXY = new XYint(0, 0);

	/*
	 * BUTTON1_DOWN_MASK is declared here as it may be missing in JRE InputEvent
	 * class.
	 */
	private static final int BUTTON1_DOWN_MASK = 1 << 10;

	public void mousePressed(MouseEvent e) {
		if (debugMouseEvents) {
			System.out.println("mousePressed: " + e);
		}
		isMousePressed = false;
		if (!doPopup(e)
				&& (e.getModifiers() & (InputEvent.BUTTON1_MASK | BUTTON1_DOWN_MASK)) != 0) {
			isMousePressed = true;
		}
		mouseDragPrevXY.setLocation(e.getX(), e.getY());
	}

	public void mouseReleased(MouseEvent e) {
		if (debugMouseEvents) {
			System.out.println("mouseReleased: " + e);
		}
		isMousePressed = false;
	}

	public void mouseDragged(MouseEvent e) {
		if (debugMouseEvents) {
			System.out.println("mouseDragged: " + e);
		}
		if (isMousePressed || mouseGetButtonMethod != null) {
			handleDrag(e);
		}
	}

	private void handleDrag(MouseEvent e) {
		XYint deltadrag = new XYint(e.getX() - mouseDragPrevXY.x, e.getY()
				- mouseDragPrevXY.y);
		viewOffset.add(deltadrag);
		viewOffsetChanged();

		mouseDragPrevXY.setLocation(e.getX(), e.getY());
	}

	public void mouseMoved(MouseEvent e) {
		if (isMousePressed) {
			if (debugMouseEvents) {
				System.out.println("mouseMoved (pressed state): " + e);
			}
			handleDrag(e);
		}
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
			switch (keyCode) {
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

		switch (keyCode) {
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
			System.gc(); // Free unused objects before showing heap usage
			printHeapStat();
			break;
		case KeyEvent.VK_1:
			switchMapToAndRepaint(Config.getCurMapIndex() + 1);
			break;
		case KeyEvent.VK_2:
			switchMapToAndRepaint(Config.getCurMapIndex() - 1);
			break;
		default:
			break;
		}
	}

	private static void printHeapStat() {
		long totalMemSize = Runtime.getRuntime().totalMemory();
		System.out
				.println("Memory heap used/total: "
						+ (int) ((totalMemSize - Runtime.getRuntime()
								.freeMemory()) >> 10) + "/"
						+ (int) (totalMemSize >> 10) + " KiB");
	}

	private void switchMapToAndRepaint(int mapIndex) {
		App.cmiCurMapSetState(false);
		Config.switchMapTo(mapIndex < 0 ? Config.maps.length - 1
				: mapIndex < Config.maps.length ? mapIndex : 0);
		App.cmiCurMapSetState(true);
		repaint();
	}

	public void viewOffsetChanged() {
		viewOffset.setLocation(
				TilesUtil.adjustViewOfsX(viewOffset.getX(), Config.zoom),
				viewOffset.getY());
		if (offsetBtn.isImageLoaded()) {
			boolean found = false;
			for (int i = 0; i < this.getComponentCount(); i++) {
				if (this.getComponent(i) == offsetBtn) {
					found = true;
					break;
				}
			}
			if (viewOffset.x != 0 || viewOffset.y != 0) {
				if (!found) {
					Dimension offsetBtnSize = offsetBtn.getSize();
					add(offsetBtn, new XYConstraints(getSize().width
							- offsetBtnSize.width - 5, getSize().height
							- offsetBtnSize.height - 5, offsetBtnSize.width,
							offsetBtnSize.height));
					offsetBtn.setVisible(true);
				}
			} else {
				if (found) {
					remove(offsetBtn);
					offsetBtn.setVisible(false);
				}
			}
		}
		validate();
		repaint();
		this.requestFocus();
	}

	public void viewOffset0() {
		viewOffset.setLocation(0, 0);
		viewOffsetChanged();
	}

	void zoomTo(int zoom) {
		double deltaview = Math.pow(2, zoom - Config.zoom);
		viewOffset.multiply(deltaview);
		Config.zoom = zoom;
		App.getSelf().zoomMenu();
		repaint();
	}

	public void keyTyped(KeyEvent e) {
		char ch = e.getKeyChar();
		if (ch != '\n' && ch != '1' && ch != '2')
			System.out.println("keyTyped=" + e);
	}

	private void processGoHere() {
		clickLatlng.copyTo(latLng);
		clearTrackTail();
		viewOffset0(); // it will repaint also
		repaint();
	}

	public static void clearTrackTail() {
		trackTail.clear();
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.startsWith("POPUP_WIKI")) {
			int i = Integer.valueOf(command.substring("POPUP_WIKI".length()))
					.intValue();
			new ShowMessage(((MenuItem) popupWiki.get(i)).getLabel());
		} else if (command == ZOOMIN_COMMAND) {
			App.zoomIn();
		} else if (command == ZOOMOUT_COMMAND) {
			App.zoomOut();
		} else if (command == GO_HERE) {
			processGoHere();
		} else if (command == CREATE_WAYPOINT) {
			new NewWaypointDialog(App.getSelf(), clickLatlng).setVisible(true);
		}
	}

}
