package com.hollabaugh.montroseyardsalemap;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class YardSaleItemizedOverlay extends ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private Context mContext;

    public YardSaleItemizedOverlay(Drawable defaultMarker) {
	super(boundCenterBottom(defaultMarker));
    }

    public void addOverlay(OverlayItem overlay) {
	mOverlays.add(overlay);
	populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
	return mOverlays.get(i);
    }

    @Override
    public int size() {
	return mOverlays.size();
    }

    public YardSaleItemizedOverlay(Drawable defaultMarker, Context context) {
	super(boundCenterBottom(defaultMarker));
	//super(defaultMarker);
	mContext = context;
    }

    @Override
    protected boolean onTap(int index) {
	try {
	OverlayItem item = mOverlays.get(index);
	AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	dialog.setTitle(item.getTitle());
	dialog.setMessage(item.getSnippet());
	dialog.show();
	} catch (Exception e) {}
	return true;
    }

}
