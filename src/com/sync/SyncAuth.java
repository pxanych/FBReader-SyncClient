package com.sync;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class SyncAuth extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_auth);
		WebView browserView = (WebView)findViewById(R.id.browserView);
		browserView.setNetworkAvailable(true);
		browserView.setWebViewClient(new SyncWebViewClient());
		browserView.getSettings().setJavaScriptEnabled(true);
		browserView.loadUrl("https://data.fbreader.org/sync/?sync_respondtype=url");
	}
	
	private void completeAuthentication(Uri response) {
		Intent result = new Intent();
    	String query = response.getQuery();
    	try{
    		if (query == null) {
    			Toast.makeText(
    							this, 
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
				result.putExtra(getString(R.string.settings_auth), true);
				result.putExtra(getString(R.string.settings_id), args[1]);
				result.putExtra(getString(R.string.settings_sig), args[2]);
			} else {
				result.putExtra(getString(R.string.settings_auth), false);
			}
    	}
    	catch(ArrayIndexOutOfBoundsException e) {
    		result.putExtra(getString(R.string.settings_auth), false);
    		Toast.makeText(this, R.string.auth_failed_server, Toast.LENGTH_SHORT).show();
    	}
    	setResult(RESULT_OK, result);
    	finish();
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
