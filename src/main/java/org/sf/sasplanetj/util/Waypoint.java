package org.sf.sasplanetj.util;

import org.sf.sasplanetj.gps.LatLng;

public class Waypoint {
	public String name;
	public LatLng latlng;

	public Waypoint() {
	}

	public Waypoint(LatLng latlng, String name) {
		this.latlng = latlng;
		this.name = name;
	}

}
