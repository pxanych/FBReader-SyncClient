package com.sync;


import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.json.JSONArray;

import android.content.Context;


public class ServerInterface{
	
	public ServerInterface(Context context, String id, String signature) 
	throws MalformedURLException {
		myID = id;
		mySignature = signature;
		myServerUrl = new URL(context.getString(R.string.host));
	}
	
	private URL myServerUrl;
	private String myID;
	private String mySignature;	
	
	public JSONArray uploadString(String string){
		String hash = "";
		try{
			hash = Digests.hmacSHA256(string, mySignature);
		}
		catch (InvalidKeyException e){
			e.printStackTrace();
			return null;
		}
		
		String requestBody = hash + "&" + myID + "&" + string;
		String requestUrl = myServerUrl + "sync_upload.php5";
		Request request = new Request( requestUrl, null, requestBody);
		ZLNetworkManager networkManager = new ZLNetworkManager();
		try {
			networkManager.perform(request);
			return request.getResponse();
		} 
		catch (ZLNetworkException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public JSONArray downloadString()	{
		String requestUrl = myServerUrl + "sync_download.php5?stage=init";
		Request request = new Request(requestUrl);
		ZLNetworkManager networkManager = new ZLNetworkManager();
		try {
			networkManager.perform(request);
		}
		catch (ZLNetworkException e) {
			return null;
		}
		
		try{
			JSONArray response = request.getResponse();
			if ((Boolean) response.get(0)){
				String random = response.get(1).toString();
				String hash = Digests.hashSHA256(random + mySignature);
				
				requestUrl = myServerUrl + "sync_download.php5?stage=confirm&" +
							"id=" + myID + "&hash=" + hash + "&rand=" + random;
				request = new Request(requestUrl);
				networkManager.perform(request);
				response = request.getResponse();
				return response;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}	
}
