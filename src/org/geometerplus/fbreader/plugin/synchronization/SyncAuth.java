package org.geometerplus.fbreader.plugin.synchronization;

import org.geometerplus.fbreader.plugin.synchronization.R;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class SyncAuth extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().getExtras() == null){
			finish();
		}
		
		setContentView(R.layout.auth_start);
		
		
		TextView about_openid = (TextView)findViewById(R.id.about_openid);
		about_openid.setText(Html.fromHtml(getString(R.string.auth_start_about_openid)));
		about_openid.setMovementMethod(LinkMovementMethod.getInstance());
		
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
		browserView.loadUrl(SyncConstants.AUTH_URL);
    }
	
    
	private void completeAuthentication(Uri reply) {
		Bundle extras = getIntent().getExtras();
		AccountAuthenticatorResponse response = 
				extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
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
			} else {
				response.onError(
						AccountManager.ERROR_CODE_CANCELED, 
						getString(R.string.operation_cancelled)
						);		
			}
    	}
    	catch(ArrayIndexOutOfBoundsException e) {
    		response.onError(
    				AccountManager.ERROR_CODE_INVALID_RESPONSE, 
    				getString(R.string.bad_server_response)
    				);
    	}
    	setResult(RESULT_OK);
    	finish();
	}
	
	
	public static Boolean addAccount(Context context, AccountManager accountManager, String id, String signature) {
		Account account = new Account(
				context.getString(R.string.account_name), 
				context.getString(R.string.account_type)
				);
		Bundle userData = new Bundle();
		userData.putString(SyncConstants.SETTINGS_ID, id);
		userData.putString(SyncConstants.SETTINGS_SIG, signature);
		ContentResolver.setIsSyncable(account, context.getString(R.string.authority_positions), 1);
		ContentResolver.setIsSyncable(account, context.getString(R.string.authority_bookmarks), 1);
		ContentResolver.setIsSyncable(account, context.getString(R.string.authority_settings), 1);
		ContentResolver.addPeriodicSync(account, context.getString(R.string.authority_positions), new Bundle(), 1800);
		ContentResolver.addPeriodicSync(account, context.getString(R.string.authority_bookmarks), new Bundle(), 1800);
		ContentResolver.setSyncAutomatically(account, context.getString(R.string.authority_positions), true);
		ContentResolver.setSyncAutomatically(account, context.getString(R.string.authority_bookmarks), true);
		return accountManager.addAccountExplicitly(account, "", userData);
	}
	
	
    private class SyncWebViewClient extends WebViewClient {
    	@Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	if (!url.startsWith("fbsync")) {
		        view.loadUrl(url);
		        return true;
	    	} else {
	    		view.setVisibility(View.INVISIBLE);
	    		completeAuthentication(Uri.parse(url));
	    		return false;
	    	}
	    }
	}
    
    @Override
    protected void onPause() {
    	super.onPause();
    	finish();
    }
}
