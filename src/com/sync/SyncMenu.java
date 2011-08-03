package com.sync;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.geometerplus.android.fbreader.api.ApiClientImplementation;
import org.geometerplus.android.fbreader.api.ApiException;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import org.w3c.dom.Element;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SyncMenu extends Activity implements ApiClientImplementation.ConnectionListener{
	
	public static final String SETTINGS_FILENAME = "FBSYNC_SETTINGS";
	
	private ApiClientImplementation myApi;
	private Button myUpload;
	private Button myDownload;
	private Button myAuthenticate;
	private TextView myUploadResponse;
	private TextView myDownloadResponse;
	private TextView myAuthenticationStatus;
	private ServerConversation myServerInterface;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
    	setContentView(R.layout.sync_menu);
    	myUpload = (Button)findViewById(R.id.button1);
    	myDownload = (Button)findViewById(R.id.button2);
    	myAuthenticate = (Button)findViewById(R.id.button3);
    	myUploadResponse = (TextView)findViewById(R.id.textView1);
    	myDownloadResponse = (TextView)findViewById(R.id.textView2);
    	myAuthenticationStatus = (TextView)findViewById(R.id.textView3);
    	myAuthenticationStatus.setVisibility(View.VISIBLE);
    	myUpload.setOnClickListener(new UploadListener());
    	myDownload.setOnClickListener(new DownloadListener());
    	
    	SharedPreferences settings = getSharedPreferences(SETTINGS_FILENAME, 0);
		if (settings.getBoolean("AUTH", false)){
			myServerInterface = new ServerConversation(
					null,
					settings.getString("ID", ""), 
					settings.getString("SIG", ""));
			myAuthenticate.setOnClickListener(new DropAuthListener());
			myAuthenticate.setText("Logout");
			myUploadResponse.setText("Loaded");
			setControlsEnabled(true);
		} else {
			myAuthenticate.setOnClickListener(new AuthListener());
			myAuthenticate.setText("Authenticate");
			setControlsEnabled(false);
			Uri data = getIntent().getData();
	    	if (data != null){
	    		completeAuthentication(data);
	    	}
		}    	
		
		myApi = new ApiClientImplementation(this, this);
		super.onCreate(savedInstanceState);
    }
    

    public void onConnected() {
    	// TODO: Buttons should be enabled here.
    };
    
    private String serializeOptions(Map<String, Map<String, String>> options){
    	try{
    		StringWriter writer = new StringWriter();
    		XmlSerializer serializer = Xml.newSerializer();
    		serializer.setOutput(writer);
    		
    	   	serializer.startDocument("utf-8", true);
    	   	serializer.startTag(null, "options");
    	   	serializer.attribute(null, "groupcount", String.valueOf(options.size()));
    	   	
    	   	for (Map.Entry<String, Map<String, String>> group : options.entrySet()){
    	   		serializer.startTag(null, "group");
    	   		serializer.attribute(null, "groupName", group.getKey());    	   		
    	   		for(Map.Entry<String, String> option : group.getValue().entrySet()){
    	   			serializer.startTag(null, "option");
    	   			serializer.attribute(null, "optionName", option.getKey());
    	   			serializer.attribute(null, "value", option.getValue());
    	   			serializer.endTag(null, "option");
    	   		}
    	   		serializer.endTag(null, "group");
    	   	}
    	   	
    	   	serializer.endTag(null, "options");
    	   	serializer.endDocument();
    	   	serializer.flush();
    	   	
    	   	return writer.toString();
    	}
    	catch(Exception e){
    		return e.getMessage();
    	}
    }
    
    private void applyXmlEncodedOptions(String xml){
    	InputSource source = new InputSource(new StringReader(xml));
    	String debugOutputString = "";
    	try{
    		Element docElement = DocumentBuilderFactory.newInstance()
									.newDocumentBuilder().parse(source)
									.getDocumentElement();
    		
    		if (docElement.getNodeName().equals("options")){
    			NodeList groups = docElement.getChildNodes();
    			
    			int groupCount = groups.getLength();
    			for(int i = 0; i < groupCount; ++i){
    				Node groupTag = groups.item(i);
    				NodeList options = groupTag.getChildNodes();
    				debugOutputString += groupTag.getAttributes()
    										.getNamedItem("groupName").getNodeValue() + "\n";
    				
    				int optionCount = options.getLength();
    				for(int j = 0; j < optionCount; ++j){
    					Node option = options.item(j);
    					String optName = option.getAttributes().getNamedItem("optionName").getNodeValue();
    					String optValue = option.getAttributes().getNamedItem("value").getNodeValue();
    					debugOutputString += "\t" + optName + " : " + optValue +  "\n"; 
    				}
    			}
    		}
    		myDownloadResponse.setText(debugOutputString);
    	}
    	catch (Exception e){
    		myDownloadResponse.setText(xml + "\n\n" + e.getMessage());
    	}
    }
    
    @Override
    protected void onNewIntent(Intent intent){
    	setIntent(intent);
    	if (intent.hasExtra("RETRY")){
    		myAuthenticationStatus.setText("Authentication failed. Please retry");
    		myAuthenticationStatus.setVisibility(View.VISIBLE);
    	}
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
		myUpload.setEnabled(state);
		myDownload.setEnabled(state);
    }
    
    private class AuthListener implements OnClickListener{
    	public void onClick(View v) {
    		Intent browserIntent = new Intent(Intent.ACTION_VIEW);
    		browserIntent.setData(Uri.parse("https://data.fbreader.org/sync/?sync_respondtype=url"));
    		startActivity(browserIntent);
			finish();
    	}
    }
    
    private class DropAuthListener implements OnClickListener{
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
    	public void onClick(View v) {
        	try{
        		List<String> groups = myApi.getOptionGroups();
        		Map<String, Map<String, String>> options = 
        			new HashMap<String,Map<String, String>>();
        		
        		for (String group : groups){
        			Map<String, String> groupOptions = new HashMap<String, String>();
        			options.put(group, groupOptions);
        			for (String option : myApi.getOptionNames(group)){
        				groupOptions.put(option, myApi.getOptionValue(group, option));
        			}
        		}
        		
        		JSONArray respond = myServerInterface.uploadString(serializeOptions(options));
        		if(respond != null){
        			myUploadResponse.setText(respond.toString());
        		}
        	}
        	catch(ApiException e){
        		myUploadResponse.setText("Fail: " + e.getMessage());
        	}
    	}
    }
    
    private class DownloadListener implements OnClickListener{
    	public void onClick(View v) {
    		JSONArray respond = myServerInterface.downloadString();
    		if(respond != null){
    			try{
    				applyXmlEncodedOptions(respond.get(1).toString());
    			} 
    			catch (JSONException e) {
					myDownloadResponse.setText(e.getMessage());
				}
    		} else {
    			myDownloadResponse.setText("Server interaction error.");
    		}
    	}
    }
        
}
