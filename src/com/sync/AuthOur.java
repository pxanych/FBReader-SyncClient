package com.sync;

import com.sync.ServerInterface.ServerInterfaceException;

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
	
	private EditText login;
	private EditText password;
	private EditText passwordConfirm;
	private Button loginButton;
	private Button registerButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth_our);
		
		login = (EditText)findViewById(R.id.loginEditText);
		password = (EditText)findViewById(R.id.passwordEditText);
		passwordConfirm = (EditText)findViewById(R.id.passwordConfirmEditText);
		loginButton = (Button)findViewById(R.id.buttonLogin);
		registerButton = (Button)findViewById(R.id.buttonRegister);
	
		registerButton.setOnClickListener(new RegisterClickListener());
		loginButton.setOnClickListener(new LoginClickListener());
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
    	finish();
    }
    
    
    private class RegisterClickListener implements OnClickListener {
    	
    	private Boolean inRegisterMode = false;

		public void onClick(View v) {
			
			if (!inRegisterMode) {
				loginButton.setVisibility(View.GONE);
				passwordConfirm.setVisibility(View.VISIBLE);
				
				LayoutParams regButtonParams = (LayoutParams)registerButton.getLayoutParams();
				regButtonParams.addRule(RelativeLayout.BELOW, R.id.passwordConfirmEditText);
				inRegisterMode = true;
			} else {
				String account = login.getText().toString();
				String pass = password.getText().toString();
				String passConfirm = passwordConfirm.getText().toString();
				if (!pass.equals(passConfirm)) {
					password.setText("");
					passwordConfirm.setText("");
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
					String sig = ServerInterface.our_auth_register(
											getApplicationContext(), 
											account, 
											pass
											);
					SyncAuth.addAccount(
							AuthOur.this, 
							AccountManager.get(AuthOur.this), 
							account, 
							sig
							);
				}
				catch (ServerInterfaceException e) {
					if (e.getMessage().equals("already_registered")) {
						Toast.makeText(
								getApplicationContext(),
								R.string.already_registered,
								Toast.LENGTH_SHORT).show();
						return;
					} else {
						Toast.makeText(
								getApplicationContext(),
								getString(R.string.internal_error) + ": " + e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				}
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
				String sig = ServerInterface.our_auth_login(
										getApplicationContext(), 
										account, 
										pass
										);
				SyncAuth.addAccount(
						AuthOur.this, 
						AccountManager.get(AuthOur.this), 
						account, 
						sig
						);
			}
			catch (ServerInterfaceException e) {
				if (e.getMessage().equals("wrong_pw")) {
					Toast.makeText(
							getApplicationContext(),
							R.string.wrong_pw,
							Toast.LENGTH_SHORT).show();
					return;
				} 
				if (e.getMessage().equals("no_account")) {
					Toast.makeText(
							getApplicationContext(),
							R.string.no_account,
							Toast.LENGTH_SHORT).show();
					return;
				} else {
					Toast.makeText(
							getApplicationContext(),
							getString(R.string.internal_error) + ": " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
}
