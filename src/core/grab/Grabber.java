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

import java.io.IOException;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;

import javax.naming.ConfigurationException;
import org.xml.sax.InputSource;

/**
 * Implemented by classes that grab TV listings from an internet source
 * 
 * @author Louis Amstutz
 */
public interface Grabber {
	
	/**
	 * Grab the listings
	 * 
	 * @param listingsSource - The stream of this object will be set to the input stream
	 * 						   of the source.  This object is then passed to the parser
	 * 						   which can then parse the listings
	 * @param start - Start time in milliseconds
	 * @param end - Stop time in milliseconds
	 * @param progress - Monitors progress
	 * @throws ConfigurationException 
	 */
	public InputSource grabListings(long start, long end, GrabListingsWorker worker)
			throws SocketTimeoutException, ConnectException, ProtocolException, IOException, 
			ConfigurationException;
	
	/**
	 * Cancel the grab operation
	 */
	public void cancelGrab();
}
