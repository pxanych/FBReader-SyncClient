package org.geometerplus.fbreader.plugin.synchronization;

import org.json.JSONObject;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import org.geometerplus.fbreader.plugin.synchronization.R;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AuthFacebook extends Activity {

	private Facebook fbAuth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		fbAuth = new Facebook(SyncConstants.FACEBOOK_APP_ID);
		fbAuth.authorize(this, new DialogListener() {
			
			public void onFacebookError(FacebookError e) {
				Intent errorDescription = new Intent();
				errorDescription.putExtra(AuthSelectActivity.ERROR_DESCRIPTION, e.getMessage());
				setResult(AuthSelectActivity.RESULT_ERROR, errorDescription);
				finish();
			}
			
			public void onError(DialogError e) {
				Intent errorDescription = new Intent();
				errorDescription.putExtra(AuthSelectActivity.ERROR_DESCRIPTION, e.getMessage());
				setResult(AuthSelectActivity.RESULT_ERROR, errorDescription);
				finish();
			}
			
			public void onComplete(Bundle values) {
				try {
					Context context = getApplicationContext();
					String jsonreply = fbAuth.request("me");
					JSONObject jsonReply = new JSONObject(jsonreply);
					String acc_hash = Digests.hashSHA256(jsonReply.getString("id"));
					Bundle reply = ServerInterface.login_facebook(context, acc_hash);
					if (reply.containsKey(ServerInterface.SIG_KEY)) {
						SyncAuth.addAccount(
								context, 
								AccountManager.get(context), 
								acc_hash, 
								reply.getString(ServerInterface.SIG_KEY));
						setResult(RESULT_OK);
						finish();
					} else {
						Intent errorDescription = new Intent();
						errorDescription.putExtra(
								AuthSelectActivity.ERROR_DESCRIPTION, 
								getString(R.string.internal_error) + ": "
									+ reply.getString(ServerInterface.ERROR_MESSAGE) 
								);
						setResult(AuthSelectActivity.RESULT_ERROR, errorDescription);
						finish();
					}
				} 
				catch (Exception e) {
					Intent errorDescription = new Intent();
					errorDescription.putExtra(
							AuthSelectActivity.ERROR_DESCRIPTION, 
							getString(R.string.internal_error) + ": " + e.getMessage()
							);
					setResult(AuthSelectActivity.RESULT_ERROR, errorDescription);
					finish();
				}
			}
			
			public void onCancel() {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}
	
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	fbAuth.authorizeCallback(requestCode, resultCode, data);
    }

}
