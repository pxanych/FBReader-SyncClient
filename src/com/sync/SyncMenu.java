package com.sync;

import java.io.IOException;
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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import org.w3c.dom.Element;

import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SyncMenu extends Activity implements ApiClientImplementation.ConnectionListener{
	
	private static final String SETTINGS_FILENAME = "FBSYNC_SETTINGS";
	
	private ApiClientImplementation myApi;
	private ServerConversation myServerInterface;
	private Boolean myAuthorized = false;
	
    public void onCreate(Bundle savedInstanceState) {
    	setContentView(R.layout.sync_menu);
    	Button myUpload;
    	Button myDownload;
    	Button myAuthenticate;
    	myUpload = (Button)findViewById(R.id.uploadButton);
    	myDownload = (Button)findViewById(R.id.downloadButton);
    	myAuthenticate = (Button)findViewById(R.id.authButton);
    	myUpload.setOnClickListener(new UploadListener());
    	myDownload.setOnClickListener(new DownloadListener());    
    	
    	
    	SharedPreferences settings = getSharedPreferences(SETTINGS_FILENAME, 0);
		if (settings.getBoolean("AUTH", false) == true) {
			myServerInterface = new ServerConversation(
					null,	// default host (https://data.fbreader.org/sync) is used
					settings.getString("ID", ""), 
					settings.getString("SIG", ""));
			myAuthenticate.setOnClickListener(new DropAuthListener());
			myAuthenticate.setText(R.string.auth_button_logout);
			myAuthorized = true;
		} else {
			myAuthenticate.setOnClickListener(new AuthListener());
			myAuthenticate.setText(R.string.auth_button_authenticate);
			setControlsEnabled(false);
			Uri data = getIntent().getData();
	    	if (data != null) {
	    		completeAuthentication(data);
	    	}
		}    	
		
		myApi = new ApiClientImplementation(this, this);
		if (myApi == null) {
			Toast.makeText(this, R.string.internal_error, Toast.LENGTH_SHORT);
			finish();
		}
		super.onCreate(savedInstanceState);
    }    

    public void onConnected() {
    	((TextView)findViewById(R.id.debugString)).setText(myAuthorized.toString());
    	setControlsEnabled(myAuthorized);
    };
    
    private String serializeOptions(Map<String, Map<String, String>> options) {
    	try{
    		StringWriter writer = new StringWriter();
    		XmlSerializer serializer = Xml.newSerializer();
    		serializer.setOutput(writer);
    		
    	   	serializer.startDocument("utf-8", true);
    	   	serializer.startTag(null, "options");
    	   	
    	   	for (Map.Entry<String, Map<String, String>> group : options.entrySet()) {
    	   		serializer.startTag(null, "group");
    	   		serializer.attribute(null, "groupName", group.getKey());    	   		
    	   		for(Map.Entry<String, String> option : group.getValue().entrySet()) {
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
    	catch(IOException e) {
    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
    		return null;
    	}
    }
    
    private void applyXmlEncodedOptions(String xml) { 
    	TextView debugView = (TextView)findViewById(R.id.debugString);
    	InputSource source = new InputSource(new StringReader(xml));
    	String debugOutputString = "";
    	try{
    		Element docElement = DocumentBuilderFactory.newInstance()
									.newDocumentBuilder().parse(source)
									.getDocumentElement();
    		
    		if (docElement.getNodeName().equals("options")) {
    			NodeList groups = docElement.getChildNodes();
    			
    			int groupCount = groups.getLength();
    			for(int i = 0; i < groupCount; ++i) {
    				Node groupTag = groups.item(i);
    				NodeList options = groupTag.getChildNodes();
    				debugOutputString += groupTag.getAttributes()
    										.getNamedItem("groupName").getNodeValue() + "\n";
    				
    				int optionCount = options.getLength();
    				for(int j = 0; j < optionCount; ++j) {
    					Node option = options.item(j);
    					String optName = option.getAttributes().getNamedItem("optionName").getNodeValue();
    					String optValue = option.getAttributes().getNamedItem("value").getNodeValue();
    					debugOutputString += "\t" + optName + " : " + optValue +  "\n"; 
    				}
    			}
    		}
    		debugView.setText(debugOutputString);
    	}
    	catch (Exception e) {
    		debugView.setText(xml + "\n\n" + e.getMessage());
    		Toast.makeText(this,R.string.internal_error, Toast.LENGTH_SHORT);
    	}
    }
    
    private void completeAuthentication(Uri response) {
    	TextView debugView = (TextView)findViewById(R.id.debugString);
    	Button authButton = (Button)findViewById(R.id.authButton);
    	String query = response.getQuery();
    	findViewById(R.id.browserView).setVisibility(View.INVISIBLE);
    	try{
    		if (query == null) {
    			debugView.setText("Invalid server response: no query string");
    			query = "";
    		}
			String[] args = query.split("&");
			for (int i = 0; i < args.length; ++i) {
				args[i] = args[i].split("=", 2)[1];
			}
			
			if("1".equals(args[0])) {
				SharedPreferences settings = getSharedPreferences(SETTINGS_FILENAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("ID", args[1]);
				editor.putString("SIG", args[2]);
				editor.putBoolean("AUTH", true);
				editor.commit();
				
				myServerInterface = new ServerConversation(null, args[1], args[2]);
				authButton.setOnClickListener(new DropAuthListener());
				authButton.setText(R.string.auth_button_logout);
				setControlsEnabled(true);
				myAuthorized = true;
			} else {
				Toast.makeText(this, R.string.auth_failed, Toast.LENGTH_SHORT);
			}
    	}
    	catch(ArrayIndexOutOfBoundsException e) {
    		Toast.makeText(this, R.string.auth_failed_server, Toast.LENGTH_SHORT);
    		debugView.setText("Invalid server response: " + response.getQuery());    		
    	}
    }
    
    private void setControlsEnabled(Boolean state) {
		findViewById(R.id.uploadButton).setEnabled(state);
		findViewById(R.id.downloadButton).setEnabled(state);
    }
    
    private class SyncWebViewClient extends WebViewClient {
    	@Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	if (!url.startsWith("fbsync")) {
		        view.loadUrl(url);
		        return true;
	    	} else {
	    		completeAuthentication(Uri.parse(url));
	    		return false;
	    	}
	    }
	}
    
    private class AuthListener implements OnClickListener{
    	public void onClick(View v) {    		
    		WebView browserView = (WebView)findViewById(R.id.browserView);
    		browserView.setVisibility(View.VISIBLE);
    		browserView.setNetworkAvailable(true);
    		browserView.setWebViewClient(new SyncWebViewClient());
    		browserView.getSettings().setJavaScriptEnabled(true);
    		browserView.loadUrl("https://data.fbreader.org/sync/?sync_respondtype=url");
    	}
    }
    
    private class DropAuthListener implements OnClickListener{
    	public void onClick(View v) {
    		Button authButton = (Button)findViewById(R.id.authButton);
    		SharedPreferences settings = getSharedPreferences(SETTINGS_FILENAME, 0);
    		Editor editor = settings.edit();
    		editor.clear();
    		editor.commit();
    		authButton.setOnClickListener(new AuthListener());
    		authButton.setText(R.string.auth_button_authenticate);
    		setControlsEnabled(false);
    		myAuthorized = false;
    	}
    }
    
    private class UploadListener implements OnClickListener{
    	public void onClick(View v) {
    		TextView debugView = (TextView)findViewById(R.id.debugString);
        	try{
        		List<String> groups = myApi.getOptionGroups();
        		Map<String, Map<String, String>> options = 
        			new HashMap<String,Map<String, String>>();
        		
        		for (String group : groups) {
        			Map<String, String> groupOptions = new HashMap<String, String>();
        			options.put(group, groupOptions);
        			for (String option : myApi.getOptionNames(group)) {
        				groupOptions.put(option, myApi.getOptionValue(group, option));
        			}
        		}
        		
        		JSONArray respond = myServerInterface.uploadString(serializeOptions(options));
        		if(respond != null) {
        			debugView.setText(respond.toString());
        		}
        	}
        	catch(ApiException e) {
        		debugView.setText("Fail: " + e.getMessage());
        	}
    	}
    }
    
    private class DownloadListener implements OnClickListener{
    	public void onClick(View v) {
    		TextView debugView = (TextView)findViewById(R.id.debugString);
    		JSONArray respond = myServerInterface.downloadString();
    		if(respond != null) {
    			try{
    				applyXmlEncodedOptions(respond.get(1).toString());
    			} 
    			catch (JSONException e) {
    				Toast.makeText(
	    						getApplicationContext(),
	    						R.string.server_error,
	    						Toast.LENGTH_SHORT
	    					);
				}
    		} else {
    			debugView.setText("Server interaction error.");
    		}
    	}
    }
        
}
