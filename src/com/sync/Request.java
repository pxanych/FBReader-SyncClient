package com.sync;


import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.json.JSONException;
import org.json.JSONArray;


public class Request extends ZLNetworkRequest {
		
	public Request(String url) {
		super(url);
	}

	
	public Request(String url, String sslCertificate, String postData) {
		super(url, sslCertificate, postData);
	}
	
	
	private JSONArray myResponse;
	
	public JSONArray getResponse()
	{
		return myResponse;
	}
	

	@Override
	public void handleStream(InputStream inputStream, int length)
			throws IOException, ZLNetworkException {
		byte[] buf = new byte[(length != -1) ? length : 1024];
		String s = null;
		if (length == -1)
		{
			StringBuilder sb = new StringBuilder();
			int nread;
			while ((nread = inputStream.read(buf, 0, 1024)) != -1)
			{
				sb.append(new String(buf, 0, nread));
			}
			s = sb.toString();		
		} else 
		{
			inputStream.read(buf, 0, length);
			s = new String(buf);
		}
		try {
			myResponse = new JSONArray(s);
		} catch (JSONException e) {
			myResponse = null;
		}
	}
}
