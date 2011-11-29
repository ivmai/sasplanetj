package org.sf.sasplanetj.util;

import org.sf.sasplanetj.gps.LatLng;

public class Waypoint {

	private final LatLng latLng;

	private final String name;

	public Waypoint(LatLng latLng, String name) {
		this.latLng = latLng;
		this.name = name;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public String getName() {
		return name;
	}
}
