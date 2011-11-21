package sasplanetj.util;
/*
 * http://www.fermoll.de/ozi_ce/formate.htm
 *
 * Track File (.plt)

Line 1 : File type and version information
Line 2 : Geodetic Datum used for the Lat/Lon positions for each trackpoint
Line 3: "Altitude is in feet" - just a reminder that the altitude is always stored in feet
Line 4: Reserved for future use
Line 5 : multiple fields as below

Field 1 : always zero (0)
Field 2 : width of track plot line on screen - 1 or 2 are usually the best
Field 3 : track color (RGB)
Field 4 : track description (no commas allowed)
Field 5 : track skip value - reduces number of track points plotted, usually set to 1
Field 6 : track type - 0 = normal , 10 = closed polygon , 20 = Alarm Zone
Field 7 : track fill style - 0 =bsSolid; 1 =bsClear; 2 =bsBdiagonal; 3 =bsFdiagonal; 4 =bsCross;
5 =bsDiagCross; 6 =bsHorizontal; 7 =bsVertical;
Field 8 : track fill color (RGB)

Line 6 : Number of track points in the track, not used, the number of points is determined when reading the points file

Trackpoint data
One line per trackpoint
each field separated by a comma
non essential fields need not be entered but comma separators must still be used (example ,,)
defaults will be used for empty fields

Field 1 : Latitude - decimal degrees.
Field 2 : Longitude - decimal degrees.
Field 3 : Code - 0 if normal, 1 if break in track line
Field 4 : Altitude in feet (-777 if not valid)
Field 5 : Date - see Date Format below, if blank a preset date will be used
Field 6 : Date as a string
Field 7 : Time as a string

Note that OziExplorer reads the Date/Time from field 5, the date and time in fields 6 & 7 are ignored.

Example
-27.350436, 153.055540,1,-777,36169.6307194, 09-Jan-99, 3:08:14
-27.348610, 153.055867,0,-777,36169.6307194, 09-Jan-99, 3:08:14
 *
 */
import java.io.*;

import sasplanetj.App;
import sasplanetj.gps.*;

public class TrackLogger implements GPSListener{
	public static final TrackLogger self = new TrackLogger();


	public static final String logFilename = "track.plt";

	public static final File f = new File(logFilename);
	public static FileWriter out;

	//public static LatLng latlng = new LatLng();

	public static int breakTrack = 1;

	private static int skipCounter = 0;

	public void gpsEvent(LatLng latlng) {
		if (skipCounter==Config.trackLogSkip){
			skipCounter=0;
		}else{
			skipCounter++;
			return;
		}
		//TrackLogger.latlng = latlng;
		try {
			out.write(LatLng.latlngFormat7.format(latlng.lat)+", "+LatLng.latlngFormat7.format(latlng.lng)+", "+breakTrack+"\r\n");
			breakTrack = 0;
			out.flush();
		} catch (IOException e) {
			System.out.println("TrackLogger: error writing to "+logFilename);
		}
	}


	public static void loggerStart(){
		if (!Config.connectGPS) return;
		if (out==null){
			try {
				if (!f.exists()) {
					out = new FileWriter(logFilename, true);
					out.write("OziExplorer Track Point File Version 2.0\r\n");
					out.write("WGS 84\r\n");
					out.write("Altitude is in Feet\r\n");
					out.write("Reserved 3\r\n");
					out.write("0,2,255,sasplanetJ Track Log File,1\r\n");
					out.write("0\r\n");
					out.flush();
				}else{
					out = new FileWriter(logFilename, true);
				}
				breakTrack = 1;
				App.serialReader.addGPSListener(self);
				App.cmiTrackLog.setState(Config.trackLog);
			} catch (IOException e) {
				System.out.println("TrackLogger: error opening "+logFilename);
			}
		}
	}

	public static void loggerStop(){
		if (out!=null){
			App.serialReader.removeGPSListener(self);
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
				System.out.println("TrackLogger: error flushing "+logFilename);
			}
			out = null;
		}
		App.cmiTrackLog.setState(Config.trackLog);
	}

}
