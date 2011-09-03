package com.hollabaugh.montroseyardsalemap;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import com.hollabaugh.montroseyardsalemap.R;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class YardSaleMapActivity extends MapActivity {
    LocationManager lm;
    LocationListener locationListener;

    MapView mapView;
    MapController mc;
    GeoPoint p;
    MyLocationOverlay mylocationoverlay;
    String ysd = null;
    ArrayList<YardSale> yardsales;
    TextView status;
    CheckBox fricb, satcb, trackcb;
    ListView list;
    Button listbtn;
    static Boolean tracking;
    static Boolean friselected;
    static Boolean satselected;
    static Boolean firsttime = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.map);
	status = (TextView) findViewById(R.id.status);
	fricb = ((CheckBox) findViewById(R.id.togglefri));
	satcb = ((CheckBox) findViewById(R.id.togglesat));
	trackcb = ((CheckBox) findViewById(R.id.toggletrack));
	list = ((ListView) findViewById(R.id.list));
	listbtn = ((Button) findViewById(R.id.listbtn));

	yardsales = YardSaleListActivity.yardsales;
	lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	locationListener = new MyLocationListener();

	mapView = (MapView) findViewById(R.id.mapView);
	mapView.setBuiltInZoomControls(true);
	mylocationoverlay = new MyLocationOverlay(this, mapView);
	mylocationoverlay.enableMyLocation();
	// mylocationoverlay.enableCompass();
	mapView.getOverlays().add(mylocationoverlay);

	mc = mapView.getController();
	double lat = 38.482;
	double lng = -107.868;
	p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));

	if (firsttime == true) {
	    // montrose 38.48257,-107.86877
	    mc.setCenter(p);
	    mc.setZoom(13);
	    friselected = true;
	    satselected = false;
	    tracking = true;
	    firsttime = false;
	}

	fricb.setChecked(friselected);
	satcb.setChecked(satselected);
	trackcb.setChecked(tracking);

	this.addOverlays();
	mapView.invalidate();

	listbtn.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		lm.removeUpdates(locationListener);
		mylocationoverlay.disableMyLocation();
		// mylocationoverlay.disableCompass();
		Intent i = new Intent(YardSaleMapActivity.this,
			YardSaleListActivity.class);
		startActivity(i);
	    }
	});

	trackcb.setOnClickListener(new TrackHandler());

	final CheckBox togglefri = (CheckBox) findViewById(R.id.togglefri);
	togglefri.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		friselected = fricb.isChecked();
		addOverlays();
		mapView.invalidate();
	    }
	});
	final CheckBox togglesat = (CheckBox) findViewById(R.id.togglesat);
	togglesat.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		satselected = satcb.isChecked();
		addOverlays();
		mapView.invalidate();
	    }
	});
    }

    @Override
    protected void onResume() {
	super.onResume();
	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f,
		this.locationListener);
	mylocationoverlay.enableMyLocation();
	// mylocationoverlay.enableCompass();
    }

    @Override
    protected boolean isRouteDisplayed() {
	return false;
    }

    @Override
    protected void onPause() {
	super.onPause();
	mylocationoverlay.disableMyLocation();
	// mylocationoverlay.disableCompass();
	lm.removeUpdates(this.locationListener); // turn off gps
    }

    private class TrackHandler implements OnClickListener {
	public void onClick(View v) {
	    tracking = ((CheckBox) v).isChecked();
	}

    }

    @SuppressWarnings("unused")
    private class TrackHandler1 implements OnClickListener {
	public void onClick(View v) {
	    if (((CheckBox) v).isChecked()) {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
			10f, locationListener);
		mylocationoverlay.enableMyLocation();
		// mylocationoverlay.enableCompass();
	    } else {
		mylocationoverlay.disableMyLocation();
		// mylocationoverlay.disableCompass();
		lm.removeUpdates(locationListener);
	    }
	}
    }

    private class MyLocationListener implements LocationListener {
	public void onLocationChanged(Location loc) {
	    YardSale nearestyardsale = yardsales.get(0);
	    YardSale yardsale;
	    float mindistance = (float) 1E6;
	    float distance;

	    if (loc != null) {
		/*
		 * Toast.makeText( getBaseContext(), "Location changed : Lat: "
		 * + loc.getLatitude() + " Lng: " + loc.getLongitude(),
		 * Toast.LENGTH_SHORT).show();
		 */

		GeoPoint p = new GeoPoint((int) (loc.getLatitude() * 1E6),
			(int) (loc.getLongitude() * 1E6));
		if (tracking) {
		    mc.animateTo(p);
		}
		// mc.setZoom(16);
		mapView.invalidate();
		for (int i = 0; i < yardsales.size(); i++) {
		    yardsale = yardsales.get(i);
		    if ((yardsale.friday && fricb.isChecked())
			    || (!yardsale.friday && satcb.isChecked())) {
			distance = loc.distanceTo(yardsales.get(i).location);
			if (distance < mindistance) {
			    nearestyardsale = yardsales.get(i);
			    mindistance = distance;
			}
		    }
		}
		if (mindistance < 1E6) {
		    status.setText(new DecimalFormat("0.0")
			    .format(mindistance * 0.000621371192)
			    + "m - "
			    + nearestyardsale.address
			    + " - "
			    + nearestyardsale.description);
		} else {
		    status.setText("");
		}
	    }
	}

	public void onProviderDisabled(String provider) {
	    // TODO Auto-generated method stub
	}

	public void onProviderEnabled(String provider) {
	    // TODO Auto-generated method stub
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	    // TODO Auto-generated method stub
	}
    }

    public void addOverlays() {
	YardSale yardsale;

	mapView.getOverlays().clear();
	mapView.getOverlays().add(mylocationoverlay);

	Drawable fridrawable = this.getResources().getDrawable(
		R.drawable.fripushpin);
	YardSaleItemizedOverlay frioverlay = new YardSaleItemizedOverlay(
		fridrawable, this);
	Drawable satdrawable = this.getResources().getDrawable(
		R.drawable.satpushpin);
	YardSaleItemizedOverlay satoverlay = new YardSaleItemizedOverlay(
		satdrawable, this);

	Drawable fridrawablelight = this.getResources().getDrawable(
		R.drawable.fripushpinlight);
	YardSaleItemizedOverlay frioverlaylight = new YardSaleItemizedOverlay(
		fridrawablelight, this);
	Drawable satdrawablelight = this.getResources().getDrawable(
		R.drawable.satpushpinlight);
	YardSaleItemizedOverlay satoverlaylight = new YardSaleItemizedOverlay(
		satdrawablelight, this);

	for (int i = 0; i < yardsales.size(); i++) {
	    yardsale = yardsales.get(i);
	    if (yardsale.friday) {
		if (fricb.isChecked()) {
		    if (yardsale.selected) {
			frioverlay.addOverlay(yardsale.overlayitem);
		    } else {
			frioverlaylight.addOverlay(yardsale.overlayitem);
		    }
		}
	    } else {
		if (satcb.isChecked()) {
		    if (yardsale.selected) {
			satoverlay.addOverlay(yardsale.overlayitem);
		    } else {
			satoverlaylight.addOverlay(yardsale.overlayitem);

		    }
		}
	    }
	}

	if (frioverlay.size() > 0) {
	    mapView.getOverlays().add(frioverlay);
	}
	if (frioverlaylight.size() > 0) {
	    mapView.getOverlays().add(frioverlaylight);
	}
	if (satoverlay.size() > 0) {
	    mapView.getOverlays().add(satoverlay);
	}
	if (satoverlaylight.size() > 0) {
	    mapView.getOverlays().add(satoverlaylight);
	}
    }
}