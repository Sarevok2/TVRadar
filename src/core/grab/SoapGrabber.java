/**
 * TV Radar
 * Copyright 2009-2010 Louis Amstutz (louis34@gmail.com)
 * 
 *   This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package core.grab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import org.xml.sax.InputSource;

/**
 * Grabs XML listings from a source using the SOAP protocol
 * Must be overridden by a subclass that builds the SOAP envelope body for the specific source
 * 
 * @author Louis Amstutz
 */
public abstract class SoapGrabber implements Grabber {
	
	/** The start of the SOAP envelope.  The body will be inserted by a subclass */
	private static final String SOAP_ENVELOPE_START = "<?xml version='1.0' encoding='UTF-8'?>\n" +
		"<SOAP-ENV:Envelope SOAP-ENV:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/' " +
		"xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' " +
		"xmlns:xsd='http://www.w3.org/2001/XMLSchema' " +
		"xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
		"xmlns:SOAP-ENC='http://schemas.xmlsoap.org/soap/encoding/'>\n" +
		"<SOAP-ENV:Body>\n";
	
	/** End of the SOAP envelope */
	private static final String SOAP_ENVELOPE_END = "</SOAP-ENV:Body>\n</SOAP-ENV:Envelope>";
	
	private HttpURLConnection conn = null;
	
	private String username, password, serverURL;
	
	/** True if we were redirected on authentication, which probably means authentication failed */
	private boolean redirected = false;
	
	/**
	 * Constructor
	 * 
	 * @param _serverURL - URL of the source
	 * @param _username - Username
	 * @param _password - Password
	 */
	public SoapGrabber(String _serverURL, String _username, String _password) {
		serverURL = _serverURL;
		username = _username;
		password = _password;
	}
	
	@Override
	public InputSource grabListings(long start, long end, GrabListingsWorker worker) 
			throws SocketTimeoutException, ConnectException, ProtocolException, IOException {
		
		Authenticator.setDefault(new TVAuthenticator(username, password, this));

		worker.publishPublic("Connecting");
		
		try {
			conn = (HttpURLConnection) (new URL(serverURL)).openConnection();
			
			
			conn.setRequestProperty("Accept-Encoding", "gzip");
			setSOAPActionProperty(conn);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
		
			PrintWriter pw = new PrintWriter(conn.getOutputStream());
			pw.println(buildDownloadSOAPEnvelope(start, end));
			pw.flush();
			pw.close();
		
			BufferedReader br = null;
			String enc = conn.getHeaderField("Content-encoding");

		    if (enc != null && enc.equals("gzip")) {
		        br = new BufferedReader(new InputStreamReader(
		        		new GZIPInputStream(conn.getInputStream()), "UTF-8"));
		    }
		    else br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

		    worker.setProgressPublic(5);
		    worker.publishPublic("Downloading listings");
		
		    int response = conn.getResponseCode();
		    if (response == HttpURLConnection.HTTP_OK) {
				/*java.io.OutputStreamWriter out = new java.io.OutputStreamWriter(
					new java.io.FileOutputStream("SD Output.xml"), "UTF-8");
				String line;
				while ((line = br.readLine()) != null) {
					out.write(line + "\n");
				}
				out.close();*/
				return new InputSource(br);
			}
		    else if (response == HttpURLConnection.HTTP_UNAUTHORIZED || 
		    		response == HttpURLConnection.HTTP_FORBIDDEN) 
		    	throw new ConnectException("Login failed.  Please check username/password");
		    else if (response == HttpURLConnection.HTTP_CLIENT_TIMEOUT ||
					response == HttpURLConnection.HTTP_GATEWAY_TIMEOUT) 
		    	throw new SocketTimeoutException("Connection Timed out");
			else throw new ConnectException("Unknown connection error");
		}
	    catch (IOException e) {
			if (redirected) {
				ConnectException ce = new ConnectException(
						"Login failed.  Please check username/password");
				ce.initCause(e);
				throw ce;
			}
			else throw e;
		}
	}
	
	/**
	 * Used by the subclass to set the SOAPAction property for the specific source
	 * 
	 * @param conn - The connection to set the property on
	 */
	protected abstract void setSOAPActionProperty(HttpURLConnection conn);
	
	public void setRedirected(boolean _redirected) {
		redirected = _redirected;
	}
	
	/**
	 * Build the SOAP Envelope
	 * 
	 * @param start - Start time in millis
	 * @param end - End time in millis
	 * @return - The SOAP envelope
	 */
	private String buildDownloadSOAPEnvelope(long start, long end) {
		StringBuilder envelope = new StringBuilder();
		envelope.append(SoapGrabber.SOAP_ENVELOPE_START);
		
		envelope.append(SOAPEnvelopeBody(start,end));
		envelope.append(SOAP_ENVELOPE_END);
		
		return envelope.toString();
	}
	
	/**
	 * Overridden by the subclass to provide the body of the SOAP envelope
	 * 
	 * @param start - Start time in millis
	 * @param end - Stop time in millis
	 * @return - The body of the SOAP envelope
	 */
	protected abstract StringBuilder SOAPEnvelopeBody(long start, long end);
	
	@Override
	public void cancelGrab(){}
	
	/**
	 * Authenticator class used to authenticate with the source
	 * 
	 * @author Louis Amstutz
	 */
	private class TVAuthenticator extends Authenticator {
		private String username, password;
		SoapGrabber grabber;
		
		/** Number of times Authenticator has been called.  If more then once we were redirected */
		int count=0;

		/**
		 * Constructor
		 * 
		 * @param user - Username
		 * @param pass - Password
		 * @param _grabber - Reference to Parent SoapGrabber object
		 */
		public TVAuthenticator(String user, String pass, SoapGrabber _grabber) {
			username = user;
			password = pass;
			grabber = _grabber;
		}
		
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			if (count > 0) {
				grabber.setRedirected(true); //If we were redirected, that means the login failed
				return null;
			}
			count ++;
			return new PasswordAuthentication(username, password.toCharArray());
		}
	}
}
