package com.example.mobileid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity implements OnClickListener{
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_ID_NUMBER = "idNumber";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String STRING_EMPTY_IDNUMBER = "Entry 16 digit nomer KTP di bawah";
	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "138118172315";
	
	/**
	 * Tag used on log messages.
	 */
	final String TAG = "GCM Demo Main";
	
	TextView mDisplay, mKtp;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;
	
	String regid, nomerKTP;
	EditText formKTP;
	Button toggleSave;

	JSONObject ktpObj;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i(TAG,"On Create");
		
		Intent intent = getIntent();
		try{
			Log.i(TAG,"Intent:"+intent.getExtras().toString());
			String info = intent.getExtras().getString("info");
			String otp = intent.getExtras().getString("OTP");
			Log.i(TAG,"Info: "+info);
			Log.i(TAG,"OTP: "+otp);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		mDisplay = (TextView)findViewById(R.id.display);
		mKtp = (TextView)findViewById(R.id.dataKtp);
		formKTP = (EditText)findViewById(R.id.idNumber);
		toggleSave = (Button)findViewById(R.id.toggleSave);
		toggleSave.setOnClickListener(this);
		
		context = getApplicationContext();
		
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            nomerKTP = getIdNumber(context);
            
            if (regid.isEmpty()) {
                registerInBackground();
            } else {
            	mDisplay.setText(regid);
            	if(nomerKTP.isEmpty()){
            		//kalo belom diisi kolom no ktp, isi dulu masukin app pref
            		mKtp.setText(STRING_EMPTY_IDNUMBER);
            	} else {
            		formKTP.setText(nomerKTP);
            		formKTP.setEnabled(false);
            		//display isi data ktp
            		getKtpDataPutToTextView();
            	}
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
        Log.i(TAG, "On Resume");
    }

	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	    Log.i(TAG,"new intent:"+intent.getExtras().toString());
	    try{
		    String info = intent.getExtras().getString("info");
			String otp = intent.getExtras().getString("OTP");
			Log.i(TAG,"Info: "+info);
			Log.i(TAG,"OTP: "+otp);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getAppPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    
    /**
     * set id number to shared preferences as string
     * 
     * @param context
     */
    private void storeIdNumber(Context context, String idNumber) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_ID_NUMBER, idNumber);
        editor.commit();
    }
    
    /**
     * Get ID Number from app preferences
     * 
     * @param context
     * @return ID Number in string
     */
    private String getIdNumber(Context context) {
    	final SharedPreferences prefs = getAppPreferences(context);
        String idNumber = prefs.getString(PROPERTY_ID_NUMBER, "");
        if (idNumber.isEmpty()) {
            Log.i(TAG, "There's no ID Number in preferences");
            return "";
        }
        return idNumber;
    }
    
    /**
     * delete id number from app preferences
     * 
     * @param context
     */
    private void deleteIdNumber(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_ID_NUMBER);
        editor.commit();
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    /**
     * on click listener method
     */
    // Send an upstream message.
    public void onClick(final View view) {
    	switch(view.getId()){
    		case R.id.send:
    			new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        String msg = "";
                        try {
                            Bundle data = new Bundle();
                            data.putString("my_message", "Hello World");
                            data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
                            String id = Integer.toString(msgId.incrementAndGet());
                            gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                            msg = "Sent message";
                        } catch (IOException ex) {
                            msg = "Error :" + ex.getMessage();
                        }
                        return msg;
                    }

                    @Override
                    protected void onPostExecute(String msg) {
                        mDisplay.append(msg + "\n");
                    }
                }.execute(null, null, null);
            break;
    		case R.id.clear:
    			mDisplay.setText("");
			break;
    		case R.id.toggleSave:
    			if(nomerKTP.isEmpty()){
    				String tempIdNum = formKTP.getText().toString();
    				if(tempIdNum.length() == 16){
    					storeIdNumber(context, tempIdNum);
    					formKTP.setEnabled(false);
    					nomerKTP = tempIdNum;
    					getKtpDataPutToTextView();
    				} else {
    					Toast.makeText(context, "nomer KTP 16 digit", Toast.LENGTH_SHORT).show();
    				}
    			}else{
    				deleteIdNumber(context);
    				nomerKTP = "";
    				mKtp.setText(STRING_EMPTY_IDNUMBER);
    				formKTP.setEnabled(true);
    			}
			break;
    	}
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getAppPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    
    /**
     * Read KTP data in internal storage. Put it to textview.
     */
    private void getKtpDataPutToTextView(){
    	FileInputStream fin;
    	String path = Environment.getExternalStorageDirectory().toString()+"/"+nomerKTP.concat(".json");
    	File filePath = new File(path);
		try {
			fin = new FileInputStream(filePath);
			int c;
	    	String temp="";
	    	while( (c = fin.read()) != -1){
	    	   temp = temp + Character.toString((char)c);
	    	}
	    	//string temp contains all the data of the file.
	    	fin.close();
	    	
			JSONObject obj = new JSONObject(temp);
			ktpObj = obj.getJSONObject("KTP");
			JSONObject ttlObj = ktpObj.getJSONObject("TempatTglLahir");
			JSONObject addrObj = ktpObj.getJSONObject("Alamat");
			
			mKtp.setText("NIK: "+ktpObj.getString("NIK")+"\n");
			mKtp.append("Nama: "+ktpObj.getString("Nama")+"\n");
			mKtp.append("Tempat/Tgl Lahir: "+ttlObj.getString("TempatLahir")+"/"+ttlObj.getString("TanggalLahir")+"\n");
			mKtp.append("Gender: "+ktpObj.getString("JenisKelamin")+"\n");
			mKtp.append("Gol.Darah: "+ktpObj.getString("GolDarah")+"\n");
			mKtp.append("Alamat: "+addrObj.getString("Jalan")+" RT"+addrObj.getString("RT")+" RW"+addrObj.getString("RW")+"\n");
			mKtp.append("Kelurahan/Desa: "+addrObj.getString("KelDesa")+"\n");
			mKtp.append("Kecamatan: "+addrObj.getString("Kecamatan")+"\n");
			mKtp.append("Agama: "+ktpObj.getString("Agama")+"\n");
			mKtp.append("Status: "+ktpObj.getString("StatusPerkawinan")+"\n");
			mKtp.append("Pekerjaan: "+ktpObj.getString("Pekerjaan")+"\n");
			mKtp.append("Kewarganegaraan: "+ktpObj.getString("Kewarganegaraan")+"\n");
			mKtp.append("Berlaku Hingga: "+ktpObj.getString("BerlakuHingga")+"\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	@Override
	protected void onDestroy() {
	  // Unregister since the activity is about to be closed.
	  super.onDestroy();
	}
}
