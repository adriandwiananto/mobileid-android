package com.example.mobileid;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class Network extends AsyncTask<Void, Void, JSONObject> {

	private final static String TAG = "GCM Network";
			
	private String hostname;
	private String data;
	private Context ctx;
	private String hmacData;
	
	/**
	 * USE THIS CONSTRUCTOR TO SEND HTTP POST REGISTRATION DATA
	 * @param parent parent activity
	 * @param context caller context
	 * @param jobj JSON object of registration data
	 * @param NewPass new password inputted in registration activity
	 * @param ACCNtoSend ACCN inputted in registration activity
	 * @param HWID phone IMEI
	 */
	public Network(Context context, JSONObject jobj, String hmacKtp){
		ctx = context;
		hmacData = hmacKtp;
		
		try {
			hostname = jobj.getString("SIaddress");

			JSONObject objMeta = new JSONObject();
			objMeta.put("AppID", jobj.getString("AppID"));
			objMeta.put("PID", jobj.getString("PID"));
			
			JSONObject objMsg = new JSONObject();
			objMsg.put("hmac", hmacData);
			
			JSONObject objSend = new JSONObject();
			objSend.put("META", objMeta);
			objSend.put("MESSAGE", objMsg);
			
			data = objSend.toString();
			Log.i(TAG,"data to send: "+data);
		} catch (JSONException e) {
			e.printStackTrace();
			Log.i(TAG,"JSON Exception: "+e.getMessage());
		}
	}
	
	@Override
	protected JSONObject doInBackground(Void... params) {
		try {
			int TIMEOUT_MILLISEC = 10000;  // = 10 seconds
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
			HttpClient client = new DefaultHttpClient(httpParams);

			HttpPost request = new HttpPost(hostname);
			request.setEntity(new ByteArrayEntity(data.getBytes("UTF8")));
			HttpResponse response = client.execute(request);
			
			// Get response and return it in JSON Object type
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String json = reader.readLine();
			Log.i(TAG, "return:"+json);

			JSONObject finalResult = new JSONObject();
			return finalResult;
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG,"error:"+e.getMessage());
			return null;
		}
	}

	@Override
	protected void onPreExecute() {
		//Toast this message before doInBackground starts
	}
	 
	@Override
	protected void onPostExecute(JSONObject result) {
		
	}
}
