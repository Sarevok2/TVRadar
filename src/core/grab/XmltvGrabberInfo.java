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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Describes an XMLTV sub grabber, such as tv_grab_na_dd
 * 
 * @author Louis Amstutz
 */
public class XmltvGrabberInfo {
	/** This file has details about all the XMLTV subgrabbers */
	private static final String XMLTV_GRABBER_FILENAME = "resources" + 
			System.getProperty("file.separator") + "xmltv_grabbers.cfg";
	
	public String name="", region="", url="", notes="";
	
	@Override
	public String toString() {
		return name + " (" + region + ")";
	}
	
	/**
	 * Loads the info for all the sub grabbers from a file
	 * 
	 * @param grabberList - All the XmltvGrabberInfo objects will be added to this list
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void loadGrabberList(List<XmltvGrabberInfo> grabberList) throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(XMLTV_GRABBER_FILENAME));
		
		XmltvGrabberInfo currentInfo=null;
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() >= 7 && line.substring(0,7).equalsIgnoreCase("[start]")) {
				currentInfo = new XmltvGrabberInfo();
			}
			else if (line.length() >= 5 && line.substring(0,5).equalsIgnoreCase("[end]")) {
				if (currentInfo == null) throw new IOException("Parsing error. " + 
						XMLTV_GRABBER_FILENAME + " may be corrupt.");
				grabberList.add(currentInfo);
			}
			else if (line.length() >= 6 && line.substring(0,4).equalsIgnoreCase("name")) {
				if (currentInfo == null) throw new IOException("Parsing error. " + 
						XMLTV_GRABBER_FILENAME + " may be corrupt.");
				currentInfo.name = line.split("=")[1];
			}
			else if (line.length() >= 8 && line.substring(0,6).equalsIgnoreCase("region")) {
				if (currentInfo == null) throw new IOException("Parsing error. " + 
						XMLTV_GRABBER_FILENAME + " may be corrupt.");
				currentInfo.region = line.split("=")[1];
			}
			else if (line.length() >= 5 && line.substring(0,3).equalsIgnoreCase("url")) {
				if (currentInfo == null) throw new IOException("Parsing error. " + 
						XMLTV_GRABBER_FILENAME + " may be corrupt.");
				currentInfo.url = line.split("=")[1];
			}
			else if (line.length() >= 6 && line.substring(0,5).equalsIgnoreCase("notes")) {
				if (currentInfo == null) throw new IOException("Parsing error. " + 
						XMLTV_GRABBER_FILENAME + " may be corrupt.");
				currentInfo.notes = line.split("=")[1];
			}
		}

	}
}
