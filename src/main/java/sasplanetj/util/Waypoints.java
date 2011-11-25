package sasplanetj.util;

/*
 * Waypoint File (.wpt)

 Line 1 : File type and version information
 Line 2: Geodetic Datum used for the Lat/Lon positions for each waypoint
 Line 3 : Reserved for future use
 Line 4 : GPS Symbol set - not used yet

 Waypoint data
 One line per waypoint
 each field separated by a comma
 comma's not allowed in text fields, character 209 can be used instead and a comma will be substituted.
 non essential fields need not be entered but comma separators must still be used (example ,,)
 defaults will be used for empty fields
 Any number of the last fields in a data line need not be included at all not even the commas.

 Field 1 : Number - this is the location in the array (max 1000), must be unique, usually start at 1 and increment. Can be set to -1 (minus 1) and the number will be auto generated.
 Field 2 : Name - the waypoint name, use the correct length name to suit the GPS type.
 Field 3 : Latitude - decimal degrees.
 Field 4 : Longitude - decimal degrees.
 Field 5 : Date - see Date Format below, if blank a preset date will be used
 Field 6 : Symbol - 0 to number of symbols in GPS
 Field 7 : Status - always set to 1
 Field 8 : Map Display Format
 Field 9 : Foreground Color (RGB value)
 Field 10 : Background Color (RGB value)
 Field 11 : Description (max 40), no commas
 Field 12 : Pointer Direction
 Field 13 : Garmin Display Format
 Field 14 : Proximity Distance - 0 is off any other number is valid
 Field 15 : Altitude - in feet (-777 if not valid)
 Field 16 : Font Size - in points
 Field 17 : Font Style - 0 is normal, 1 is bold.
 Field 18 : Symbol Size - 17 is normal size
 Field 19 : Proximity Symbol Position
 Field 20 : Proximity Time
 Field 21 : Proximity or Route or Both
 Field 22 : File Attachment Name
 Field 23 : Proximity File Attachment Name
 Field 24 : Proximity Symbol Name
 *
 */
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import sasplanetj.App;
import sasplanetj.gps.LatLng;
import sasplanetj.ui.ColorsAndFonts;
import sasplanetj.ui.ShowMessage;

public class Waypoints {
	public static ArrayList points = new ArrayList(); // <Waypoint>

	public static void load(String filename) {
		System.out.println("Waypoints: loading " + filename);

		if (points == null)
			points = new ArrayList();
		try {
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String s;
			// Read File Line By Line
			int lineCount = 0;
			while ((s = br.readLine()) != null) {
				lineCount++;
				if (lineCount <= 4)
					continue;
				String[] splits = StringUtil.split(s, ",");
				if (splits.length < 4) {
					System.out.println("Waypoints: wrong data on line "
							+ lineCount + ": " + s);
					continue;
				}
				Waypoint wp = new Waypoint();
				wp.name = splits[1].trim();
				wp.latlng = new LatLng(Double.valueOf(splits[2].trim())
						.doubleValue(), Double.valueOf(splits[3].trim())
						.doubleValue());
				points.add(wp);
			}
			// Close the input stream
			in.close();
			System.out.println("Waypoints: now we have " + points.size()
					+ " waypoints loaded");
			if (points.size() == 0)
				points = null;
			App.main.repaint();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Waypoints: error reading " + filename);
		}

	}

	public static void draw(Graphics dbf, XYint[] matrix) {
		/*
		 * TODO: skip WPs which are out of screen
		 */
		for (Iterator it = points.iterator(); it.hasNext();) {
			final Waypoint wp = (Waypoint) it.next();
			final XYint inmatrix = TilesUtil.coordinateToDisplay(wp.latlng.lat,
					wp.latlng.lng, Config.zoom, Config.isMapYandex);
			inmatrix.subtract(matrix[0]); // find point in tile matrix
			inmatrix.add(matrix[1]); // point with matrix position drawing
										// offset

			Rectangle r = new Rectangle(inmatrix.x - 3, inmatrix.y - 3, 7, 7);
			dbf.setColor(ColorsAndFonts.clWaypointBrush);
			dbf.fillOval(r.x, r.y, r.width, r.height);

			dbf.drawString(wp.name, inmatrix.x + 5, inmatrix.y + 5);

			dbf.setColor(ColorsAndFonts.clWaypointPen);
			dbf.drawOval(r.x, r.y, r.width, r.height);
		}
	}

	public static void save(String filename) {
		if (points == null || points.size() == 0)
			return;

		try {
			Writer out = new BufferedWriter(new FileWriter(filename, false));
			out.write("OziExplorer CE Waypoint File Version 1.2\r\n");
			out.write("WGS 84\r\n");
			out.write("Reserved 2\r\n");
			out.write("Reserved 3\r\n");
			out.flush();

			for (Iterator it = points.iterator(); it.hasNext();) {
				Waypoint wp = (Waypoint) it.next();
				out.write("-1," + wp.name + "," + wp.latlng.lat + ","
						+ wp.latlng.lng);
				out.write("\r\n");
			}
			out.close();
			System.out.println("Waypoint: saved " + points.size()
					+ " points to " + filename);
		} catch (IOException e) {
			new ShowMessage("Failed to write to file " + filename);
			System.err.println(e);
		}

	}

}
