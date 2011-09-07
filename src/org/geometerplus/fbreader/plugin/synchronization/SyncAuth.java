package org.geometerplus.fbreader.plugin.synchronization;

import org.geometerplus.fbreader.plugin.synchronization.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class SyncAuth extends Activity {
	
	private static final String GOOGLE_DIALOG = "https://www.google.com/accounts/ServiceLogin";
	//?... - google shows login dialog
	private static final String GOOGLE_SIGNED_IN = "/accounts/SetSID";
	// - user clicks "Sign In"
	private boolean signedIn = false;
	
	
	public static boolean hasAccount(Context context) {
		AccountManager am = AccountManager.get(context);
		return am.getAccountsByType(context.getString(R.string.account_type)).length > 0;
	}
	
	
	public static Boolean addAccount(Context context, AccountManager accountManager, String id, String signature) {
		Account account = new Account(
				context.getString(R.string.account_name), 
				context.getString(R.string.account_type)
				);
		Bundle userData = new Bundle();
		userData.putString(SyncConstants.SETTINGS_ID, id);
		userData.putString(SyncConstants.SETTINGS_SIG, signature);

		boolean result = accountManager.addAccountExplicitly(account, "", userData);
		ContentResolver.setIsSyncable(account, context.getString(R.string.authority_positions), 1);
		ContentResolver.setIsSyncable(account, context.getString(R.string.authority_bookmarks), 1);
		ContentResolver.setIsSyncable(account, context.getString(R.string.authority_settings), 1);
		// TODO ContentResolver.addPeriodicSync(account, context.getString(R.string.authority_positions), new Bundle(), 1800);
		// TODO ContentResolver.addPeriodicSync(account, context.getString(R.string.authority_bookmarks), new Bundle(), 1800);
		ContentResolver.setSyncAutomatically(account, context.getString(R.string.authority_positions), true);
		ContentResolver.setSyncAutomatically(account, context.getString(R.string.authority_bookmarks), true);
		return result;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.auth_start);
		
		Button buttonContinue = (Button)findViewById(R.id.button_continue);
		buttonContinue.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						performAuth();
					}
				}
			);
	}
	
	
    private void performAuth() {
    	setContentView(R.layout.auth);
		WebView browserView = (WebView)findViewById(R.id.browserView);
		/////////////////////////////////////////////////////////
		// http://code.google.com/p/android/issues/detail?id=7189
		browserView.requestFocus(View.FOCUS_DOWN);
		browserView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
		/////////////////////////////////////////////////////////
		browserView.setNetworkAvailable(true);
		browserView.setWebViewClient(new SyncWebViewClient());
		browserView.getSettings().setJavaScriptEnabled(true);
		browserView.loadUrl(ServerInterface.AUTH_URL);
		browserView.setVisibility(View.INVISIBLE);
    }
	
    
	private void completeAuthentication(Uri reply) {
		String query = reply.getQuery();
    	try{
    		if (query == null) {
    			query = "";
    		}
			String[] args = query.split("&");
			for (int i = 0; i < args.length; ++i) {
				args[i] = args[i].split("=", 2)[1];
			}
			AccountManager accountManager = AccountManager.get(this);
			if("1".equals(args[0])) {
				addAccount(this, accountManager, args[1], args[2]);
				setResult(RESULT_OK);
		    	finish();
			} else {
				setResult(RESULT_CANCELED);
				finish();
			}
    	}
    	catch(ArrayIndexOutOfBoundsException e) {
    		Intent errorDescription = new Intent();
    		errorDescription.putExtra(AuthSelectActivity.ERROR_DESCRIPTION, e.getMessage());
    		setResult(AuthSelectActivity.RESULT_ERROR, errorDescription);
    		finish();
    	}
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
    	
    	@Override
    	public void onPageStarted(WebView view, String url, Bitmap favicon) {
    		if (GOOGLE_DIALOG.equals(url.split("\\?", 2)[0]) && !signedIn) {
    			view.setVisibility(View.VISIBLE);	
    		}
    		String path = Uri.parse(url).getPath();
    		if (GOOGLE_SIGNED_IN.equals(path)) {
    			view.setVisibility(View.GONE);
    			signedIn = true;
    		}
    		super.onPageStarted(view, url, favicon);
    	}
	}
    
    @Override
    protected void onPause() {
    	super.onPause();
    	finish();
    }
}
