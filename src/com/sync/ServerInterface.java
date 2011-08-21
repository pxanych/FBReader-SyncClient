package com.sync;


import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.List;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.json.JSONArray;

import service.FBSyncPositionsProvider.Position;

import android.content.Context;


public class ServerInterface{
	
	public ServerInterface(Context context, String id, String signature) 
	throws MalformedURLException {
		myContext = context;
		myID = id;
		mySignature = signature;
		myServerUrl = new URL(myContext.getString(R.string.host));
	}
	
	private URL myServerUrl;
	private String myID;
	private String mySignature;	
	private Context myContext;
	
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
	
	
	public Position[] get_positions(List<String> books) throws ServerInterfaceException {
		ZLNetworkManager networkManager = new ZLNetworkManager();
		JSONArray query = new JSONArray();
		JSONArray args = new JSONArray();
		
		args.put(myID);
		args.put(new JSONArray(books));
		
		query.put("get_positions");
		query.put(args);
		
		Request request = new Request(
				myContext.getString(R.string.api_url), 
				null, 
				query.toString()
				);
		try {
			networkManager.perform(request);
			JSONArray response = request.getResponse();
			Position[] ret = new Position[response.length()];
			for (int i = 0; i < response.length(); ++i) {
				ret[i] = new Position(response.getString(i));
			}
			return ret;
		} 
		catch (Exception e) {
			throw new ServerInterfaceException("Error during server conversation", e);
		}
	}
	
	public String[] set_positions(List<Position> position_list) throws ServerInterfaceException {
		ZLNetworkManager networkManager = new ZLNetworkManager();
		JSONArray query = new JSONArray();
		JSONArray args = new JSONArray();
		
		JSONArray position_array = new JSONArray();
		for(Position pos : position_list){
			position_array.put(pos.toString());
		}
		
		try {
			args.put(myID);
			args.put(position_array);
			args.put(Digests.hmacSHA256(position_array.toString(), mySignature));
			
			query.put("set_positions");
			query.put(args);
			
			Request request = new Request(
									myContext.getString(R.string.api_url), 
									null, 
									query.toString()
									);
			networkManager.perform(request);
			JSONArray reply = request.getResponse();
			String[] ret = new String[reply.length()];
			for (int i = 0; i < reply.length(); ++i) {
				ret[i] = reply.getString(i);
			}
			return ret;
		}
		catch (Exception e) {
			throw new ServerInterfaceException("Error during server conversation", e);
		}
	}
	
	public class ServerInterfaceException extends Exception {
		private static final long serialVersionUID = -3168258901503477292L;
		public ServerInterfaceException(String message) {
			super(message);
		}
		public ServerInterfaceException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
