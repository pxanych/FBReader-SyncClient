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
import android.widget.Toast;

public class AuthFacebook extends Activity {

	private Facebook fbAuth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		fbAuth = new Facebook(SyncConstants.FACEBOOK_APP_ID);
		fbAuth.authorize(this, new DialogListener() {
			
			public void onFacebookError(FacebookError e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				onCancel();
			}
			
			public void onError(DialogError e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				onCancel();
			}
			
			public void onComplete(Bundle values) {
				try {
					Context context = getApplicationContext();
					String jsonreply = fbAuth.request("me");
					JSONObject jsonReply = new JSONObject(jsonreply);
					String acc_hash = Digests.hashSHA256(jsonReply.getString("id"));
					String sig = ServerInterface.login_facebook(context, acc_hash);
					SyncAuth.addAccount(context, AccountManager.get(context), acc_hash, sig);
					finish();
				} 
				catch (Exception e) {
					Toast.makeText(
							AuthFacebook.this, 
							getString(R.string.internal_error) + ": " + e.getMessage(), 
							Toast.LENGTH_SHORT
							).show();
					onCancel();
				}
			}
			
			public void onCancel() {
				finish();
				startActivity(new Intent(AuthFacebook.this, AuthSelectActivity.class));
			}
		});
	}
	

        
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	super.onActivityResult(requestCode, resultCode, data);
    	fbAuth.authorizeCallback(requestCode, resultCode, data);
    }

}
