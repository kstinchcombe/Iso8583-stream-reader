package util;

import java.io.InputStream;
import libs.Logger;   //use your favorite play.Logger or equivalent.

/**
 * Sip ISO-8583 format financial transactions with two-byte length headers
 * from an input stream, e.g. within a socket server, and then
 * fill the message based on that header and return as a byte array.
 *  
 * @author kai
 *
 */
public class IsoStreamReader {

	private InputStream in;
	boolean verbose = false;
	
	public IsoStreamReader(InputStream in) {
		this.in = in;
	}
	
	/**
	 * read an iso transaction out of the pipe. returns null if socket
	 * closes or fails to return, or there is an error.
	 * 
	 * like StreamReader.readline this will hang indefinitely for the
	 * message to finish, (rather than helpfully timing out if the
	 * transaction is incomplete, for example).
	 * 
	 * @return the transaction as a byte[], or null if the pipe closes
	 */
	public byte[] readIso() {
		int messageLength = -2;
		int i = -1;
		try {
			if (verbose) Logger.info("* Waiting for iso read. *");
			
			// read message length
			int byte1 = in.read();
			int byte2 = in.read();
			if (byte1 == -1 || byte2 == -1) {
				return null;
			}

			// initialize our input array
			messageLength = byte1 + byte2 * 256;
			if (verbose) Logger.info("* Beginning iso read: length = %s. *", messageLength);
			byte[] iso = new byte[messageLength];
			int newByte = 0;
			
			// read the message body
			// TODO handle a timeout somehow
			for (i=0; i<messageLength; i++) {
				newByte = in.read();
				if (newByte == -1) {
					return null;
				}
				iso[i] = (byte)newByte;
			}
			if (verbose) Logger.info("* Finished iso read: %s *", new String(iso));

			return iso;
			
		} catch (Exception e) {
			Logger.error("Error accepting an iso transaction! Message length was %s, read %s bytes so far.", messageLength, i);
			e.printStackTrace();
		}
		
		return null;
	}
}
