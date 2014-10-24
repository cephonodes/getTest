package com.example.morinaga.metrosample_morinaga;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button btn = (Button)findViewById(R.id.button1);
		btn.setOnClickListener(this);
	}

	public void onClick(View view) {
		final TextView resultView = (TextView)findViewById(R.id.resultView1);
		//String query;

		// APIのURLを生成する
	    //<string name="data_search">datapoints?</string>
	    //<string name="data_get">datapoints/DATA_URI</string>
	    //<string name="chibutsu_search">places?</string>
	    //<string name="chibutsu_get">places/DATA_URI</string>
		String endpoint = (String)getText(R.string.endpoint);
		String access_token = (String)getText(R.string.access_token);
		String path = (String)getText(R.string.data_search);
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.scheme("https");
		uriBuilder.encodedAuthority(endpoint);
		uriBuilder.encodedPath(path);
		uriBuilder.appendQueryParameter("rdf:type", "odpt:StationTimetable");
		uriBuilder.appendQueryParameter("odpt:station", "odpt.Station:TokyoMetro.Tozai.Otemachi");
		//uriBuilder.appendQueryParameter("odpt:railway", "");
		//uriBuilder.appendQueryParameter("odpt:railDirection", "");
		uriBuilder.appendQueryParameter("acl:consumerKey", access_token);
		String uriStr = uriBuilder.toString();

		HttpGetTask task = new HttpGetTask(
			this,
			uriStr,

			// タスク完了時に呼ばれるUIのハンドラ
			new HttpGetHandler() {

				@Override
				public void onGetCompleted(String response) {
					// JSONをパース
					StringBuilder viewStrBuilder = new StringBuilder();
					try {
						JSONArray result = new JSONArray(response);
						for(int i=0; i<result.length(); i++) {
							JSONObject info = result.getJSONObject(i);
							viewStrBuilder.append(info.getString("dc:date") + "\n");
							viewStrBuilder.append(info.getString("odpt:station") + "\n");
							viewStrBuilder.append(info.getString("odpt:railway") + "\n");
							viewStrBuilder.append(info.getString("odpt:operator") + "\n");
							viewStrBuilder.append(info.getString("odpt:railDirection") + "\n");
							String timeTable = info.getString("odpt:weekdays");
							JSONArray result2 = new JSONArray(timeTable);
							for(int j=0; j<result2.length(); j++) {
								JSONObject info2 = result2.getJSONObject(j);
								if(info2.getString("odpt:departureTime") == "true") {
									viewStrBuilder.append(info2.getString("odpt:trainType") + "\n");
									viewStrBuilder.append(info2.getString("odpt:destinationStation") + "\n");
									viewStrBuilder.append(info2.getString("odpt:depatureTime") + "\n");
									viewStrBuilder.append(info2.getString("odpt:isLast") + "\n");
									viewStrBuilder.append(info2.getString("odpt:isOrigin") + "\n");
									viewStrBuilder.append(info2.getString("odpt:carComposition") + "\n");
									viewStrBuilder.append(info2.getString("odpt:note") + "\n");
								}
							}
							viewStrBuilder.append("-----------\n");
						}
						resultView.setText(viewStrBuilder);
					} catch (JSONException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}

				@Override
				public void onGetFailed(String response) {
					resultView.setText(response);
					Toast.makeText(
							getApplicationContext(),
							"エラーが発生しました。",
							Toast.LENGTH_LONG
							).show();
				}
			}
		);

		task.execute();
	}
}
