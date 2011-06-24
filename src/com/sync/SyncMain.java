package com.sync;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SyncMain extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// https://data.fbreader.org/sync/
				String ID = "https://www.google.com/accounts/o8/id?id=AItOawn30drmKXwpwx9qehEGyHSnXaKo5fq6CDY";
				String Signature = "3ba5344f4ec8134051f8fe194083e0633a20e37a13e3ec85785d3d3622111183";
				
				EditText editUpload = (EditText) findViewById(R.id.editText1);
				TextView uploadResult = (TextView) findViewById(R.id.textView2);
				
				ServerConversation serverConversation = 
					new ServerConversation(null, ID, Signature);
				String uploadString = editUpload.getText().toString();
				JSONArray response = serverConversation.uploadFile(uploadString);
				if (response != null)
				{
					uploadResult.setText(response.toString());					
				}
				else
				{
					uploadResult.setText("Failed");
				}
			}
		});
        
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// https://data.fbreader.org/sync/
				String ID = "https://www.google.com/accounts/o8/id?id=AItOawn30drmKXwpwx9qehEGyHSnXaKo5fq6CDY";
				String Signature = "3ba5344f4ec8134051f8fe194083e0633a20e37a13e3ec85785d3d3622111183";
				
				EditText editDownload = (EditText) findViewById(R.id.editText2);
				
				ServerConversation serverConversation = 
					new ServerConversation(null, ID, Signature);
				JSONArray response = serverConversation.downloadFile();
				if (response != null)
				{
					editDownload.setText(response.toString());					
				}
				else
				{
					editDownload.setText("Failed");
				}
			}
		});
        
        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("https://data.fbreader.org/sync/?sync_respondtype=url"));
				startActivity(i);
			}
		});
        
        
    }
}
