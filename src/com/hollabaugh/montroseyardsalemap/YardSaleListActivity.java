package com.hollabaugh.montroseyardsalemap;

/* references
 * http://developer.android.com/resources/articles/painless-threading.html
 * http://developer.android.com/reference/android/os/AsyncTask.html
 * http://www.mathcs.org/java/android/multi_acts.html */

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.hollabaugh.montroseyardsalemap.R;
import com.hollabaugh.montroseyardsalemap.YardSaleMapActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class YardSaleListActivity extends Activity implements
		View.OnClickListener {
	String ysd = null;
	static ArrayList<YardSale> yardsales;
	ListView list;
	YardSaleAdapter ys_adapter;
	TextView status;
	Button mapbtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		mapbtn = ((Button) findViewById(R.id.mapbtn));
		status = (TextView) findViewById(R.id.status);
		list = (ListView) findViewById(R.id.list);
		mapbtn.setVisibility(0);
		
		if (yardsales == null) {
			new loadDataTask().execute("http://hollabaugh.com/ysd.txt");
		} else {
			this.adapterSetup();
		}

		mapbtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(YardSaleListActivity.this,
						YardSaleMapActivity.class);
				startActivity(i);
			}
		});
	}

	/*
	 * @Override protected void onResume() { super.onResume(); }
	 * 
	 * @Override protected void onPause() { super.onResume(); }
	 */

	private void adapterSetup() {
		this.ys_adapter = new YardSaleAdapter(this, R.layout.row, yardsales);
		list.setAdapter(this.ys_adapter);
	}

	private class loadDataTask extends AsyncTask<String, String, String> {
		protected void onPostExecute(String arg0) {
			Log.i("loadDataTask", "onPostExecute");
			ysd = arg0;
			createYardSales();
			adapterSetup();
			mapbtn.setVisibility(1);
			status.setText("");

		}

		protected void onProgressUpdate(String... arg0) {
			status.setText(arg0[0]);
		}

		@Override
		protected String doInBackground(String... arg0) {
			Log.i("loadDataTask", "doInBackground");
			HttpUriRequest request = null;
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
			HttpConnectionParams.setSoTimeout(httpParameters, 5000);
			DefaultHttpClient client = new DefaultHttpClient(httpParameters);

			this.publishProgress("Requesting yard sale data");
			while (true) {
				try {
					this.publishProgress("Requesting yard sale data");
					request = new HttpGet(arg0[0]);
					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					this.publishProgress("Processing yard sale data");
					return (client.execute(request, responseHandler));
				} catch (Exception e) {
					this.publishProgress("Requesting yard sale failed, retrying");
					Log.e("loadDataTask", " FAIL! " + e.toString());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}

	}

	public void createYardSales() {
		Log.i("createYardSales", "start");
		YardSale yardsale;
		yardsales = new ArrayList<YardSale>();
		StringTokenizer line = new StringTokenizer(ysd, "\n");
		while (line.hasMoreTokens()) {
			try {
				yardsale = new YardSale(line.nextToken());
				if (yardsale.valid) {
					yardsales.add(yardsale);
					status.setText(yardsale.address);
					status.invalidate();
				}
			} catch (Error e) {
				Log.e("createYardSales", "error 1");
				status.setText("createYardSales failed. " + e.toString());
			}

		}
	}

	private class YardSaleAdapter extends ArrayAdapter<YardSale> {

		private ArrayList<YardSale> items;

		public YardSaleAdapter(Context context, int textViewResourceId,
				ArrayList<YardSale> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row, null);
			}
			YardSale ys = items.get(position);
			if (ys != null) {
				TextView desc = (TextView) v.findViewById(R.id.description);
				if (desc != null) {
					String line = "<b>" + ys.day + " - " + ys.address
							+ "</b><br/>\n" + ys.description.trim();
					desc.setTag(position);
					desc.setText(Html.fromHtml(line),
							TextView.BufferType.SPANNABLE);
					desc.setClickable(true);
					desc.setOnClickListener(new DescClickHandler());
				}
				CheckBox sel = (CheckBox) v.findViewById(R.id.selected);
				if (sel != null) {
					sel.setTag(position);
					sel.setChecked(ys.selected);
					sel.setOnCheckedChangeListener(new CheckedChangedHandler());
				}

			}
			return v;
		}

		private class DescClickHandler implements OnClickListener {
			public void onClick(View arg0) {
				YardSale ys = items.get(Integer.parseInt(arg0.getTag()
						.toString()));
				Log.i("DescClickHandler", ys.address);
				ys.selected = !ys.selected;
				list.invalidateViews();
			}

		}

		private class CheckedChangedHandler implements OnCheckedChangeListener {
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				YardSale ys = items.get(Integer.parseInt(arg0.getTag()
						.toString()));
				ys.selected = arg1;
				// status.setText(ys.address + " " + ys.selected.toString());
			}
		}
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}
}