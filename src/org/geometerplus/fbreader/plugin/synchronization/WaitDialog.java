package org.geometerplus.fbreader.plugin.synchronization;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
public class WaitDialog extends Dialog {
	
	private Thread myBackgroundThread = null;
	
	public WaitDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		boolean featureGranted = requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.wail_dialog);
		if (featureGranted) {
			getWindow().setFeatureInt(Window.FEATURE_NO_TITLE, 1);
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onBackPressed() {
		if (myBackgroundThread != null) {
			myBackgroundThread.interrupt();
		}
		super.onBackPressed();
	}
	
	public void setBackgroundThread(Thread backgroundThread) {
		myBackgroundThread = backgroundThread;
	}
}
