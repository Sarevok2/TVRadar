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

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.SimpleTimeZone;

/**
 * Grabs listings from www.schedulesdirect.org
 * 
 * @author Louis Amstutz
 */
public class SchedulesDirectGrabber extends SoapGrabber {
	/** Download start and end tags */
	private static final String DOWNLOAD_START_TAG = "<tms:download xmlns:tms='urn:TMSWebServices'>\n",
			DOWNLOAD_END_TAG = "</tms:download>\n";
	
	/**
	 * Constructor
	 * 
	 * @param _serverURL - URL of listings
	 * @param _username - Username
	 * @param _password - Password
	 */
	public SchedulesDirectGrabber(String _serverURL, String _username, String _password) {
		super(_serverURL, _username, _password);
	}
	
	@Override
	protected void setSOAPActionProperty(HttpURLConnection conn) {
		conn.setRequestProperty("SOAPAction", "urn:TMSWebServices:xtvdWebService#download");
	}
	
	@Override
	protected StringBuilder SOAPEnvelopeBody(long start, long end) {
		StringBuilder body = new StringBuilder();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(new SimpleTimeZone(0, "UTC"));
		
		body.append(DOWNLOAD_START_TAG);
		
		body.append("<startTime xsi:type='tms:dateTime'>");
		body.append(df.format(start));
		body.append("</startTime>\n");
		
		body.append("<endTime xsi:type='tms:dateTime'>");
		body.append(df.format(end));
		body.append("</endTime>\n");

		body.append(DOWNLOAD_END_TAG);
		
		return body;
	}

}
