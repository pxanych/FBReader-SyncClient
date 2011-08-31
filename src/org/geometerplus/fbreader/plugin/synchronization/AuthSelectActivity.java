package org.geometerplus.fbreader.plugin.synchronization;

import com.android.settings.IconPreferenceScreen;
import org.geometerplus.fbreader.plugin.synchronization.R;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class AuthSelectActivity extends PreferenceActivity {
	
	public final static int RESULT_ERROR = RESULT_FIRST_USER + 1;
	public final static String ERROR_DESCRIPTION = "error_description";
	private final int OUR_AUTH_REQUEST_CODE = 1;
	private final int GOOGLE_REQUEST_CODE = 2;
	private final int FACEBOOK_REQUEST_CODE = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.auth_selection);
		
		String ourSignInKey = getString(R.string.auth_our_acc_sign_in_key);
		String ourRegisterKey = getString(R.string.auth_our_acc_register_key);
		String googleAccKey = getString(R.string.auth_google_key);
		String fbAccKey = getString(R.string.auth_facebook_key);

		Preference ourSignIn = 
			(Preference)findPreference(ourSignInKey);
		Preference ourRegister = 
			(Preference)findPreference(ourRegisterKey);
		IconPreferenceScreen googleAcc = 
			(IconPreferenceScreen)findPreference(googleAccKey);
		IconPreferenceScreen fbAcc = 
			(IconPreferenceScreen)findPreference(fbAccKey);
		
		Drawable googleIcon = getResources().getDrawable(R.drawable.google_icon);
		Drawable fbIcon = getResources().getDrawable(R.drawable.fb_icon);
		
		googleAcc.setIcon(googleIcon);
		fbAcc.setIcon(fbIcon);
		
		ourSignIn.setOnPreferenceClickListener(new OurAccOnClickListener(false));
		ourRegister.setOnPreferenceClickListener(new OurAccOnClickListener(true));
		googleAcc.setOnPreferenceClickListener(new GoogleOnClickListener());
		fbAcc.setOnPreferenceClickListener(new FacebookOnClickListener());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			startActivity(new Intent(this, MainScreen.class));
			finish();
			break;
		case RESULT_ERROR:
			Toast.makeText(
					this, 
					data.getStringExtra(ERROR_DESCRIPTION), 
					Toast.LENGTH_LONG
					).show();
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	private class GoogleOnClickListener implements OnPreferenceClickListener {
		public boolean onPreferenceClick(Preference preference) {
			Intent forward = new Intent(getApplicationContext(), SyncAuth.class);
			startActivityForResult(forward, GOOGLE_REQUEST_CODE);
			return true;
		}
	}
	
	private class FacebookOnClickListener implements OnPreferenceClickListener {
		public boolean onPreferenceClick(Preference preference) {
			Intent forward = new Intent(AuthSelectActivity.this, AuthFacebook.class);
			startActivityForResult(forward, FACEBOOK_REQUEST_CODE);
			return true;
		}
	}
	
	private class OurAccOnClickListener implements OnPreferenceClickListener {
		
		public OurAccOnClickListener(boolean registerMode){
			myRegisterMode = registerMode;
		}
		
		private boolean myRegisterMode;
		
		public boolean onPreferenceClick(Preference preference) {
			Intent ourAuth = new Intent(AuthSelectActivity.this, AuthOur.class);
			ourAuth.putExtra(AuthOur.REGISTER_FLAG, myRegisterMode);
			startActivityForResult(ourAuth, OUR_AUTH_REQUEST_CODE);
			return true;
		}
	}
}
