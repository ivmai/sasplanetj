package sasplanetj.gps;

import java.text.DecimalFormat;

public class LatLng implements Cloneable, java.io.Serializable {

	public static final DecimalFormat latlngFormat7 = new DecimalFormat("#.0000000");
	public static final DecimalFormat latlngFormat4 = new DecimalFormat("#.0000");

	// position
	public double lat; //latitude
	public double lng; //longitude

	public LatLng(){
	}

	public LatLng(double latitude, double longitude){
		this.lat = latitude;
		this.lng = longitude;
	}

	/*
	public LatLng clone(){
		return new LatLng(this.lat, this.lng);
	}
	*/

	public void copyTo(LatLng other){
		other.lat = this.lat;
		other.lng = this.lng;
	}


	public String toString(){
		return "Lat "+latlngFormat7.format(this.lat)+" Lng "+latlngFormat7.format(this.lng);
	}

	public String toShortString(){
		return latlngFormat4.format(this.lat)+" "+latlngFormat4.format(this.lng);
	}

	public boolean equalXY(LatLng other) {
		return this.lat==other.lat && this.lng==other.lng;
	}

}
