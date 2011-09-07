/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.api;

public interface ApiListener {
	int EVENT_READ_MODE_OPENED = 1;
	int EVENT_READ_MODE_CLOSED = 2;
	int EVENT_BOOK_OPENED = 3;

	void onEvent(int event);
}
