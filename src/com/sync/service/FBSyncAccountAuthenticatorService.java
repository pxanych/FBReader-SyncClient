package com.sync.service;

import com.sync.R;
import com.sync.SyncAuth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class FBSyncAccountAuthenticatorService extends Service {
	
	private static FBSyncAccountAuthenticator ourAccountAuthenticator = null;

	public FBSyncAccountAuthenticatorService() {
		super();
	}

	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		if (intent.getAction().equals(
				android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT
				)) {
			ret = getAuthenticator(this).getIBinder();			
		}
		return ret;
	}

	private FBSyncAccountAuthenticator getAuthenticator(Context context) {
		if (ourAccountAuthenticator == null) {
			ourAccountAuthenticator = new FBSyncAccountAuthenticator(context);
		}
		return ourAccountAuthenticator;
	}
	
	public static Boolean accountExists(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = 
			accountManager.getAccountsByType(context.getString(R.string.account_type));
		return accounts.length != 0;
	}
	
	
	private static class FBSyncAccountAuthenticator extends AbstractAccountAuthenticator {
		
		private Context myContext;
		
		public FBSyncAccountAuthenticator(Context context) {
			super(context);
			myContext = context;
		}
		
		@Override
		public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, 
				String authTokenType, String[] requiredFeatures, Bundle options)
				throws NetworkErrorException {
			
			Bundle reply = new Bundle();
			if(accountExists(myContext)) {
				reply.putInt(
						AccountManager.KEY_ERROR_CODE, 
						AccountManager.ERROR_CODE_CANCELED
						);
				reply.putString(
						AccountManager.KEY_ERROR_MESSAGE, 
						myContext.getString(R.string.auth_one_account)
						);
			} else {
				Intent i = new Intent(myContext, SyncAuth.class);
				i.setAction("com.sync.AUTH");
				i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
				reply.putParcelable(AccountManager.KEY_INTENT, i);
			}
			return reply;
		}
		
		public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
			throw new UnsupportedOperationException();
		}
		 
		public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
			throw new UnsupportedOperationException();
		}
		 
		public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
			throw new UnsupportedOperationException();
		}
		 
		public String getAuthTokenLabel(String authTokenType) {
			throw new UnsupportedOperationException();
		}
		 
		public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
			throw new UnsupportedOperationException();
		}
		 
		public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
			throw new UnsupportedOperationException();
		}
	}
	
}
