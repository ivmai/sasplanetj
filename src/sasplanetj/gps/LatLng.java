package sasplanetj.gps;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class LatLng implements Cloneable, java.io.Serializable {

	private static final DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols(Locale.US);

	public static final DecimalFormat latlngFormat7 = new DecimalFormat("0.0000000", decimalSymbols);
	public static final DecimalFormat latlngFormat4 = new DecimalFormat("0.0000", decimalSymbols);

	// position
	public double lat; //latitude
	public double lng; //longitude

	public LatLng(){
	}

	public LatLng(double latitude, double longitude){
		this.lat = normCoord(latitude, 90);
		this.lng = normCoord(longitude, 180);
	}

	private static double normCoord(double v, double max) {
		double res = (v + max) % (max * 2);
		return res >= 0 ? res - max : res + max;
	}

	public void copyTo(LatLng other){
		other.lat = this.lat;
		other.lng = this.lng;
	}


	public String toString(){
		return "Lat "+latlngFormat7.format(this.lat)+" Lng "+latlngFormat7.format(this.lng);
	}

	public String toShortString(){
		return latlngFormat4.format(this.lat) + ", " + latlngFormat4.format(this.lng);
	}

	public boolean equalXY(LatLng other) {
		return this.lat==other.lat && this.lng==other.lng;
	}

}
