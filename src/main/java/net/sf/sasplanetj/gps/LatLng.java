package net.sf.sasplanetj.gps;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.sf.sasplanetj.util.Config;
import net.sf.sasplanetj.util.TilesUtil;
import net.sf.sasplanetj.util.XYint;

public class LatLng {

	private static final DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols(
			Locale.US);

	private static final DecimalFormat latlngFormat7 = new DecimalFormat(
			"0.0000000", decimalSymbols);
	private static final DecimalFormat latlngFormat4 = new DecimalFormat(
			"0.0000", decimalSymbols);

	// position
	private double lat; // latitude
	private double lng; // longitude

	public LatLng() {
	}

	public LatLng(double latitude, double longitude) {
		set(latitude, longitude);
	}

	public void set(double latitude, double longitude) {
		lat = normCoord(latitude, 90);
		lng = normCoord(longitude, 180);
	}

	private static double normCoord(double v, double max) {
		double res = (v + max) % (max * 2);
		return res >= 0 ? res - max : res + max;
	}

	public void copyTo(LatLng other) {
		other.lat = lat;
		other.lng = lng;
	}

	public XYint toDisplayCoord(int zoom) {
		return TilesUtil.coordinateToDisplay(getLat(), getLng(), zoom,
				Config.isCurMapYandex());
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}

	public static String formatP7d(double v) {
		return latlngFormat7.format(v);
	}

	public String toString() {
		return "Lat " + formatP7d(getLat()) + " Lng " + formatP7d(getLng());
	}

	public String toShortString() {
		return latlngFormat4.format(getLat()) + ", "
				+ latlngFormat4.format(getLng());
	}

	public boolean equalXY(LatLng other) {
		return lat == other.lat && lng == other.lng;
	}
}
