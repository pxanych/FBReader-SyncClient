package service;

import java.util.zip.DataFormatException;

import org.geometerplus.android.fbreader.api.TextPosition;


public class FBSyncPositionsProvider extends FBSyncBaseContentProvider {
	
	public static class Position {
		public final String myHash;
		public final TextPosition myPosition;
		public final int myTimestamp;
		
		public Position(String hash, TextPosition position, int timestamp){
			myHash = hash;
			myTimestamp = timestamp;
			myPosition = position;
		}
		
		public Position(String stringValue) throws DataFormatException{
			try {
				String[] parts = stringValue.split("&");
				myHash = parts[0];
				myTimestamp = Integer.parseInt(parts[1]);
				myPosition = new TextPosition(
						Integer.parseInt(parts[2]),
						Integer.parseInt(parts[3]),
						Integer.parseInt(parts[4])
						);
			}
			catch (ArrayIndexOutOfBoundsException e) {
				throw new DataFormatException("Can't deserialize Position: bad data");
			}
		}
		
		public String toString() {
			String stringValue = myHash + "&";
			stringValue += String.valueOf(myTimestamp) + "&";
			stringValue += myPosition.ParagraphIndex + "&";
			stringValue += myPosition.ElementIndex + "&";
			stringValue += myPosition.CharIndex;
			return stringValue;
		}
	}
}
