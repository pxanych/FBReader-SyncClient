package org.geometerplus.fbreader.plugin.synchronization;

import java.nio.channels.ClosedByInterruptException;

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
		super.onCreate(savedInstanceState);
		fbAuth = new Facebook(SyncConstants.FACEBOOK_APP_ID);
		fbAuth.authorize(this, new FacebookDialogListener());
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	fbAuth.authorizeCallback(requestCode, resultCode, data);
    }
    
    
    private class FacebookDialogListener implements DialogListener {
		
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
				WaitDialog dialog = new WaitDialog(AuthFacebook.this);
				Thread authThread = new AuthThread(dialog);
				dialog.setBackgroundThread(authThread);
				authThread.start();
				dialog.show();
			}
			catch (RuntimeException e){
				e.printStackTrace();
			}
		}
		
		public void onCancel() {
			setResult(RESULT_CANCELED);
			finish();
		}
	}
    
    
    private class AuthThread extends Thread {
    	
    	private WaitDialog myDialog;
    	
    	public AuthThread(WaitDialog dialog) {
			super();
			myDialog = dialog;
		}
    	
    	private void cancel() {
    		setResult(RESULT_CANCELED);
			myDialog.dismiss();
			finish();
    	}
    	
    	@Override
    	public void run() {
    		try {
				Context context = getApplicationContext();
				String jsonreply = fbAuth.request("me");
				JSONObject jsonReply = new JSONObject(jsonreply);
				String acc_hash = Digests.hashSHA256(jsonReply.getString("id"));
				Bundle reply = ServerInterface.loginFacebook(context, acc_hash);
				if (reply.containsKey(ServerInterface.SIG_KEY)) {
					if (interrupted()) {
						cancel();
						return;
					}
					SyncAuth.addAccount(
							context, 
							AccountManager.get(context), 
							acc_hash, 
							reply.getString(ServerInterface.SIG_KEY));
					setResult(RESULT_OK);
					myDialog.dismiss();
					finish();
				} else {
					Intent errorDescription = new Intent();
					errorDescription.putExtra(
							AuthSelectActivity.ERROR_DESCRIPTION, 
							getString(R.string.internal_error) + ": "
								+ reply.getString(ServerInterface.ERROR_MESSAGE) 
							);
					setResult(AuthSelectActivity.RESULT_ERROR, errorDescription);
					myDialog.dismiss();
					finish();
				}
			}
    		catch (ClosedByInterruptException e) {
    			cancel();
    		}
			catch (Exception e) {
				Intent errorDescription = new Intent();
				errorDescription.putExtra(
						AuthSelectActivity.ERROR_DESCRIPTION, 
						getString(R.string.internal_error) + ": " + e.getMessage()
						);
				setResult(AuthSelectActivity.RESULT_ERROR, errorDescription);
				myDialog.dismiss();
				finish();
			}
    	}
    }

}
