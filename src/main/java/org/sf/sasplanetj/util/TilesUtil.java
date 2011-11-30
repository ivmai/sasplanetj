package org.sf.sasplanetj.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;

import org.sf.sasplanetj.App;
import org.sf.sasplanetj.gps.LatLng;
import org.sf.sasplanetj.ui.ColorsAndFonts;

public class TilesUtil {

	private static class ImageContainer {

		private final Image img;

		ImageContainer(Image image) {
			img = image;
		}

		Image getImage() {
			return img;
		}

		protected void finalize() {
			if (img != null) {
				img.flush();
			}
		}
	}

	public static final int ZOOM_MAX = 23;

	public static final int LOG2_TILESIZE = 8;
	private static final int TILESIZE = 1 << LOG2_TILESIZE;

	public static Cache tilesCache; // <String, Image>

	public static final Cache zipExistanceCache = new Cache(200);

	private static final boolean useMathSinH;

	public static int lastWikiEndX;

	static {
		boolean hasSinH = false;
		// Check whether Math class has sinh()
		try {
			Math.sinh(0);
			hasSinH = true;
		} catch (NoSuchMethodError e) {
			System.out.println("No Math.sinh()");
		}
		useMathSinH = hasSinH;
	}

	private TilesUtil() {
	}

	/**
	 * Math atanh() emulation.
	 * http://sasgis.ru/2009/05/27/yandekskarty-na-google-api/#more-248 Yandex
	 * maps have ellipse projection
	 */
	private static double atanh(double x) {
		return Math.log((1 + x) / (1 - x)) / 2;
	}

	private static final double exct = 0.081819699999999995;

	public static XYint coordinateToDisplay(double lat, double lng, int zoom,
			boolean isYandex) {
		double quarterPixelsAtZoom = (double) (1 << (zoom + (LOG2_TILESIZE - 2)));
		double projection;
		if (isYandex) {
			double z = Math.sin(Math.toRadians(lat));
			projection = atanh(z) - exct * atanh(exct * z);
		} else {
			projection = Math.log(Math.tan(Math.toRadians(lat) / 2 + Math.PI
					/ 4));
		}
		double x = (lng / 180.0 + 1) * quarterPixelsAtZoom;
		double y = (1 - projection / Math.PI) * quarterPixelsAtZoom;
		return new XYint((int) x, (int) y);
	}

	/** returns Tile XY with given display coordinates */
	public static XYint getTileByDisplayCoord(XYint p) {
		return new XYint(p.x >> LOG2_TILESIZE, p.y >> LOG2_TILESIZE);
	}

	public static int adjustTileX(int x, int zoom) {
		int max = 1 << (zoom - 1);
		x %= max;
		return x >= 0 ? x : max + x;
	}

	private static int maxTileY(int zoom, boolean isYandex) {
		return (int) (1.15 * (1 << (zoom - 1)));
	}

	public static int adjustViewOfsX(int x, int zoom) {
		int max = 1 << (zoom + (LOG2_TILESIZE - 2));
		int res = (x + max) % (max << 1);
		return res >= 0 ? res - max : res + max;
	}

	/**
	 * Math.expm1() emulation.
	 */
	private static double emulate_expm1(double a) {
		double v = Math.exp(a) - 1;
		return v != 0 ? v : a;
	}

	/**
	 * Math.sinh() emulation.
	 */
	private static double emulate_sinh(double a) {
		boolean isneg = false;
		if (a < 0.0) {
			isneg = true;
			a = -a;
		}

		double v = emulate_expm1(a);
		if (v < Double.POSITIVE_INFINITY) {
			v = (v / (v + 1) + v) / 2;
		} else {
			v = Math.exp(a / 2);
			v = (v / 2) * v;
		}

		return isneg ? -v : v;
	}

	public static LatLng displayToCoordinate(XYint point, int zoom,
			boolean isYandex) {
		double quarterPixelsAtZoom = (double) (1 << (zoom + (LOG2_TILESIZE - 2)));
		double lng = (point.x / quarterPixelsAtZoom - 1) * 180;
		if (isYandex) {
			double yy = (point.y - quarterPixelsAtZoom) * 2;
			boolean isLatNeg = false;
			if (yy > 0) {
				isLatNeg = true;
				yy = -yy;
			}
			double latRad = Math.atan(Math.exp(yy
					/ (quarterPixelsAtZoom * (-2 / Math.PI))))
					* 2 - Math.PI / 2;
			double k = -Math.PI / quarterPixelsAtZoom;
			double prev;

			int i = 1000;
			do {
				prev = latRad;
				double sinPrev = Math.sin(prev);
				latRad = Math.asin(1
						- (sinPrev + 1)
						* Math.pow(1 - exct * sinPrev, exct)
						/ (Math.exp(yy * k) * Math
								.pow(1 + exct * sinPrev, exct)));
			} while (Math.abs(prev - latRad) > 1e-9 && --i > 0);

			if (i >= 0 && !Double.isNaN(latRad))
				return new LatLng(Math.toDegrees(isLatNeg ? -latRad : latRad),
						lng);

			System.out.println("Failed to calculate lattitude for y=" + point.y
					+ ", zoom=" + zoom);
		}

		double projection = (1 - point.y / quarterPixelsAtZoom) * Math.PI;
		return new LatLng(Math.toDegrees(Math.atan(useMathSinH ? Math
				.sinh(projection) : emulate_sinh(projection))), lng);
	}

	private static String getCachePath(int x, int y, int zoom) {
		// sasplanet\cache\sat\z17\37\x38349\21\y22110.jpg
		// result:=path+'\z'+zoom+'\'+(x div 1024)+'\x'+x+'\'+(y div
		// 1024)+'\y'+y+ext;
		return Config.cachePath + StringUtil.fileSep + Config.getCurMapDir()
				+ StringUtil.fileSep + "z" + Config.zoom + StringUtil.fileSep
				+ (x / 1024) + StringUtil.fileSep + "x" + x
				+ StringUtil.fileSep + (y / 1024) + StringUtil.fileSep + "y"
				+ y + "." + Config.getCurMapExt();
	}

	private static ImageContainer getTileImage(int x, int y, int zoom) {
		if (zoom < Config.getCurMinZoom())
			return null;

		String filename = getCachePath(x, y, zoom);
		ImageContainer img = (ImageContainer) tilesCache.get(filename);
		Image image;

		if (img == null && (image = loadImage(filename)) != null) {
			img = new ImageContainer(image);
			tilesCache.put(filename, img, Config.useSoftRefs);
		}
		return img;
	}

	private static Image loadImage(String filename) {
		/* Check if there are any zips instead of directories */
		String[] zipnames = checkZipFileExistance(filename);
		if (zipnames != null) {
			byte[] binary = Zip.getZipFile(zipnames[0], zipnames[1]);
			if (binary == null) {
				return loadImageFromFS(filename);
			}

			// Ensure that image is created. Otherwise sometimes there is white
			// areas painted instead of image.
			Image img = Toolkit.getDefaultToolkit().createImage(binary);
			return waitForImageLoaded(img) ? img : null;
		} else {
			return loadImageFromFS(filename);
		}
	}

	private static boolean waitForImageLoaded(Image img) {
		MediaTracker mt = new MediaTracker(App.getSelf());
		mt.addImage(img, 0);
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (img.getWidth(null) == TILESIZE && img.getHeight(null) == TILESIZE
				&& (mt.statusAll(false) & MediaTracker.COMPLETE) != 0)
			return true;
		img.flush();
		return false;
	}

	/**
	 * Will return {zipname, filename} or null For some reason fileSep in zips
	 * is / always, never \
	 */
	public static String[] checkZipFileExistance(String filename) {
		String zipnames[] = (String[]) zipExistanceCache.get(filename);
		if (zipnames != null)
			return zipnames;

		String[] splits = StringUtil.split(
				filename.substring(Config.curDir.length()), StringUtil.fileSep);
		String prepath = Config.curDir + StringUtil.fileSep;
		for (int i = 0; i < splits.length - 1; i++) {
			// skip last which is filename itself
			if (new File(prepath + splits[i]).exists()) {
				prepath += splits[i] + StringUtil.fileSep;
				continue;
			} else { // check for zip
				if (new File(prepath + splits[i] + ".zip").exists()) {
					String zipname = prepath + splits[i] + ".zip";
					StringBuffer sb = new StringBuffer();
					for (int j = i; j < splits.length; j++) {
						sb.append(splits[j]);
						if (j < splits.length - 1) {
							sb.append('/');
						}
					}
					String fileinzip = sb.toString();
					zipnames = new String[] { zipname, fileinzip };
					zipExistanceCache.put(filename, zipnames, false);
					return zipnames;
				}
			}
		}
		return null;
	}

	private static Image loadImageFromFS(String filename) {
		if (!App.useAwtWorkaround() || (new File(filename)).isFile()) {
			Image img = Toolkit.getDefaultToolkit().getImage(filename);
			if (waitForImageLoaded(img))
				return img;
		}
		System.out.println("Cannot load map tile: " + filename);
		return null;
	}

	/**
	 * calculate how much tiles needed to cover the area
	 */
	public static XYint[] drawTilesArea(int width, int height,
			XYint centerTileTopLeft, XYint tileXY, XYint tileWikiXY,
			Graphics dbf) {
		int tilesToLeft = centerTileTopLeft.x <= 0 ? 0
				: (centerTileTopLeft.x + (TILESIZE - 1)) >> LOG2_TILESIZE;
		int tilesToRight = centerTileTopLeft.x + TILESIZE >= width ? 0 : (width
				- centerTileTopLeft.x - 1) >> LOG2_TILESIZE;

		int tilesToTop = centerTileTopLeft.y <= 0 ? 0
				: (centerTileTopLeft.y + (TILESIZE - 1)) >> LOG2_TILESIZE;
		int tilesToBottom = centerTileTopLeft.y + TILESIZE >= height ? 0
				: (height - centerTileTopLeft.y - 1) >> LOG2_TILESIZE;

		int totalMatrixW = tilesToLeft + tilesToRight + 1;
		int totalMatrixH = tilesToTop + tilesToBottom + 1;

		int matrixX = centerTileTopLeft.x - (tilesToLeft << LOG2_TILESIZE);
		int matrixY = centerTileTopLeft.y - (tilesToTop << LOG2_TILESIZE);

		XYint[] matrix = {
				new XYint((tileXY.x - tilesToLeft) << LOG2_TILESIZE,
						(tileXY.y - tilesToTop) << LOG2_TILESIZE),
				new XYint(matrixX, matrixY) };

		Wikimapia.clearDrawnKmls();
		int zoom = Config.zoom;
		int maxY = maxTileY(zoom, Config.isCurMapYandex());
		int startY = tileXY.y - tilesToTop;

		for (int i = tileXY.x - tilesToLeft; i < tileXY.x - tilesToLeft
				+ totalMatrixW; i++) {
			for (int j = startY; j < startY + totalMatrixH; j++) {
				ImageContainer img = null;
				if (j >= 0 && j < maxY) {
					img = getTileImage(adjustTileX(i, zoom), j, zoom);
				}
				int x = matrixX
						+ ((i - tileXY.x + tilesToLeft) << LOG2_TILESIZE);
				int y = matrixY
						+ ((j - tileXY.y + tilesToTop) << LOG2_TILESIZE);

				if (img != null) {
					dbf.drawImage(img.getImage(), x, y, null);
				} else {
					dbf.setColor(ColorsAndFonts.clImageNotFound);
					dbf.fillRect(x, y, TILESIZE, TILESIZE);
				}

				if (Config.drawGrid) {
					dbf.setColor(ColorsAndFonts.clGrid);
					dbf.drawRect(x, y, TILESIZE, TILESIZE);
				}
			}
		}

		if (Config.drawWikimapia) {
			startY = tileWikiXY.y - tilesToTop;
			int endY = Math.min(startY + totalMatrixH,
					Config.isCurMapYandex() ? maxTileY(zoom, false) : maxY);
			if (startY < 0) {
				startY = 0;
			}
			int startX = tileWikiXY.x - tilesToLeft;
			for (int i = startX; i < startX + totalMatrixW; i++) {
				for (int j = startY; j < endY; j++) {
					Wikimapia.drawTile(i, j, zoom, dbf, matrix);
				}
			}
			lastWikiEndX = matrixX + (totalMatrixW << LOG2_TILESIZE);
		}

		/**
		 * 1) matrix display coordinates (map pixel) 2) matrix screen offset
		 * coordinates
		 */
		return matrix;
	}
}
