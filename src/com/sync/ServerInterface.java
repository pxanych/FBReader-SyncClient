package com.sync;


import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.List;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.json.JSONArray;

import com.sync.service.FBSyncPositionsProvider.Position;

import android.content.Context;


public class ServerInterface{
	
	public static String login_facebook(Context context, String account_hash)
					throws ServerInterfaceException {
		ZLNetworkManager networkManager = new ZLNetworkManager();
		JSONArray query = new JSONArray();
		JSONArray args = new JSONArray();
		
		Boolean success;
		String msg;
		try {
			args.put(account_hash);
			args.put(Digests.hashSHA256(account_hash + SyncConstants.FACEBOOK_APP_SECRET));
			
			query.put("login_facebook");
			query.put(args);
			
			Request request = new Request(
							context.getString(R.string.api_url), 
							null, 
							query.toString()
							);
			networkManager.perform(request);
			JSONArray reply = request.getResponse();
			success = reply.getBoolean(0);
			if (success) {
				return reply.getString(2);
			} else {
				msg = reply.getString(1);
			}
		}
		catch (Exception e) {
			throw new ServerInterfaceException("Error during server conversation", e);
		}
		throw new ServerInterfaceException(msg);
	}
	
	
	public static String our_auth_register(Context context, String account, String password)
					throws ServerInterfaceException {
		return our_auth(context, account, password, "register");
	}

	public static String our_auth_login(Context context, String account, String password) 
					throws ServerInterfaceException {
		return our_auth(context, account, password, "login");
	}

	private static String our_auth(Context context, String account, 
			String password, String operation) throws ServerInterfaceException {
	
		ZLNetworkManager networkManager = new ZLNetworkManager();
		JSONArray query = new JSONArray();
		JSONArray args = new JSONArray();
		
		try {
			args.put(account);
			args.put(Digests.hashSHA256(password));
			
			query.put(operation);
			query.put(args);
			
			Request request = new Request(
							context.getString(R.string.api_url), 
							null, 
							query.toString()
							);
			networkManager.perform(request);
			JSONArray reply = request.getResponse();
			Boolean success = reply.getBoolean(0);
			String msg = reply.getString(1);
			
			if (success) {
				return msg;
			} else {
				throw new ServerInterfaceException(msg);
			}
		}
		catch (Exception e) {
			if (e instanceof ServerInterfaceException) {
				throw (ServerInterfaceException)e;
		} else {
			throw new ServerInterfaceException("Error during server conversation", e);
			}
		}
	}


	public ServerInterface(Context context, String id, String signature) 
			throws ServerInterfaceException {
		myContext = context;
		myID = id;
		mySignature = signature;
		try {
			myServerUrl = new URL(myContext.getString(R.string.host));
		}
		catch (MalformedURLException e) {
			throw new ServerInterfaceException("Malformed URL", e);
		}
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
	
	public static class ServerInterfaceException extends Exception {
		private static final long serialVersionUID = -3168258901503477292L;
		public ServerInterfaceException(String message) {
			super(message);
		}
		public ServerInterfaceException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
