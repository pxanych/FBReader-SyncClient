package org.geometerplus.fbreader.plugin.synchronization;


import org.geometerplus.fbreader.plugin.synchronization.ServerInterface.ServerInterfaceException;

import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class AuthOur extends Activity {
	
	public static final String REGISTER_FLAG = "register_mode";
	private EditText myLogin;
	private EditText myPassword;
	private EditText myPasswordConfirm;
	private Button myButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth_our);
		
		myLogin = (EditText)findViewById(R.id.loginEditText);
		myPassword = (EditText)findViewById(R.id.passwordEditText);
		myPasswordConfirm = (EditText)findViewById(R.id.passwordConfirmEditText);
		myButton = (Button)findViewById(R.id.button);
	
		if (getIntent().getBooleanExtra(REGISTER_FLAG, false)) {
			myPasswordConfirm.setVisibility(View.VISIBLE);
			LayoutParams layoutParams = (LayoutParams)myButton.getLayoutParams();
			layoutParams.addRule(RelativeLayout.BELOW, R.id.passwordConfirmEditText);
			myButton.setText(R.string.auth_our_acc_do_register);
			myButton.setOnClickListener(new RegisterClickListener());
			setTitle(getString(R.string.auth_our_acc_register));
		} else {
			myPasswordConfirm.setVisibility(View.INVISIBLE);
			myButton.setText(R.string.auth_our_acc_do_sign_in);
			myButton.setOnClickListener(new LoginClickListener());
			setTitle(getString(R.string.auth_our_acc_sign_in));
		}
		
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
    	finish();
    }
    
    private class RegisterClickListener implements OnClickListener {
    	
		public void onClick(View v) {
			
			String account = myLogin.getText().toString();
			String pass = myPassword.getText().toString();
			String passConfirm = myPasswordConfirm.getText().toString();
			if (!pass.equals(passConfirm)) {
				myPassword.setText("");
				myPasswordConfirm.setText("");
				Toast.makeText(
						getApplicationContext(),
						R.string.not_match,
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (pass.length() == 0 || account.length() == 0){
				Toast.makeText(
						getApplicationContext(),
						R.string.empty_login_or_pass,
						Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				Bundle reply = ServerInterface.ourAuthRegister(
										AuthOur.this, 
										account, 
										pass
										);
				if (reply.containsKey(ServerInterface.SIG_KEY)) {
					SyncAuth.addAccount(
							AuthOur.this, 
							AccountManager.get(AuthOur.this), 
							account, 
							reply.getString(ServerInterface.SIG_KEY)
							);
					setResult(RESULT_OK);
					finish();
				} else {
					setResult(RESULT_CANCELED);
					switch (reply.getInt(ServerInterface.ERROR_CODE)) {
					case ServerInterface.DB_ERROR:
						Toast.makeText(
								AuthOur.this,
								getString(R.string.internal_error), 
								Toast.LENGTH_LONG
								).show();
						return;
					case ServerInterface.ALREADY_REGISTERED:
						Toast.makeText(
								AuthOur.this,
								getString(R.string.already_registered), 
								Toast.LENGTH_LONG
								).show();
							return;
					}
				}
			}
			catch (ServerInterfaceException e) {
				Toast.makeText(
						AuthOur.this,
						getString(R.string.internal_error) + ": " + e.getMessage(), 
						Toast.LENGTH_LONG
						).show();
			}
		}
	}
    
    
    private class LoginClickListener implements OnClickListener {

		public void onClick(View v) {
			EditText login = (EditText)findViewById(R.id.loginEditText);
			EditText password = (EditText)findViewById(R.id.passwordEditText);

			String account = login.getText().toString();
			String pass = password.getText().toString();
			
			if (pass.length() == 0 || account.length() == 0){
				Toast.makeText(
						getApplicationContext(),
						R.string.empty_login_or_pass,
						Toast.LENGTH_SHORT).show();
				return;
			}
				
			try {
				Bundle reply = ServerInterface.ourAuthLogin(
										getApplicationContext(), 
										account, 
										pass
										);
				if (reply.containsKey(ServerInterface.SIG_KEY)) {
					SyncAuth.addAccount(
							AuthOur.this, 
							AccountManager.get(AuthOur.this), 
							account, 
							reply.getString(ServerInterface.SIG_KEY)
							);
					setResult(RESULT_OK);
					finish();
				} else {
					setResult(RESULT_CANCELED);
					switch (reply.getInt(ServerInterface.ERROR_CODE)) {
					case ServerInterface.DB_ERROR:
						Toast.makeText(
								AuthOur.this,
								getString(R.string.internal_error), 
								Toast.LENGTH_LONG
								).show();
						return;
					case ServerInterface.NO_ACCOUNT:
						Toast.makeText(
								AuthOur.this,
								getString(R.string.no_account), 
								Toast.LENGTH_LONG
								).show();
						return;
					case ServerInterface.WRONG_PW:
						Toast.makeText(
								AuthOur.this,
								getString(R.string.wrong_pw), 
								Toast.LENGTH_LONG
								).show();
						return;
					}
				}
			}
			catch (ServerInterfaceException e) {
				Toast.makeText(
						AuthOur.this,
						getString(R.string.internal_error) + ": " + e.getMessage(), 
						Toast.LENGTH_LONG
						).show();
			}
		}
	}
}
