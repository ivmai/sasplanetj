package net.sf.sasplanetj.util;

import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import net.sf.sasplanetj.ui.ColorsAndFonts;

public class Wikimapia {

	public static Cache kmlCache; // <String, ArrayList<WikiMapKML>>

	private static final String mapDir = "Wiki";

	/**
	 * last drawn KMLs
	 */
	private static final ArrayList drawnKmls = new ArrayList(); // <WikiMapKML>

	public static void clearDrawnKmls() {
		drawnKmls.clear();
	}

	public static Iterator drawnKmlsIterator() {
		return drawnKmls.iterator();
	}

	public static String getCachePath(int x, int y, int zoom) {
		// /cache/Wiki/z17/37/x38343/21/y22152.kml
		return Config.cachePath + StringUtil.fileSep + mapDir
				+ StringUtil.fileSep + "z" + Config.zoom + StringUtil.fileSep
				+ (x / 1024) + StringUtil.fileSep + "x" + x
				+ StringUtil.fileSep + (y / 1024) + StringUtil.fileSep + "y"
				+ y + ".kml";
	}

	public static ArrayList getTileKML(int x, int y, int zoom, boolean isYandex) {
		String filename = getCachePath(x, y, zoom);
		String entryName = isYandex ? filename + "?Y" : filename;
		ArrayList kmls = (ArrayList) kmlCache.get(entryName);

		if (kmls == null) {
			String kmlstr = loadKML(filename);
			kmls = parse(kmlstr, zoom, isYandex);
			kmlCache.put(entryName, kmls, false);
		}
		return kmls;
	}

	private static ArrayList parse(String kmlstr, int zoom, boolean isYandex)
			throws NumberFormatException {
		if (kmlstr == null)
			return null;

		int totalCoordCnt = 0;
		ArrayList kmls = new ArrayList(1);
		StopWatch watch = new StopWatch();
		watch.start();

		int descrPos = 0;
		while ((descrPos = kmlstr.indexOf("<description>", descrPos + 1)) != -1) {
			int descrTextStart = kmlstr.indexOf("<![CDATA[", descrPos + 1)
					+ "<![CDATA[".length();
			int descrTextEnd = kmlstr.indexOf("<br>", descrTextStart + 1); // ]]>
			String description = StringUtil.replace(
					kmlstr.substring(descrTextStart, descrTextEnd).trim(),
					"&amp;quot;", "\"");

			int coordinatesStart = kmlstr
					.indexOf("<coordinates>", descrTextEnd)
					+ "<coordinates>".length();
			int coordinatesEnd = kmlstr.indexOf("</coordinates>",
					coordinatesStart + 1);
			String coordsStr = kmlstr.substring(coordinatesStart,
					coordinatesEnd).trim();

			StringTokenizer st1 = new StringTokenizer(coordsStr, "\n");
			int coordsCnt = st1.countTokens();
			totalCoordCnt += coordsCnt;
			int[] kmlX = new int[coordsCnt];
			int[] kmlY = new int[coordsCnt];
			double prevLng = 0.0;

			for (int i = 0; i < coordsCnt; i++) {
				String coord = st1.nextToken();
				int comma1 = coord.indexOf(',', 1);
				int comma2 = coord.indexOf(',', comma1 + 1);
				// lat/lng order is twisted
				double lng = Double.valueOf(coord.substring(0, comma1))
						.doubleValue();
				if (Math.abs(prevLng - lng) > 180.0) {
					lng = lng >= 0.0 ? lng - 360.0 : lng + 360.0;
				}
				double lat = Double
						.valueOf(coord.substring(comma1 + 1, comma2))
						.doubleValue();
				prevLng = lng;
				XYint intile = TilesUtil.coordinateToDisplay(lat, lng, zoom,
						isYandex);
				kmlX[i] = intile.x;
				kmlY[i] = intile.y;
			}
			descrPos = coordinatesEnd;
			WikiMapKML kml = new WikiMapKML(description, kmlX, kmlY);
			kmls.add(kml);
		}

		if (kmls.size() == 0)
			return null;
		System.out.println("Wikimapia: Parsed " + totalCoordCnt
				+ " coordinates in " + watch.currentMillis() + "ms");
		return kmls;
	}

	public static String loadKML(String filename) {
		// Check if there are any zips instead of directories
		String[] zipnames = TilesUtil.checkZipFileExistance(filename);
		if (zipnames != null) {
			byte[] binary = Zip.getZipFile(zipnames[0], zipnames[1]);
			if (binary != null)
				return utf8ToString(binary);
		}
		return loadKML_FS(filename);
	}

	private static String utf8ToString(byte buf[]) {
		String str = null;
		try {
			str = new String(buf, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.err.println(e);
		}
		return str;
	}

	public static String loadKML_FS(String filename) {
		File f = new File(filename);
		if (!f.exists()) {
			System.out.println("Wikimapia: " + filename + " does not exist");
			return null;
		}
		try {
			int len = (int) f.length();
			DataInputStream is = new DataInputStream(new FileInputStream(
					filename));
			byte buf[] = new byte[len];
			is.readFully(buf);
			is.close();
			return utf8ToString(buf);
		} catch (IOException e) {
			System.err.println("Wikimapia loadKML: " + e);
		}
		return null;
	}

	public static void drawTile(int x, int y, int zoom, Graphics dbf,
			XYint[] matrix) {
		ArrayList kmlsForTile = Wikimapia.getTileKML(
				TilesUtil.adjustTileX(x, zoom), y, zoom,
				Config.isCurMapYandex());
		if (kmlsForTile != null) {
			int shiftX = (x & ~((1 << (zoom - 1)) - 1)) << TilesUtil.LOG2_TILESIZE;
			dbf.setColor(ColorsAndFonts.clWikimapia);
			for (Iterator iterator = kmlsForTile.iterator(); iterator.hasNext();) {
				WikiMapKML kml = (WikiMapKML) iterator.next();
				kml.drawPolygon(dbf, shiftX - matrix[0].x + matrix[1].x,
						-matrix[0].y + matrix[1].y);
			}
			drawnKmls.addAll(kmlsForTile);
		}
	}

}
