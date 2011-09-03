package com.hollabaugh.montroseyardsalemap;

import java.util.StringTokenizer;

import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class YardSale {
	public Boolean friday;
	public String day;
	public String address;
	public String description;
	public GeoPoint latlng;
	public OverlayItem overlayitem;
	public Location location;
	public Boolean selected;
	public Boolean valid;

	public YardSale(String line) {
		StringTokenizer items;
		String lat, lng;
		items = new StringTokenizer(line, "|");
		if (items.countTokens() == 5) {
			day = items.nextToken();
			lat = items.nextToken();
			lng = items.nextToken();
			address = items.nextToken();
			description = items.nextToken();

			latlng = new GeoPoint((int) (Double.parseDouble(lat) * 1E6),
					(int) (Double.parseDouble(lng) * 1E6));
			overlayitem = new OverlayItem(latlng, address, description);
			friday = day.contains("Fri");
			location = new Location(address);
			location.setLatitude(Double.parseDouble(lat));
			location.setLongitude(Double.parseDouble(lng));
			selected = false;
			valid = true;
		} else {
			valid = false;
		}
	}
}
