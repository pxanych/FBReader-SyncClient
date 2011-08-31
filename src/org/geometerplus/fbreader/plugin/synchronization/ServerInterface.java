package org.geometerplus.fbreader.plugin.synchronization;


import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.List;

import org.geometerplus.fbreader.plugin.synchronization.service.FBSyncPositionsProvider.Position;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Debug;


public class ServerInterface{
	
    public static final String HOST = "https://data.fbreader.org/sync/";
    public static final String AUTH_URL = "https://data.fbreader.org/sync/?sync_respondtype=url";
    public static final String API_URL = "https://data.fbreader.org/sync/sync_api_interface.php5";

    public static final String ID_KEY = "id";
    public static final String SIG_KEY = "signature";
    public static final String SUCCESS_KEY = "success";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String METHOD_KEY = "method";
    public static final String ARGS_KEY = "arguments";
    public static final String DATA_ARRAY_KEY = "data_array";
    public static final String ERROR_CODE = "error_code";
    
    public static final int DB_ERROR = 1;
    public static final int ALREADY_REGISTERED = 2;
    public static final int NO_ACCOUNT = 3;
    public static final int WRONG_PW = 4;
    public static final int WRONG_DIGEST = 5;
    //	Server php defines:
	//    define("ID_KEY", "id");
	//    define("SIG_KEY", "signature");
	//    define("SUCCESS_KEY", "success");
	//    define("ERROR_MESSAGE", "error_message");
	//    define("METHOD_KEY", "method");
	//    define("ARGS_KEY", "arguments");
//    define("ERROR_CODE", "error_code");
//    define("DB_ERROR", 1);
//    define("ALREADY_REGISTERED", 2);
//    define("NO_ACCOUNT", 3);
//    define("WRONG_PW", 4);
//    define("WRONG_DIGEST", 5);
    
    public static Bundle jsonToBundle(JSONObject json) {
    	Bundle result = new Bundle();
    	Iterator<?> jsonKeys = json.keys();
    	while (jsonKeys.hasNext()) {
    		Object next = jsonKeys.next();
    		if (next instanceof String) {
    			String key = (String)next;
    			if (key.equals(SUCCESS_KEY)) {
        			result.putBoolean(key, json.optBoolean(key));
        			continue;
        		}
    			if (key.equals(ERROR_CODE)) {
    				result.putInt(key, json.optInt(key));
    				continue;
    			}
    			result.putString(key, json.optString(key, ""));
    		}
    	}
    	return result;
    }
    
	public static Bundle login_facebook(Context context, String account_hash) 
						throws ServerInterfaceException {
		JSONArray args = new JSONArray();
		args.put(account_hash);
		args.put(Digests.hashSHA256(account_hash + SyncConstants.FACEBOOK_APP_SECRET));
		return jsonToBundle(callAPI(ApiMethod.LOGIN_FACEBOOK, args));
	}
	
	private static JSONObject callAPI(ApiMethod method, JSONArray args) 
							throws ServerInterfaceException {
		ZLNetworkManager networkManager = new ZLNetworkManager();
		JSONObject query = new JSONObject();

		try {
			query.put(METHOD_KEY, method);
			query.put(ARGS_KEY, args);
			/////////////////////////
			Debug.waitForDebugger();
			/////////////////////////
			Request request = new Request(API_URL, null, query.toString());
			networkManager.perform(request);
			return request.getResponse();
		} 
		catch (ZLNetworkException e) {
			throw new ServerInterfaceException(e);
		}
		catch (JSONException e) {
			throw new ServerInterfaceException(e);
		}
	}
	
	
	public static Bundle our_auth_register(Context context, String account, String password)
					throws ServerInterfaceException {
		return our_auth(context, account, password, ApiMethod.REGISTER);
	}

	public static Bundle our_auth_login(Context context, String account, String password) 
					throws ServerInterfaceException {
		return our_auth(context, account, password, ApiMethod.LOGIN);
	}

	private static Bundle our_auth(Context context, String account, 
								   String password, ApiMethod method) 
									throws ServerInterfaceException {
		JSONArray args = new JSONArray();
		args.put(account);
		args.put(Digests.hashSHA256(password));
		return jsonToBundle(callAPI(method, args));
	}


	public ServerInterface(String id, String signature) throws ServerInterfaceException {
		myID = id;
		mySignature = signature;
	}
	
	private String myID;
	private String mySignature;
	
	
	public Position[] get_positions(List<String> books) throws ServerInterfaceException {
		JSONArray args = new JSONArray();
		
		args.put(myID);
		args.put(new JSONArray(books));
		try {
			JSONObject reply = callAPI(ApiMethod.GET_POSITIONS, args);
			if (reply.optBoolean(SUCCESS_KEY)) {
				JSONArray jsonPositions = reply.getJSONArray(DATA_ARRAY_KEY);
				Position[] ret = new Position[jsonPositions.length()];
				for (int i = 0; i < jsonPositions.length(); ++i) {
					ret[i] = new Position(jsonPositions.getString(i));
				}
				return ret;
			} else {
				throw new ServerInterfaceException(reply.optString(ERROR_MESSAGE));
			}
		} 
		catch (JSONException e) {
			throw new ServerInterfaceException(e);
		}
	}
	
	public String[] set_positions(List<Position> position_list) 
				throws ServerInterfaceException {
		
		JSONArray args = new JSONArray();
		JSONArray position_array = new JSONArray(position_list);
//		JSONArray position_array = new JSONArray();
//		for(Position pos : position_list){
//			position_array.put(pos.toString());
//		}
		args.put(myID);
		args.put(position_array);
		try {
			args.put(Digests.hmacSHA256(position_array.toString(), mySignature));
		} 
		catch (InvalidKeyException e) {
			throw new ServerInterfaceException(e);
		}
		
		try {
			JSONObject reply = callAPI(ApiMethod.SET_POSITIONS, args);
			JSONArray replyArray = reply.getJSONArray(DATA_ARRAY_KEY);
			
			String[] ret = new String[replyArray.length()];
			for (int i = 0; i < replyArray.length(); ++i) {
				ret[i] = replyArray.getString(i);
			}
			return ret;
		}
		catch (JSONException e) {
			throw new ServerInterfaceException(e);
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
		public ServerInterfaceException(Throwable cause) {
			super(cause);
		}
	}
	
	private enum ApiMethod {
		SET_POSITIONS("set_positions"),//
		GET_POSITIONS("get_positions"),//
        REGISTER("register"),
        LOGIN("login"),
        LOGIN_FACEBOOK("login_facebook");
        
		private String myValue;
		
		private ApiMethod(String str) {
			myValue = str;
		}
		
		@Override
		public String toString() {
			return myValue;
		}
	}
}
