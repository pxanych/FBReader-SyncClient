package com.sync;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class SyncAuth extends Dialog {
	
	public Boolean myAuthOk;
	public String myID;
	public String mySIG;
	
	private OnDismissListener myDismissListener;
	
	public SyncAuth(Context context, OnDismissListener listener){
		super(context);
		myDismissListener = listener;
		myAuthOk = false;
		myID = null;
		mySIG = null;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WebView browserView = new WebView(getContext());//(WebView)findViewById(R.id.browserView);
		
		setContentView(browserView);
		browserView.setNetworkAvailable(true);
		browserView.setWebViewClient(new SyncWebViewClient());
		browserView.getSettings().setJavaScriptEnabled(true);
		browserView.loadUrl("https://data.fbreader.org/sync/?sync_respondtype=url");
	}
	
	private void completeAuthentication(Uri response) {
    	String query = response.getQuery();
    	try{
    		if (query == null) {
    			Toast.makeText(
    							getContext(), 
    							"No result query in server response.",
    							Toast.LENGTH_SHORT
    						).show();
    			query = "";
    		}
			String[] args = query.split("&");
			for (int i = 0; i < args.length; ++i) {
				args[i] = args[i].split("=", 2)[1];
			}
			if("1".equals(args[0])) {
				myAuthOk = true;
				myID = args[1];
				mySIG = args[2];
			}
    	}
    	catch(ArrayIndexOutOfBoundsException e) {
    		myAuthOk = false;
    		Toast.makeText(getContext(), R.string.auth_failed_server, Toast.LENGTH_SHORT).show();
    	}
		dismiss();
	}
	
	@Override
	protected void onStop() {
    	myDismissListener.onDismiss(this);
		super.onStop();
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
}
