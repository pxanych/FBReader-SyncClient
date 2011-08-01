package com.sync;

import org.json.JSONArray;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SyncMenu extends Activity{
	
	public static final String SETTINGS_FILENAME = "FBSYNC_SETTINGS";
	
	private Button myUpload;
	private Button myDownload;
	private Button myAuthenticate;
	private EditText myDataToSave;
	private TextView myUploadResponse;
	private TextView myDownloadResponse;
	private TextView myAuthenticationStatus;
	private ServerConversation myServerInterface;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.sync_menu);
    	myUpload = (Button)findViewById(R.id.button1);
    	myDownload = (Button)findViewById(R.id.button2);
    	myAuthenticate = (Button)findViewById(R.id.button3);
    	myDataToSave = (EditText)findViewById(R.id.editText1);
    	myUploadResponse = (TextView)findViewById(R.id.textView1);
    	myDownloadResponse = (TextView)findViewById(R.id.textView2);
    	myAuthenticationStatus = (TextView)findViewById(R.id.textView3);
    	myAuthenticationStatus.setVisibility(View.VISIBLE);
    	myUpload.setOnClickListener(new UploadListener());
    	myDownload.setOnClickListener(new DownloadListener());
    	
    	Intent intent = getIntent();
    	Uri data = intent.getData();
    	SharedPreferences settings = getSharedPreferences(SETTINGS_FILENAME, 0);
		if (settings.getBoolean("AUTH", false)){
			String id = settings.getString("ID", "");
			String sig = settings.getString("SIG", "");
			myServerInterface = new ServerConversation(null, id, sig);
			myAuthenticate.setOnClickListener(new DropAuthListener());
			myAuthenticate.setText("Logout");
			myUploadResponse.setText("Loaded");
			setControlsEnabled(true);
		} else {
			myAuthenticate.setOnClickListener(new AuthListener());
			myAuthenticate.setText("Authenticate");
			setControlsEnabled(false);
	    	if (data != null){
	    		completeAuthentication(data);
	    	}
		}    	
    }
    
    @Override
    protected void onNewIntent(Intent intent){
    	setIntent(intent);
    	if (intent.hasExtra("RETRY")){
    		myAuthenticationStatus.setText("Authentication failed. Plaese retry");
    		myAuthenticationStatus.setVisibility(View.VISIBLE);
    	}
    	myUploadResponse.setText("onNewIntent");
    }
    
    private void completeAuthentication(Uri response){
    	String query = response.getQuery();
		String[] args = query.split("&");
		for (int i = 0; i < args.length; ++i){
			args[i] = args[i].split("=", 2)[1];
		}
		if(args[0].equals("1")){
			SharedPreferences settings = getSharedPreferences(SETTINGS_FILENAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("ID", args[1]);
			editor.putString("SIG", args[2]);
			editor.putBoolean("AUTH", true);
			editor.commit();
			myServerInterface = new ServerConversation(null, args[1], args[2]);
			myAuthenticate.setOnClickListener(new DropAuthListener());
			myAuthenticate.setText("Logout");
			setControlsEnabled(true);
		} else {
			Intent retryAuth = new Intent();
			retryAuth.setClass(getApplicationContext(), SyncMenu.class);
			retryAuth.putExtra("RETRY", true);
			startActivity(retryAuth);
			finish();
		}
    }
    
    private void setControlsEnabled(Boolean state){
		myDataToSave.setEnabled(state);
		myUpload.setEnabled(state);
		myDownload.setEnabled(state);
    }
    
    private class AuthListener implements OnClickListener{
    	@Override
    	public void onClick(View v) {
    		Intent browserIntent = new Intent(Intent.ACTION_VIEW);
    		browserIntent.setData(Uri.parse("https://data.fbreader.org/sync/?sync_respondtype=url"));
    		startActivity(browserIntent);
			finish();
    	}
    }
    
    private class DropAuthListener implements OnClickListener{
    	@Override
    	public void onClick(View v) {
    		SharedPreferences settings = getSharedPreferences(SETTINGS_FILENAME, 0);
    		Editor editor = settings.edit();
    		editor.clear();
    		editor.commit();
    		myAuthenticate.setOnClickListener(new AuthListener());
    		myAuthenticate.setText("Authenticate");
    		setControlsEnabled(false);
    	}
    }
    
    private class UploadListener implements OnClickListener{
    	@Override
    	public void onClick(View v) {
    		JSONArray respond = myServerInterface.uploadFile(myDataToSave.getText().toString());
    		if(respond != null){
    			myUploadResponse.setText(respond.toString());
    		}
    	}
    }
    
    private class DownloadListener implements OnClickListener{
    	@Override
    	public void onClick(View v) {
    		JSONArray respond = myServerInterface.downloadFile();
    		if(respond != null){
    			myDownloadResponse.setText(respond.toString());
    		}
    	}
    }
        
}
