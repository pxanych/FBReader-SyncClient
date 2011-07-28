package com.sync;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;

public class handleServerAuth extends Activity 
{
	@Override
	public void onCreate(Bundle savedState)
	{
		super.onCreate(savedState);
		Intent i = getIntent();
		String dataString = i.getDataString();
		setContentView(R.layout.handle_auth);
		TextView uriTextView = (TextView) findViewById(R.id.textView2);
		uriTextView.setText(dataString);
		return;
	}
	

}
