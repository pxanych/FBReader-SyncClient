package org.geometerplus.fbreader.plugin.synchronization.service;

import org.geometerplus.android.fbreader.api.ApiListener;

import android.content.Context;
import android.content.Intent;


public class FBReaderApiListener implements ApiListener {
	
	private Context myContext;
	private static FBReaderApiListener ourInstance = null;
	
	public static FBReaderApiListener getInstance(Context context) {
		return (ourInstance != null) ? ourInstance : new FBReaderApiListener(context);
	}
	
	private FBReaderApiListener(Context context) {
		myContext = context;
	}
	
	public static final String EVENT_CODE = "event.code";
	
	public void onEvent(int event) {
		Intent serviceIntent = new Intent(myContext, FBSyncPositionsService.class);
		switch (event) {
		case ApiListener.EVENT_READ_MODE_OPENED:
			serviceIntent.putExtra(
					FBSyncPositionsService.FBREADER_ACTION, 
					FBSyncPositionsService.FBREADER_PULL_POSITION
					);
			break;
		case ApiListener.EVENT_READ_MODE_CLOSED:
			serviceIntent.putExtra(
					FBSyncPositionsService.FBREADER_ACTION, 
					FBSyncPositionsService.FBREADER_PUSH_POSITION
					);
		default:
			break;
		}
		myContext.startService(serviceIntent);
	}
}
